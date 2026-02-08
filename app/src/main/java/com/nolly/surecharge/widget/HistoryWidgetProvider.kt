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
import com.nolly.surecharge.data.history.ChargeHistoryRepository
import com.nolly.surecharge.data.history.ChargeSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt

class HistoryWidgetProvider : AppWidgetProvider() {

	override fun onUpdate(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetIds: IntArray
	) {
		for (id in appWidgetIds) {
			updateAppWidget(context, appWidgetManager, id)
		}
	}

	fun updateAppWidget(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetId: Int
	) {
		val config = loadConfig(context, appWidgetId)

		val recent: List<ChargeSession> = runBlocking {
			val repo = ChargeHistoryRepository(context)
			repo.sessionsForLastDays(config.daysWindow).first()
		}

		val (sparkline, summary, windowLabel) =
			if (recent.isEmpty()) {
				Triple("—", "Not enough data", "Last ${config.daysWindow} days")
			} else {
				val avgStart = recent.map { it.startLevel }.average()
					.takeIf { !it.isNaN() }?.roundToInt()
				val avgEnd = recent.map { it.endLevel }.average()
					.takeIf { !it.isNaN() }?.roundToInt()

				val spark = buildSparkline(recent)
				val summaryText = when {
					avgStart != null && avgEnd != null ->
						"Avg start $avgStart% · end $avgEnd%"
					avgEnd != null ->
						"Avg end $avgEnd%"
					else ->
						"History over ${config.daysWindow} days"
				}
				val label = "Last ${config.daysWindow} days"
				Triple(spark, summaryText, label)
			}

		val layoutId = when (config.style) {
			HistoryWidgetStyle.COMPACT -> R.layout.widget_history_compact
			HistoryWidgetStyle.LARGE -> R.layout.widget_history_large
		}

		val views = RemoteViews(context.packageName, layoutId)
		views.setTextViewText(R.id.widget_history_sparkline, sparkline)
		views.setTextViewText(R.id.widget_history_summary, summary)
		if (config.style == HistoryWidgetStyle.LARGE) {
			views.setTextViewText(R.id.widget_history_window, windowLabel)
		}

		val openIntent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			putExtra(EXTRA_DESTINATION, "history")
		}
		val openPending = PendingIntent.getActivity(
			context,
			appWidgetId,
			openIntent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		views.setOnClickPendingIntent(R.id.widget_history_root, openPending)

		appWidgetManager.updateAppWidget(appWidgetId, views)
	}

	private fun buildSparkline(sessions: List<ChargeSession>): String {
		if (sessions.isEmpty()) return "—"

		val glyphs = charArrayOf('▁', '▂', '▃', '▄', '▅', '▆', '▇', '█')

		val lastSessions = sessions.takeLast(8)
		val ends = lastSessions.map { it.endLevel }.ifEmpty { return "—" }

		return ends.joinToString(separator = "") { level ->
			val clamped = level.coerceIn(0, 100)
			val bucket = (clamped / 100.0 * (glyphs.size - 1)).roundToInt()
			glyphs[bucket].toString()
		}
	}

	private fun loadConfig(context: Context, appWidgetId: Int): HistoryWidgetConfig {
		val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		val styleName = prefs.getString(keyStyle(appWidgetId), null)
		val style = HistoryWidgetStyle.entries.firstOrNull { it.name == styleName }
			?: HistoryWidgetStyle.LARGE

		val days = prefs.getInt(keyDays(appWidgetId), 14)

		return HistoryWidgetConfig(
			style = style,
			daysWindow = days
		)
	}

	companion object {
		private const val PREFS_NAME = "history_widget_prefs"
		private const val KEY_STYLE_PREFIX = "style_"
		private const val KEY_DAYS_PREFIX = "days_"

		private const val EXTRA_DESTINATION = "destination"

		private fun keyStyle(id: Int) = "$KEY_STYLE_PREFIX$id"
		private fun keyDays(id: Int) = "$KEY_DAYS_PREFIX$id"

		fun saveConfig(context: Context, appWidgetId: Int, config: HistoryWidgetConfig) {
			val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			prefs.edit().apply {
				putString(keyStyle(appWidgetId), config.style.name)
				putInt(keyDays(appWidgetId), config.daysWindow)
				apply()
			}
		}

		fun updateAllWidgets(context: Context) {
			val mgr = AppWidgetManager.getInstance(context)
			val component = ComponentName(context, HistoryWidgetProvider::class.java)
			val ids = mgr.getAppWidgetIds(component)
			for (id in ids) {
				HistoryWidgetProvider().updateAppWidget(context, mgr, id)
			}
		}
	}
}

enum class HistoryWidgetStyle {
	COMPACT,
	LARGE
}

data class HistoryWidgetConfig(
	val style: HistoryWidgetStyle,
	val daysWindow: Int
)
