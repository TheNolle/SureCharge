package com.nolly.surecharge.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nolly.surecharge.MainActivity
import com.nolly.surecharge.R
import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.data.battery.BatteryRulesStore
import com.nolly.surecharge.data.profile.BatteryProfile
import com.nolly.surecharge.data.profile.BatteryProfilesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ProfileWidgetProvider : AppWidgetProvider() {

	override fun onUpdate(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetIds: IntArray
	) {
		for (id in appWidgetIds) {
			updateAppWidget(context, appWidgetManager, id)
		}
	}

	override fun onReceive(context: Context, intent: Intent) {
		super.onReceive(context, intent)

		when (intent.action) {
			ACTION_NEXT_PROFILE -> {
				val widgetId =
					intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
				if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
					cycleProfile(context)
					updateAllWidgets(context)
				}
			}
			ACTION_OPEN_APP -> {
				val i = Intent(context, MainActivity::class.java).apply {
					flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
					putExtra(EXTRA_DESTINATION, "profiles")
				}
				context.startActivity(i)
			}
		}
	}

	fun updateAppWidget(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetId: Int
	) {
		val config = loadConfig(context, appWidgetId)

		val (currentRules, profiles) = runBlocking {
			val rulesStore = BatteryRulesStore(context)
			val repo = BatteryProfilesRepository(context)
			val rules = rulesStore.rulesFlow.first()
			val list = repo.profilesFlow.first()
			rules to list
		}

		val activeProfile = profiles.firstOrNull { matchesProfile(it, currentRules) }
		val activeName = activeProfile?.name ?: "Custom rules"

		val layoutId = when (config.style) {
			ProfileWidgetStyle.SMALL -> R.layout.widget_profile_small
			ProfileWidgetStyle.LARGE -> R.layout.widget_profile_large
		}

		val views = RemoteViews(context.packageName, layoutId)

		views.setTextViewText(R.id.widget_profile_name, activeName)

		if (config.style == ProfileWidgetStyle.LARGE) {
			val subtitle = if (config.showSubtitle) {
				if (activeProfile != null) "Tap Next to switch profile" else "No profile matches current rules"
			} else {
				""
			}
			views.setTextViewText(R.id.widget_profile_subtitle, subtitle)
		}

		// Root click → open app to Profiles area
		val openIntent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			putExtra(EXTRA_DESTINATION, "profiles")
		}
		val openPending = PendingIntent.getActivity(
			context,
			appWidgetId,
			openIntent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		views.setOnClickPendingIntent(R.id.widget_profile_root, openPending)

		if (config.style == ProfileWidgetStyle.LARGE) {
			// "Next" button
			val nextIntent = Intent(context, ProfileWidgetProvider::class.java).apply {
				action = ACTION_NEXT_PROFILE
				putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
			}
			val nextPending = PendingIntent.getBroadcast(
				context,
				appWidgetId * 10 + 1,
				nextIntent,
				PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
			)
			views.setOnClickPendingIntent(R.id.widget_profile_next, nextPending)

			// "Open" button
			val openButtonIntent = Intent(context, ProfileWidgetProvider::class.java).apply {
				action = ACTION_OPEN_APP
				putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
			}
			val openButtonPending = PendingIntent.getBroadcast(
				context,
				appWidgetId * 10 + 2,
				openButtonIntent,
				PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
			)
			views.setOnClickPendingIntent(R.id.widget_profile_open, openButtonPending)
		}

		appWidgetManager.updateAppWidget(appWidgetId, views)
	}

	private fun matchesProfile(profile: BatteryProfile, rules: BatteryRules): Boolean {
		return profile.lowLevelEnabled == rules.lowLevelEnabled &&
				profile.lowLevelPercentage == rules.lowLevelPercentage &&
				profile.highLevelEnabled == rules.highLevelEnabled &&
				profile.highLevelPercentage == rules.highLevelPercentage &&
				(profile.repeatIntervalMinutes ?: 0) == (rules.repeatIntervalMinutes ?: 0)
	}

	private fun cycleProfile(context: Context) {
		runBlocking {
			val rulesStore = BatteryRulesStore(context)
			val repo = BatteryProfilesRepository(context)

			val rules = rulesStore.rulesFlow.first()
			val profiles = repo.profilesFlow.first().sortedBy { it.id }

			if (profiles.isEmpty()) return@runBlocking

			val index = profiles.indexOfFirst { matchesProfile(it, rules) }
			val nextIndex = if (index == -1) 0 else (index + 1) % profiles.size
			val nextProfile = profiles[nextIndex]

			rulesStore.setRules(nextProfile.toRules())
		}
	}

	private fun loadConfig(context: Context, appWidgetId: Int): ProfileWidgetConfig {
		val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		val styleName = prefs.getString(keyStyle(appWidgetId), null)
		val style = ProfileWidgetStyle.entries.firstOrNull { it.name == styleName }
			?: ProfileWidgetStyle.LARGE

		val showSubtitle = prefs.getBoolean(keySubtitle(appWidgetId), true)

		return ProfileWidgetConfig(
			style = style,
			showSubtitle = showSubtitle
		)
	}

	companion object {
		private const val PREFS_NAME = "profile_widget_prefs"
		private const val KEY_STYLE_PREFIX = "style_"
		private const val KEY_SUBTITLE_PREFIX = "subtitle_"

		private const val ACTION_NEXT_PROFILE =
			"com.nolly.surecharge.widget.ACTION_PROFILE_NEXT"
		private const val ACTION_OPEN_APP =
			"com.nolly.surecharge.widget.ACTION_PROFILE_OPEN"

		private const val EXTRA_DESTINATION = "destination"

		private fun keyStyle(id: Int) = "$KEY_STYLE_PREFIX$id"
		private fun keySubtitle(id: Int) = "$KEY_SUBTITLE_PREFIX$id"

		fun saveConfig(context: Context, appWidgetId: Int, config: ProfileWidgetConfig) {
			val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			prefs.edit().apply {
				putString(keyStyle(appWidgetId), config.style.name)
				putBoolean(keySubtitle(appWidgetId), config.showSubtitle)
				apply()
			}
		}

		fun updateAllWidgets(context: Context) {
			val mgr = AppWidgetManager.getInstance(context)
			val component = ComponentName(context, ProfileWidgetProvider::class.java)
			val ids = mgr.getAppWidgetIds(component)
			for (id in ids) {
				ProfileWidgetProvider().updateAppWidget(context, mgr, id)
			}
		}
	}
}

enum class ProfileWidgetStyle {
	SMALL,
	LARGE
}

data class ProfileWidgetConfig(
	val style: ProfileWidgetStyle,
	val showSubtitle: Boolean
)
