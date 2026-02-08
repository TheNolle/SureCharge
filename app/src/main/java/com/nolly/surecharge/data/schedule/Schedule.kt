package com.nolly.surecharge.data.schedule

import com.nolly.surecharge.data.battery.BatteryRules
import java.util.Calendar
import java.util.TimeZone

data class Schedule(
	val id: Long = 0,
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
) {
	val isAllDay: Boolean get() = startMinutes == endMinutes
}

internal fun ScheduleEntity.toDomain(): Schedule =
	Schedule(
		id = id,
		name = name,
		enabled = enabled,
		daysMask = daysMask,
		startMinutes = startMinutes,
		endMinutes = endMinutes,
		useProfileId = useProfileId,
		overrideLowEnabled = overrideLowEnabled,
		overrideLowPercent = overrideLowPercent,
		overrideHighEnabled = overrideHighEnabled,
		overrideHighPercent = overrideHighPercent,
		overrideRepeatMinutes = overrideRepeatMinutes,
		priority = priority
	)

internal fun Schedule.toEntity(): ScheduleEntity =
	ScheduleEntity(
		id = id,
		name = name.ifBlank { autoName() },
		enabled = enabled,
		daysMask = daysMask,
		startMinutes = startMinutes,
		endMinutes = endMinutes,
		useProfileId = useProfileId,
		overrideLowEnabled = overrideLowEnabled,
		overrideLowPercent = overrideLowPercent,
		overrideHighEnabled = overrideHighEnabled,
		overrideHighPercent = overrideHighPercent,
		overrideRepeatMinutes = overrideRepeatMinutes,
		priority = priority
	)

private fun Schedule.autoName(): String {
	return if (useProfileId != null) {
		"Profile schedule"
	} else {
		"Custom schedule"
	}
}

fun Schedule.isActiveAt(
	nowMillis: Long,
	timeZone: TimeZone = TimeZone.getDefault()
): Boolean {
	if (!enabled) return false

	val cal = Calendar.getInstance(timeZone).apply {
		timeInMillis = nowMillis
	}

	val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
	val bitIndex = (dayOfWeek - 1).coerceIn(0, 6)
	val dayBit = 1 shl bitIndex
	if ((daysMask and dayBit) == 0) return false

	val minutes =
		cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

	if (isAllDay) {
		return true
	}

	val start = startMinutes
	val end = endMinutes

	return if (start < end) {
		minutes in start until end
	} else {
		minutes !in end..<start
	}
}

fun Schedule.applyOverrides(base: BatteryRules): BatteryRules {
	var result = base
	overrideLowEnabled?.let { result = result.copy(lowLevelEnabled = it) }
	overrideLowPercent?.let { result = result.copy(lowLevelPercentage = it) }
	overrideHighEnabled?.let { result = result.copy(highLevelEnabled = it) }
	overrideHighPercent?.let { result = result.copy(highLevelPercentage = it) }
	overrideRepeatMinutes?.let { result = result.copy(repeatIntervalMinutes = it) }
	return result
}
