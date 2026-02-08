package com.nolly.surecharge.data.profile

import com.nolly.surecharge.data.battery.BatteryRules

data class BatteryProfile(
	val id: Long,
	val name: String,
	val lowLevelEnabled: Boolean,
	val lowLevelPercentage: Int,
	val highLevelEnabled: Boolean,
	val highLevelPercentage: Int,
	val repeatIntervalMinutes: Int?,
	val isBuiltIn: Boolean
) {
	fun toRules(): BatteryRules = BatteryRules(
		lowLevelEnabled = lowLevelEnabled,
		lowLevelPercentage = lowLevelPercentage,
		highLevelEnabled = highLevelEnabled,
		highLevelPercentage = highLevelPercentage,
		repeatIntervalMinutes = repeatIntervalMinutes
	)
}

internal fun BatteryProfileEntity.toDomain(): BatteryProfile =
	BatteryProfile(
		id = id,
		name = name,
		lowLevelEnabled = lowLevelEnabled,
		lowLevelPercentage = lowLevelPercentage,
		highLevelEnabled = highLevelEnabled,
		highLevelPercentage = highLevelPercentage,
		repeatIntervalMinutes = repeatIntervalMinutes,
		isBuiltIn = builtIn
	)
