package com.nolly.surecharge.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.RemoteViews
import androidx.core.content.edit
import com.nolly.surecharge.MainActivity
import com.nolly.surecharge.R
import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.data.battery.BatteryRulesStore
import com.nolly.surecharge.data.SnoozeManager
import com.nolly.surecharge.data.profile.BatteryProfile
import com.nolly.surecharge.data.profile.BatteryProfilesRepository
import com.nolly.surecharge.data.schedule.EffectiveRulesResult
import com.nolly.surecharge.data.schedule.ScheduleResolver
import com.nolly.surecharge.data.schedule.SchedulesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SureChargeWidgetProvider : AppWidgetProvider() {

	override fun onUpdate(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetIds: IntArray
	) {
		for (id in appWidgetIds) {
			updateAppWidget(context, appWidgetManager, id)
		}
	}

	override fun onDeleted(context: Context, appWidgetIds: IntArray) {
		super.onDeleted(context, appWidgetIds)
		val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		appWidgetIds.forEach { id ->
			prefs.edit {
				remove(keyStyle(id))
				remove(keyShowNext(id))
				remove(keyShowProfile(id))
				remove(keyQuickAction(id))
				remove(keyLastProfileIndex(id))
			}
		}
	}

	override fun onReceive(context: Context, intent: Intent) {
		super.onReceive(context, intent)

		when (intent.action) {
			ACTION_WIDGET_QUICK_1,
			ACTION_WIDGET_QUICK_2 -> {
				val widgetId =
					intent.getIntExtra(
						AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID
					)
				if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
					handleQuickAction(context, widgetId, intent.action == ACTION_WIDGET_QUICK_1)
					updateAllWidgets(context)
				}
			}
		}
	}

	private fun handleQuickAction(context: Context, appWidgetId: Int, isFirst: Boolean) {
		val config = loadConfig(context, appWidgetId)
		val action = when {
			config.style == WidgetStyle.EXTENDED && isFirst -> WidgetQuickAction.SNOOZE_1H
			config.style == WidgetStyle.EXTENDED && !isFirst -> WidgetQuickAction.CYCLE_PROFILES
			!isFirst -> WidgetQuickAction.NONE
			else -> config.quickAction
		}

		when (action) {
			WidgetQuickAction.NONE -> Unit
			WidgetQuickAction.SNOOZE_1H -> {
				SnoozeManager.from(context).snoozeForMinutes(60)
			}
			WidgetQuickAction.OPEN_APP -> {
				val i = Intent(context, MainActivity::class.java).apply {
					flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
				}
				context.startActivity(i)
			}
			WidgetQuickAction.CYCLE_PROFILES -> {
				cycleProfile(context)
			}
		}
	}

	private fun cycleProfile(context: Context) {
		runBlocking {
			val rulesStore = BatteryRulesStore(context)
			val profilesRepo = BatteryProfilesRepository(context)

			val currentRules = rulesStore.rulesFlow.first()
			val profiles = profilesRepo.profilesFlow.first().sortedBy { it.id }

			if (profiles.isEmpty()) return@runBlocking

			val currentIndex = profiles.indexOfFirst { profileMatchesRules(it, currentRules) }
			val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % profiles.size
			val nextProfile = profiles[nextIndex]

			rulesStore.setRules(nextProfile.toRules())
		}
	}

	private fun profileMatchesRules(profile: BatteryProfile, rules: BatteryRules): Boolean {
		return profile.lowLevelEnabled == rules.lowLevelEnabled &&
				profile.lowLevelPercentage == rules.lowLevelPercentage &&
				profile.highLevelEnabled == rules.highLevelEnabled &&
				profile.highLevelPercentage == rules.highLevelPercentage &&
				(profile.repeatIntervalMinutes ?: 0) == (rules.repeatIntervalMinutes ?: 0)
	}

	fun updateAppWidget(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetId: Int
	) {
		val config = loadConfig(context, appWidgetId)

		val battery = readBatterySnapshot(context)
		val effective = readEffectiveRules(context)
		val rules = effective.rules
		val activeScheduleName = effective.activeSchedule?.name
		val activeProfileName = findActiveProfileName(rules, context)

		val layoutId = when (config.style) {
			WidgetStyle.COMPACT -> R.layout.widget_surecharge_compact
			WidgetStyle.STANDARD -> R.layout.widget_surecharge
			WidgetStyle.EXTENDED -> R.layout.widget_surecharge_extended
		}

		val views = RemoteViews(context.packageName, layoutId)

		val percentText = if (battery.percent >= 0) "${battery.percent}%" else "—"
		views.setTextViewText(R.id.widget_battery_percent, percentText)

		views.setTextViewText(R.id.widget_rules, buildRulesSummary(rules))

		val nextText = buildNextAlertText(rules, battery.percent, battery.isCharging)
		if (config.showNextEvent) {
			when (config.style) {
				WidgetStyle.STANDARD,
				WidgetStyle.EXTENDED -> views.setTextViewText(R.id.widget_next_event, nextText)
				else -> Unit
			}
		} else {
			if (config.style != WidgetStyle.COMPACT) {
				views.setTextViewText(R.id.widget_next_event, "")
			}
		}

		if (config.style == WidgetStyle.EXTENDED && config.showActiveProfile) {
			val label = when {
				activeProfileName != null -> "Profile: $activeProfileName"
				activeScheduleName != null -> "Schedule: $activeScheduleName"
				else -> "Custom rules"
			}
			views.setTextViewText(R.id.widget_active_profile, label)
		}

		val openIntent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
		}
		val openPending = PendingIntent.getActivity(
			context,
			appWidgetId,
			openIntent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		views.setOnClickPendingIntent(R.id.widget_root, openPending)

		when (config.style) {
			WidgetStyle.COMPACT -> {
			}
			WidgetStyle.STANDARD -> {
				val quickIntent = Intent(context, SureChargeWidgetProvider::class.java).apply {
					action = ACTION_WIDGET_QUICK_1
					putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
				}
				val quickPending = PendingIntent.getBroadcast(
					context,
					appWidgetId,
					quickIntent,
					PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
				)
				views.setOnClickPendingIntent(R.id.widget_quick_action, quickPending)
			}
			WidgetStyle.EXTENDED -> {
				val quick1Intent = Intent(context, SureChargeWidgetProvider::class.java).apply {
					action = ACTION_WIDGET_QUICK_1
					putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
				}
				val quick1Pending = PendingIntent.getBroadcast(
					context,
					appWidgetId * 10 + 1,
					quick1Intent,
					PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
				)
				views.setOnClickPendingIntent(R.id.widget_quick_action_1, quick1Pending)

				val quick2Intent = Intent(context, SureChargeWidgetProvider::class.java).apply {
					action = ACTION_WIDGET_QUICK_2
					putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
				}
				val quick2Pending = PendingIntent.getBroadcast(
					context,
					appWidgetId * 10 + 2,
					quick2Intent,
					PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
				)
				views.setOnClickPendingIntent(R.id.widget_quick_action_2, quick2Pending)
			}
		}

		appWidgetManager.updateAppWidget(appWidgetId, views)
	}

	private fun readEffectiveRules(context: Context): EffectiveRulesResult {
		return runBlocking {
			val rulesStore = BatteryRulesStore(context)
			val profilesRepo = BatteryProfilesRepository(context)
			val schedulesRepo = SchedulesRepository(context)

			val baseRules = rulesStore.rulesFlow.first()
			val profiles = profilesRepo.profilesFlow.first()
			val schedules = schedulesRepo.schedulesFlow.first()

			ScheduleResolver.resolve(
				baseRules = baseRules,
				schedules = schedules,
				profiles = profiles,
				nowMillis = System.currentTimeMillis()
			)
		}
	}

	private fun findActiveProfileName(rules: BatteryRules, context: Context): String? {
		return runBlocking {
			val repo = BatteryProfilesRepository(context)
			val profiles = repo.profilesFlow.first()
			profiles.firstOrNull { profileMatchesRules(it, rules) }?.name
		}
	}

	private data class BatterySnapshot(
		val percent: Int,
		val isCharging: Boolean
	)

	private fun readBatterySnapshot(context: Context): BatterySnapshot {
		val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
		val intent = context.registerReceiver(null, filter)

		val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
		val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
		val status = intent?.getIntExtra(
			BatteryManager.EXTRA_STATUS,
			BatteryManager.BATTERY_STATUS_UNKNOWN
		) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

		val isCharging =
			status == BatteryManager.BATTERY_STATUS_CHARGING ||
					status == BatteryManager.BATTERY_STATUS_FULL

		val percent = if (level >= 0 && scale > 0) level * 100 / scale else -1
		return BatterySnapshot(percent, isCharging)
	}

	private fun buildRulesSummary(rules: BatteryRules): String {
		val parts = mutableListOf<String>()
		if (rules.lowLevelEnabled) {
			parts.add("Low ${rules.lowLevelPercentage}%")
		}
		if (rules.highLevelEnabled) {
			parts.add("High ${rules.highLevelPercentage}%")
		}
		return if (parts.isEmpty()) {
			"Alerts off"
		} else {
			parts.joinToString(" · ")
		}
	}

	private fun buildNextAlertText(
		rules: BatteryRules,
		percent: Int,
		isCharging: Boolean
	): String {
		if (percent < 0) return "Next alert: —"

		return if (isCharging && rules.highLevelEnabled) {
			if (percent >= rules.highLevelPercentage) {
				"High alert passed"
			} else {
				"Alert at ${rules.highLevelPercentage}% (high)"
			}
		} else if (!isCharging && rules.lowLevelEnabled) {
			if (percent <= rules.lowLevelPercentage) {
				"Low alert passed"
			} else {
				"Alert at ${rules.lowLevelPercentage}% (low)"
			}
		} else {
			"No alert configured"
		}
	}

	private fun loadConfig(context: Context, appWidgetId: Int): WidgetConfig {
		val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		val styleName = prefs.getString(keyStyle(appWidgetId), null)
		val style = WidgetStyle.entries.firstOrNull { it.name == styleName }
			?: WidgetStyle.STANDARD

		val showNext = prefs.getBoolean(keyShowNext(appWidgetId), true)
		val showProfile = prefs.getBoolean(keyShowProfile(appWidgetId), true)

		val quickName = prefs.getString(keyQuickAction(appWidgetId), null)
		val quick = WidgetQuickAction.entries.firstOrNull { it.name == quickName }
			?: WidgetQuickAction.SNOOZE_1H

		return WidgetConfig(
			style = style,
			showNextEvent = showNext,
			showActiveProfile = showProfile,
			quickAction = quick
		)
	}

	companion object {
		private const val PREFS_NAME = "surecharge_widget_prefs"

		private const val KEY_STYLE_PREFIX = "style_"
		private const val KEY_SHOW_NEXT_PREFIX = "show_next_"
		private const val KEY_SHOW_PROFILE_PREFIX = "show_profile_"
		private const val KEY_QUICK_ACTION_PREFIX = "quick_action_"
		private const val KEY_LAST_PROFILE_INDEX_PREFIX = "last_profile_index_"

		private const val ACTION_WIDGET_QUICK_1 = "com.nolly.surecharge.widget.ACTION_WIDGET_QUICK_1"
		private const val ACTION_WIDGET_QUICK_2 = "com.nolly.surecharge.widget.ACTION_WIDGET_QUICK_2"

		private fun keyStyle(id: Int) = "$KEY_STYLE_PREFIX$id"
		private fun keyShowNext(id: Int) = "$KEY_SHOW_NEXT_PREFIX$id"
		private fun keyShowProfile(id: Int) = "$KEY_SHOW_PROFILE_PREFIX$id"
		private fun keyQuickAction(id: Int) = "$KEY_QUICK_ACTION_PREFIX$id"
		private fun keyLastProfileIndex(id: Int) = "$KEY_LAST_PROFILE_INDEX_PREFIX$id"

		fun saveConfig(context: Context, appWidgetId: Int, config: WidgetConfig) {
			val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			prefs.edit {
				putString(keyStyle(appWidgetId), config.style.name)
				putBoolean(keyShowNext(appWidgetId), config.showNextEvent)
				putBoolean(keyShowProfile(appWidgetId), config.showActiveProfile)
				putString(keyQuickAction(appWidgetId), config.quickAction.name)
			}
		}

		fun updateAllWidgets(context: Context) {
			val appWidgetManager = AppWidgetManager.getInstance(context)
			val component = ComponentName(context, SureChargeWidgetProvider::class.java)
			val ids = appWidgetManager.getAppWidgetIds(component)
			for (id in ids) {
				SureChargeWidgetProvider().updateAppWidget(context, appWidgetManager, id)
			}
		}
	}
}

enum class WidgetStyle {
	COMPACT,
	STANDARD,
	EXTENDED
}

enum class WidgetQuickAction {
	NONE,
	SNOOZE_1H,
	OPEN_APP,
	CYCLE_PROFILES
}

data class WidgetConfig(
	val style: WidgetStyle = WidgetStyle.STANDARD,
	val showNextEvent: Boolean = true,
	val showActiveProfile: Boolean = true,
	val quickAction: WidgetQuickAction = WidgetQuickAction.SNOOZE_1H
)
