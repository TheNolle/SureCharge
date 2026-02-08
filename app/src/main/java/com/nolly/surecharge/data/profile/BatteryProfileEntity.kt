package com.nolly.surecharge.data.profile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_profiles")
data class BatteryProfileEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val name: String,
	val lowLevelEnabled: Boolean,
	val lowLevelPercentage: Int,
	val highLevelEnabled: Boolean,
	val highLevelPercentage: Int,
	val repeatIntervalMinutes: Int?,
	val builtIn: Boolean,
	val orderIndex: Int
)
