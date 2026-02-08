package com.nolly.surecharge.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nolly.surecharge.system.AutoTuneWorker
import com.nolly.surecharge.util.AppLogger

class BootReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent?) {
		AppLogger.i(TAG, "onReceive: action=${intent?.action}")

		if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
			AppLogger.i(TAG, "BOOT_COMPLETED received, starting BatteryMonitorService and scheduling AutoTuneWorker")

			val serviceIntent = Intent(context, BatteryMonitorService::class.java)
			try {
				context.startForegroundService(serviceIntent)
				AppLogger.d(TAG, "startForegroundService(BatteryMonitorService) from BootReceiver succeeded")
			} catch (t: Throwable) {
				AppLogger.e(TAG, "Failed to start BatteryMonitorService from BootReceiver", t)
			}

			try {
				AutoTuneWorker.schedule(context)
				AppLogger.d(TAG, "AutoTuneWorker.schedule() called successfully")
			} catch (t: Throwable) {
				AppLogger.e(TAG, "Failed to schedule AutoTuneWorker", t)
			}
		}
	}

	companion object {
		private const val TAG = "BootReceiver"
	}
}
