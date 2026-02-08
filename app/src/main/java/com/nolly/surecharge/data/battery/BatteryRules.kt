package com.nolly.surecharge.data.battery

data class BatteryRules(
	val lowLevelEnabled: Boolean = true,
	val lowLevelPercentage: Int = 15,
	val highLevelEnabled: Boolean = true,
	val highLevelPercentage: Int = 80,
	val repeatIntervalMinutes: Int? = null
)
