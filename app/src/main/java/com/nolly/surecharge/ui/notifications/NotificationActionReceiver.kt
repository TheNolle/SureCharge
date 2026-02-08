package com.nolly.surecharge.ui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.nolly.surecharge.MainActivity
import com.nolly.surecharge.data.SnoozeManager
import com.nolly.surecharge.util.AppLogger
import java.util.Calendar

class NotificationActionReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		val action = intent.getStringExtra(EXTRA_ACTION)
		AppLogger.i(TAG, "onReceive: action=$action intent=$intent")

		if (action == null) {
			AppLogger.w(TAG, "Received notification action without EXTRA_ACTION; ignoring")
			return
		}

		val appContext = context.applicationContext
		val snoozeManager = SnoozeManager.from(appContext)

		when (action) {
			ACTION_SNOOZE_60 -> {
				AppLogger.i(TAG, "Handling ACTION_SNOOZE_60")
				snoozeManager.snoozeForMinutes(60)
				dismissAlerts(appContext)
			}
			ACTION_SNOOZE_1440 -> {
				AppLogger.i(TAG, "Handling ACTION_SNOOZE_1440 (24h)")
				snoozeManager.snoozeForMinutes(60 * 24)
				dismissAlerts(appContext)
			}
			ACTION_DISABLE_TODAY -> {
				val minutes = computeMinutesUntilNextMorning()
				AppLogger.i(TAG, "Handling ACTION_DISABLE_TODAY, snoozing for $minutes minutes")
				snoozeManager.snoozeForMinutes(minutes)
				dismissAlerts(appContext)
			}
			ACTION_OPEN_LOW_RULE,
			ACTION_OPEN_HIGH_RULE -> {
				AppLogger.i(TAG, "Handling open rules action: $action")
				openRulesScreen(appContext)
				dismissAlerts(appContext)
			}
			else -> {
				AppLogger.w(TAG, "Unknown notification action: $action")
			}
		}
	}

	private fun dismissAlerts(context: Context) {
		AppLogger.d(TAG, "Dismissing alert notifications")
		val nm = NotificationManagerCompat.from(context)
		nm.cancel(Notifications.ID_LOW_ALERT)
		nm.cancel(Notifications.ID_HIGH_ALERT)
	}

	private fun openRulesScreen(context: Context) {
		AppLogger.d(TAG, "Opening MainActivity from notification action")
		val intent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
		}
		context.startActivity(intent)
	}

	private fun computeMinutesUntilNextMorning(): Int {
		val now = System.currentTimeMillis()
		val cal = Calendar.getInstance().apply { timeInMillis = now }

		if (cal.get(Calendar.HOUR_OF_DAY) >= 3) {
			cal.add(Calendar.DAY_OF_YEAR, 1)
		}
		cal.set(Calendar.HOUR_OF_DAY, 3)
		cal.set(Calendar.MINUTE, 0)
		cal.set(Calendar.SECOND, 0)
		cal.set(Calendar.MILLISECOND, 0)

		val diffMillis = cal.timeInMillis - now
		val minutes = (diffMillis / 60_000L).toInt()
		val result = if (minutes > 0) minutes else 60
		AppLogger.d(TAG, "computeMinutesUntilNextMorning: result=$result")
		return result
	}

	companion object {
		private const val TAG = "NotifActionReceiver"

		const val EXTRA_ACTION = "com.nolly.surecharge.ACTION"

		const val ACTION_SNOOZE_60 = "SNOOZE_60"
		const val ACTION_SNOOZE_1440 = "SNOOZE_1440"
		const val ACTION_DISABLE_TODAY = "DISABLE_TODAY"
		const val ACTION_OPEN_LOW_RULE = "OPEN_LOW_RULE"
		const val ACTION_OPEN_HIGH_RULE = "OPEN_HIGH_RULE"
	}
}
