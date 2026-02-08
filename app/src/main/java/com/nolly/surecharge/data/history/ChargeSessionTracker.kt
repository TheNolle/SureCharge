package com.nolly.surecharge.data.history

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ChargeSessionTracker(
	context: Context,
	private val repository: ChargeHistoryRepository,
	private val scope: CoroutineScope
) {
	private val prefs: SharedPreferences =
		context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

	private var activeStartTime: Long
		get() = prefs.getLong(KEY_START_TIME, -1L)
		set(value) = prefs.edit { putLong(KEY_START_TIME, value) }

	private var activeStartLevel: Int
		get() = prefs.getInt(KEY_START_LEVEL, -1)
		set(value) = prefs.edit { putInt(KEY_START_LEVEL, value) }

	private var isActive: Boolean
		get() = prefs.getBoolean(KEY_ACTIVE, false)
		set(value) = prefs.edit { putBoolean(KEY_ACTIVE, value) }

	fun onBatteryUpdate(levelPercent: Int, isCharging: Boolean) {
		val now = System.currentTimeMillis()

		if (isCharging) {
			if (!isActive) {
				activeStartTime = now
				activeStartLevel = levelPercent
				isActive = true
			}
		} else {
			if (isActive) {
				val startTime = activeStartTime
				val startLevel = activeStartLevel

				if (startTime > 0 && startLevel in 0..100 && levelPercent in 0..100) {
					scope.launch {
						repository.addSession(
							startTimeMillis = startTime,
							endTimeMillis = now,
							startLevel = startLevel,
							endLevel = levelPercent
						)
					}
				}
				isActive = false
				activeStartTime = -1L
				activeStartLevel = -1
			}
		}
	}

	companion object {
		private const val PREFS_NAME = "surecharge_history_tracker"
		private const val KEY_ACTIVE = "active"
		private const val KEY_START_TIME = "start_time"
		private const val KEY_START_LEVEL = "start_level"
	}
}
