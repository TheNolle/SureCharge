package com.nolly.surecharge.system

import com.nolly.surecharge.data.history.ChargeSession
import kotlin.math.min
import kotlin.math.roundToInt

data class BatteryHealthSummary(
	val score: Int,
	val verdictLabel: String,
	val fullChargeCount: Int,
	val deepDischargeCount: Int,
	val approxCycles: Double,
	val ageDays: Int?,
	val averageChargeSpeedPercentPerHour: Double?,
	val hasSlowCharging: Boolean,
	val suggestions: List<String>
)

object BatteryHealthAnalyzer {
	fun fromSessions(
		sessions: List<ChargeSession>,
		nowMillis: Long = System.currentTimeMillis()
	): BatteryHealthSummary? {
		if (sessions.isEmpty()) return null

		var fullChargeCount = 0
		var deepDischargeCount = 0
		var totalDeltaPercent = 0.0
		var firstTimestamp = Long.MAX_VALUE

		var speedSamples = 0
		var speedSum = 0.0

		for (session in sessions) {
			val start = session.startLevel.coerceIn(0, 100)
			val end = session.endLevel.coerceIn(0, 100)
			val delta = (end - start).coerceAtLeast(0)

			totalDeltaPercent += delta

			if (end >= 95) {
				fullChargeCount++
			}
			if (start <= 10) {
				deepDischargeCount++
			}

			if (session.startTimeMillis < firstTimestamp) {
				firstTimestamp = session.startTimeMillis
			}

			val durationMillis = session.endTimeMillis - session.startTimeMillis
			if (delta > 0 && durationMillis > 15 * 60_000L) {
				val hours = durationMillis / 3_600_000.0
				if (hours > 0.0) {
					val speed = delta / hours
					speedSum += speed
					speedSamples++
				}
			}
		}

		val approxCycles = totalDeltaPercent / 100.0

		val ageDays = if (firstTimestamp != Long.MAX_VALUE) {
			((nowMillis - firstTimestamp) / 86_400_000.0)
				.toInt()
				.coerceAtLeast(0)
		} else {
			null
		}

		val ratioFull = fullChargeCount.toDouble() / sessions.size.toDouble()
		val ratioDeep = deepDischargeCount.toDouble() / sessions.size.toDouble()

		val avgSpeed: Double? = if (speedSamples > 0) {
			speedSum / speedSamples.toDouble()
		} else {
			null
		}

		var score = 100.0

		val penaltyFull = min(30.0, ratioFull * 40.0)

		val penaltyDeep = min(25.0, ratioDeep * 35.0)

		val penaltyCycles = min(25.0, approxCycles * 0.08)

		val penaltyAge = ageDays?.let { days ->
			val years = days / 365.0
			min(15.0, years * 10.0)
		} ?: 0.0

		val penaltySlow = if (avgSpeed != null && avgSpeed < 10.0) 5.0 else 0.0

		score -= (penaltyFull + penaltyDeep + penaltyCycles + penaltyAge + penaltySlow)
		val finalScore = score.roundToInt().coerceIn(0, 100)

		val verdictLabel = when {
			finalScore >= 85 -> "Great"
			finalScore >= 70 -> "Good"
			finalScore >= 55 -> "Worn"
			else -> "Degraded"
		}

		val suggestions = mutableListOf<String>()

		if (ratioFull > 0.30) {
			suggestions += "Try unplugging closer to 80–90% instead of charging to 100% almost every time."
		}
		if (ratioDeep > 0.20) {
			suggestions += "Avoid letting the battery drop below 10% regularly; plugging in a bit earlier is easier on the battery."
		}
		if (approxCycles > 300.0) {
			suggestions += "Your history suggests several hundred full charge cycles. A moderate capacity loss is normal at this point."
		}
		if (avgSpeed != null && avgSpeed < 10.0) {
			suggestions += "Charging often seems quite slow. If you're using an older cable or charger, trying a different charger/cable may help."
		}
		if (suggestions.isEmpty()) {
			suggestions += "Your habits already look quite battery-friendly. Keeping most cycles between roughly 20% and 80% will help in the long run."
		}

		return BatteryHealthSummary(
			score = finalScore,
			verdictLabel = verdictLabel,
			fullChargeCount = fullChargeCount,
			deepDischargeCount = deepDischargeCount,
			approxCycles = approxCycles,
			ageDays = ageDays,
			averageChargeSpeedPercentPerHour = avgSpeed,
			hasSlowCharging = avgSpeed != null && avgSpeed < 10.0,
			suggestions = suggestions
		)
	}
}
