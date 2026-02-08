package com.nolly.surecharge.system

import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.presentation.HistoryUiState
import kotlin.math.max
import kotlin.math.min

object AutoRules {
	fun fromHistory(history: HistoryUiState): BatteryRules {
		val avgStart = history.averageStartLevel
		val avgEnd = history.averageEndLevel

		return if (avgStart != null && avgEnd != null) {
			val low = clamp(avgStart - 5, 15, 40)
			val high = clamp(avgEnd, 70, 85)

			BatteryRules(
				lowLevelEnabled = true,
				lowLevelPercentage = low,
				highLevelEnabled = true,
				highLevelPercentage = high,
				repeatIntervalMinutes = 20
			)
		} else {
			BatteryRules(
				lowLevelEnabled = true,
				lowLevelPercentage = 20,
				highLevelEnabled = true,
				highLevelPercentage = 80,
				repeatIntervalMinutes = 20
			)
		}
	}

	private fun clamp(value: Int, min: Int, max: Int): Int =
		max(min, min(max, value))
}
