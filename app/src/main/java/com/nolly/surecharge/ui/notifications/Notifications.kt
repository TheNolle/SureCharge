package com.nolly.surecharge.ui.notifications

import android.Manifest
import android.R.drawable
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nolly.surecharge.MainActivity

object Notifications {
	private const val CHANNEL_ID = "surecharge_alerts"
	private const val CHANNEL_NAME = "Battery alerts"
	private const val CHANNEL_DESC = "Low battery and charge limit alerts"

	const val ID_LOW_ALERT = 1001
	const val ID_HIGH_ALERT = 1002

	private const val REQUEST_CODE_SNOOZE_60 = 2001
	private const val REQUEST_CODE_SNOOZE_1440 = 2002
	private const val REQUEST_CODE_DISABLE_TODAY = 2003
	private const val REQUEST_CODE_OPEN_LOW_RULE = 2004
	private const val REQUEST_CODE_OPEN_HIGH_RULE = 2005

	private fun ensureChannel(context: Context) {
		val channel = NotificationChannel(
			CHANNEL_ID,
			CHANNEL_NAME,
			NotificationManager.IMPORTANCE_HIGH
		).apply {
			description = CHANNEL_DESC
		}
		val nm = context.getSystemService(NotificationManager::class.java)
		nm.createNotificationChannel(channel)
	}

	private fun buildContentIntent(context: Context): PendingIntent {
		val intent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
		}
		return PendingIntent.getActivity(
			context,
			0,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
	}

	private fun buildSnooze1hPendingIntent(context: Context): PendingIntent {
		val intent = Intent(context, NotificationActionReceiver::class.java).apply {
			putExtra(
				NotificationActionReceiver.EXTRA_ACTION,
				NotificationActionReceiver.ACTION_SNOOZE_60
			)
		}
		return PendingIntent.getBroadcast(
			context,
			REQUEST_CODE_SNOOZE_60,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
	}

	private fun buildSnooze24hPendingIntent(context: Context): PendingIntent {
		val intent = Intent(context, NotificationActionReceiver::class.java).apply {
			putExtra(
				NotificationActionReceiver.EXTRA_ACTION,
				NotificationActionReceiver.ACTION_SNOOZE_1440
			)
		}
		return PendingIntent.getBroadcast(
			context,
			REQUEST_CODE_SNOOZE_1440,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
	}

	private fun buildDisableTodayPendingIntent(context: Context): PendingIntent {
		val intent = Intent(context, NotificationActionReceiver::class.java).apply {
			putExtra(
				NotificationActionReceiver.EXTRA_ACTION,
				NotificationActionReceiver.ACTION_DISABLE_TODAY
			)
		}
		return PendingIntent.getBroadcast(
			context,
			REQUEST_CODE_DISABLE_TODAY,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
	}

	private fun buildOpenLowRulePendingIntent(context: Context): PendingIntent {
		val intent = Intent(context, NotificationActionReceiver::class.java).apply {
			putExtra(
				NotificationActionReceiver.EXTRA_ACTION,
				NotificationActionReceiver.ACTION_OPEN_LOW_RULE
			)
		}
		return PendingIntent.getBroadcast(
			context,
			REQUEST_CODE_OPEN_LOW_RULE,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
	}

	private fun buildOpenHighRulePendingIntent(context: Context): PendingIntent {
		val intent = Intent(context, NotificationActionReceiver::class.java).apply {
			putExtra(
				NotificationActionReceiver.EXTRA_ACTION,
				NotificationActionReceiver.ACTION_OPEN_HIGH_RULE
			)
		}
		return PendingIntent.getBroadcast(
			context,
			REQUEST_CODE_OPEN_HIGH_RULE,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
	}

	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	fun showLowBatteryAlert(context: Context, level: Int) {
		ensureChannel(context)

		val snooze1h = buildSnooze1hPendingIntent(context)
		val snooze24h = buildSnooze24hPendingIntent(context)
		val adjustLow = buildOpenLowRulePendingIntent(context)

		val notification = NotificationCompat.Builder(context, CHANNEL_ID)
			.setSmallIcon(drawable.stat_sys_warning)
			.setContentTitle("Battery is low")
			.setContentText("Your battery is at $level%. Time to plug in.")
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setAutoCancel(true)
			.setContentIntent(buildContentIntent(context))
			.addAction(
				drawable.ic_media_pause,
				"Snooze 1h",
				snooze1h
			)
			.addAction(
				drawable.ic_menu_recent_history,
				"Snooze 24h",
				snooze24h
			)
			.addAction(
				drawable.ic_menu_manage,
				"Adjust low threshold",
				adjustLow
			)
			.build()

		NotificationManagerCompat.from(context).notify(ID_LOW_ALERT, notification)
	}

	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	fun showHighChargeAlert(context: Context, level: Int) {
		ensureChannel(context)

		val snooze1h = buildSnooze1hPendingIntent(context)
		val disableToday = buildDisableTodayPendingIntent(context)
		val adjustHigh = buildOpenHighRulePendingIntent(context)

		val notification = NotificationCompat.Builder(context, CHANNEL_ID)
			.setSmallIcon(drawable.stat_sys_warning)
			.setContentTitle("Battery is charged")
			.setContentText("Your battery reached $level%. It's a good time to unplug.")
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setAutoCancel(true)
			.setContentIntent(buildContentIntent(context))
			.addAction(
				drawable.ic_media_pause,
				"Snooze 1h",
				snooze1h
			)
			.addAction(
				drawable.ic_menu_close_clear_cancel,
				"Turn off high alerts today",
				disableToday
			)
			.addAction(
				drawable.ic_menu_manage,
				"Adjust high limit",
				adjustHigh
			)
			.build()

		NotificationManagerCompat.from(context).notify(ID_HIGH_ALERT, notification)
	}
}
