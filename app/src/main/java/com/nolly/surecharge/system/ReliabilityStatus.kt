package com.nolly.surecharge.system

import android.app.ActivityManager
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.nolly.surecharge.presentation.ChecklistState

data class ReliabilityStatus(
	val batteryOptimization: ChecklistState,
	val exactAlarm: ChecklistState?,
	val backgroundActivity: ChecklistState?
)

object ReliabilityStatusProvider {
	fun fromContext(context: Context): ReliabilityStatus {
		val appContext = context.applicationContext

		val batteryState = getBatteryOptimizationState(appContext)
		val exactState = getExactAlarmState(appContext)
		val backgroundState = getBackgroundActivityState(appContext)

		return ReliabilityStatus(
			batteryOptimization = batteryState,
			exactAlarm = exactState,
			backgroundActivity = backgroundState
		)
	}

	private fun getBatteryOptimizationState(context: Context): ChecklistState {
		val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
		val ignoring = pm.isIgnoringBatteryOptimizations(context.packageName)
		return if (ignoring) ChecklistState.Ok else ChecklistState.Pending
	}

	private fun getExactAlarmState(context: Context): ChecklistState? {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			val alarmManager =
				context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
			val canExact = alarmManager.canScheduleExactAlarms()
			if (canExact) ChecklistState.Ok else ChecklistState.Pending
		} else {
			null
		}
	}

	private fun getBackgroundActivityState(context: Context): ChecklistState? {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
			val restricted = am.isBackgroundRestricted
			if (restricted) ChecklistState.Warning else ChecklistState.Ok
		} else {
			null
		}
	}
}
