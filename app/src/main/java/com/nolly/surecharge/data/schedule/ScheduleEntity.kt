package com.nolly.surecharge.data.schedule

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val name: String,
	val enabled: Boolean,
	val daysMask: Int,
	val startMinutes: Int,
	val endMinutes: Int,
	val useProfileId: Long?,
	val overrideLowEnabled: Boolean?,
	val overrideLowPercent: Int?,
	val overrideHighEnabled: Boolean?,
	val overrideHighPercent: Int?,
	val overrideRepeatMinutes: Int?,
	val priority: Int
)
