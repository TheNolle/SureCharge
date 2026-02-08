package com.nolly.surecharge.data.schedule

import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.data.profile.BatteryProfile

data class EffectiveRulesResult(
	val rules: BatteryRules,
	val activeSchedule: Schedule?
)

object ScheduleResolver {
	fun resolve(
		baseRules: BatteryRules,
		schedules: List<Schedule>,
		profiles: List<BatteryProfile>,
		nowMillis: Long
	): EffectiveRulesResult {
		val matching = schedules.filter { it.isActiveAt(nowMillis) }
		if (matching.isEmpty()) {
			return EffectiveRulesResult(
				rules = baseRules,
				activeSchedule = null
			)
		}

		val activeSchedule = selectHighestPriority(matching)

		val rules = if (activeSchedule.useProfileId != null) {
			val profile = profiles.firstOrNull { it.id == activeSchedule.useProfileId }
			profile?.toRules() ?: activeSchedule.applyOverrides(baseRules)
		} else {
			activeSchedule.applyOverrides(baseRules)
		}

		return EffectiveRulesResult(
			rules = rules,
			activeSchedule = activeSchedule
		)
	}

	private fun selectHighestPriority(schedules: List<Schedule>): Schedule {
		return schedules
			.sortedWith(
				compareByDescending<Schedule> { it.priority }
					.thenBy { it.id }
			)
			.first()
	}
}
