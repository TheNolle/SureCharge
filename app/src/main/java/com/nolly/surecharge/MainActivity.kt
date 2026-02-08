package com.nolly.surecharge

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.nolly.surecharge.presentation.BatteryProfilesViewModel
import com.nolly.surecharge.presentation.HistoryViewModel
import com.nolly.surecharge.presentation.SchedulesViewModel
import com.nolly.surecharge.presentation.SureChargeViewModel
import com.nolly.surecharge.service.BatteryMonitorService
import com.nolly.surecharge.ui.SureChargeApp
import com.nolly.surecharge.util.AppLogger

class MainActivity : ComponentActivity() {
	private val rulesViewModel: SureChargeViewModel by viewModels()
	private val historyViewModel: HistoryViewModel by viewModels()
	private val profilesViewModel: BatteryProfilesViewModel by viewModels()
	private val schedulesViewModel: SchedulesViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		AppLogger.i(TAG, "onCreate: activity started, intent=$intent")

		requestNotificationPermissionIfNeeded()
		startBatteryMonitorService()

		setContent {
			AppLogger.d(TAG, "Composing SureChargeApp")
			SureChargeApp(
				rulesViewModel = rulesViewModel,
				historyViewModel = historyViewModel,
				profilesViewModel = profilesViewModel,
				schedulesViewModel = schedulesViewModel
			)
		}
	}

	private fun requestNotificationPermissionIfNeeded() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			val granted = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
					PackageManager.PERMISSION_GRANTED
			AppLogger.d(TAG, "Notification permission currently granted=$granted")
			if (!granted) {
				AppLogger.i(TAG, "Requesting POST_NOTIFICATIONS permission")
				requestPermissions(
					arrayOf(Manifest.permission.POST_NOTIFICATIONS),
					REQUEST_NOTIFICATIONS
				)
			}
		}
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String?>,
		grantResults: IntArray,
		deviceId: Int
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

		if (requestCode == REQUEST_NOTIFICATIONS) {
			val granted = grantResults.isNotEmpty() &&
					grantResults[0] == PackageManager.PERMISSION_GRANTED
			AppLogger.i(TAG, "Notification permission result: granted=$granted")
		}
	}

	private fun startBatteryMonitorService() {
		AppLogger.i(TAG, "Starting BatteryMonitorService")
		val intent = Intent(this, BatteryMonitorService::class.java)
		try {
			startForegroundService(intent)
			AppLogger.d(TAG, "startForegroundService for BatteryMonitorService succeeded")
		} catch (t: Throwable) {
			AppLogger.e(TAG, "Failed to start BatteryMonitorService", t)
		}
	}

	companion object {
		private const val TAG = "MainActivity"
		private const val REQUEST_NOTIFICATIONS = 100
	}
}
