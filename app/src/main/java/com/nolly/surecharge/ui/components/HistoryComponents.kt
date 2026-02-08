package com.nolly.surecharge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nolly.surecharge.data.history.ChargeSession
import com.nolly.surecharge.system.BatteryHealthSummary
import java.text.DateFormat
import java.util.Date
import kotlin.math.max
import kotlin.math.min

@Composable
fun HistoryRangeChips(
	selectedDays: Int,
	onSelected: (Int) -> Unit
) {
	val options = listOf(7, 30, 90)
	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		options.forEach { days ->
			val selected = days == selectedDays
			AssistChip(
				onClick = { onSelected(days) },
				label = { Text("${days}d") },
				colors = AssistChipDefaults.assistChipColors(
					containerColor = if (selected) {
						MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
					} else {
						MaterialTheme.colorScheme.surface
					},
					labelColor = if (selected) {
						MaterialTheme.colorScheme.primary
					} else {
						MaterialTheme.colorScheme.onSurface
					}
				)
			)
		}
	}
}

@Composable
fun ChargeHistoryChart(
	sessions: List<ChargeSession>,
	modifier: Modifier = Modifier
) {
	if (sessions.isEmpty()) return

	val cardContainerColor = MaterialTheme.colorScheme.surface
	val primaryColor = MaterialTheme.colorScheme.primary
	val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

	Card(
		modifier = modifier,
		shape = MaterialTheme.shapes.medium,
		colors = CardDefaults.cardColors(
			containerColor = cardContainerColor
		),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = "Charge levels over time",
				style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
			)
			Text(
				text = "Each point is where you unplugged after a charge session.",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)

			Spacer(Modifier.height(8.dp))

			Canvas(
				modifier = Modifier
					.fillMaxWidth()
					.height(120.dp)
			) {
				val sorted = sessions.sortedBy { it.endTimeMillis }
				if (sorted.size < 2) return@Canvas

				val minX = sorted.first().endTimeMillis.toFloat()
				val maxX = sorted.last().endTimeMillis.toFloat()
				val xRange = (maxX - minX).coerceAtLeast(1f)

				val yMin = 0f
				val yMax = 100f
				val yRange = (yMax - yMin).coerceAtLeast(1f)

				for (level in listOf(20, 40, 60, 80)) {
					val y = size.height * (1f - (level.toFloat() - yMin) / yRange)
					drawLine(
						color = lineColor,
						start = Offset(0f, y),
						end = Offset(size.width, y),
						strokeWidth = 1f
					)
				}

				val path = Path()
				sorted.forEachIndexed { index, session ->
					val x = ((session.endTimeMillis.toFloat() - minX) / xRange) * size.width
					val y =
						size.height * (1f - (session.endLevel.toFloat() - yMin) / yRange)

					if (index == 0) {
						path.moveTo(x, y)
					} else {
						path.lineTo(x, y)
					}
				}

				drawPath(
					path = path,
					color = primaryColor,
					style = Stroke(width = 4f)
				)
			}
		}
	}
}

@Composable
fun BatteryHealthCard(
	summary: BatteryHealthSummary,
	modifier: Modifier = Modifier
) {
	Card(
		modifier = modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.medium,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface
		),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = "Battery health (estimate)",
				style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
			)

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = "${summary.score}",
					style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold)
				)
				Text(
					text = summary.verdictLabel,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
				)
			}

			val cyclesText = "Approx. full cycles: ${summary.approxCycles.toInt()}"
			val highCountText = "Very high charges (≥ 95%): ${summary.fullChargeCount}"
			val deepCountText = "Deep discharges (≤ 10%): ${summary.deepDischargeCount}"

			Text(
				text = cyclesText,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
			)
			Text(
				text = "$highCountText · $deepCountText",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
			)

			summary.averageChargeSpeedPercentPerHour?.let { speed ->
				Text(
					text = buildString {
						append("Typical charge speed: ${speed.toInt()}% per hour")
						if (summary.hasSlowCharging) {
							append(" (on the slower side for many phones).")
						} else {
							append(".")
						}
					},
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
				)
			}

			summary.ageDays?.let { days ->
				if (days >= 1) {
					Text(
						text = "Based on about $days days of history.",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
					)
				}
			}

			if (summary.suggestions.isNotEmpty()) {
				Spacer(Modifier.height(4.dp))
				Text(
					text = "Suggestions",
					style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
				)
				summary.suggestions.forEach { tip ->
					Text(
						text = "• $tip",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
					)
				}
			}
		}
	}
}

@Composable
fun TrendsCard(
	averageStart: Int,
	averageEnd: Int,
	onApplySuggestedRange: (low: Int, high: Int) -> Unit
) {
	val suggestedLow = max(15, min(averageStart - 5, 40))
	val suggestedHigh = max(70, min(averageEnd, 85))

	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.medium,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface
		),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = "Charging habits",
				style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
			)

			Text(
				text = "On average, you start charging around $averageStart% and unplug around $averageEnd%.",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)

			HorizontalDivider()

			Text(
				text = "A health-friendly range for alerts based on this would be about $suggestedLow% (low) and $suggestedHigh% (high).",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
			)

			Button(
				onClick = { onApplySuggestedRange(suggestedLow, suggestedHigh) }
			) {
				Text("Apply suggested alert range")
			}
		}
	}
}

@Composable
fun ChargeSessionItem(
	session: ChargeSession,
	modifier: Modifier = Modifier
) {
	val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
	val date = Date(session.endTimeMillis)
	val dateLabel = df.format(date)

	Card(
		modifier = modifier
			.fillMaxWidth(),
		shape = MaterialTheme.shapes.medium,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface
		),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
	) {
		Column(
			modifier = Modifier.padding(12.dp),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			Text(
				text = dateLabel,
				style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
			)
			Text(
				text = "From ${session.startLevel}% to ${session.endLevel}%",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
			)
		}
	}
}
