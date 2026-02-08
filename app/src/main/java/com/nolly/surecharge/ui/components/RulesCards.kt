package com.nolly.surecharge.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nolly.surecharge.data.battery.BatteryRules

@Composable
fun LowAlertCard(
	rules: BatteryRules,
	onRulesChange: (BatteryRules) -> Unit
) {
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
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Text(
				text = "Low battery alert",
				style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
			)
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "${rules.lowLevelPercentage}%",
					style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
				)
				Switch(
					checked = rules.lowLevelEnabled,
					onCheckedChange = { enabled ->
						onRulesChange(rules.copy(lowLevelEnabled = enabled))
					}
				)
			}
			Slider(
				value = rules.lowLevelPercentage.toFloat(),
				onValueChange = { value ->
					onRulesChange(
						rules.copy(
							lowLevelPercentage = value.toInt().coerceIn(5, 50)
						)
					)
				},
				valueRange = 5f..50f
			)
			Text(
				text = "You'll get a one-time alert when your battery falls below this level in a discharge cycle.",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
		}
	}
}

@Composable
fun HighAlertCard(
	rules: BatteryRules,
	onRulesChange: (BatteryRules) -> Unit
) {
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
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Text(
				text = "Charge limit alert",
				style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
			)
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "${rules.highLevelPercentage}%",
					style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
				)
				Switch(
					checked = rules.highLevelEnabled,
					onCheckedChange = { enabled ->
						onRulesChange(rules.copy(highLevelEnabled = enabled))
					}
				)
			}
			Slider(
				value = rules.highLevelPercentage.toFloat(),
				onValueChange = { value ->
					onRulesChange(
						rules.copy(
							highLevelPercentage = value.toInt().coerceIn(60, 100)
						)
					)
				},
				valueRange = 60f..100f
			)
			Text(
				text = "You'll get a one-time alert when your phone reaches this level while charging.",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
		}
	}
}

@Composable
fun RepeatAlertsCard(
	rules: BatteryRules,
	onRulesChange: (BatteryRules) -> Unit
) {
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
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				text = "Repeat alerts (optional)",
				style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
			)

			val repeatEnabled = rules.repeatIntervalMinutes != null

			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = if (repeatEnabled) {
						"Every ${rules.repeatIntervalMinutes} minutes"
					} else {
						"Off"
					},
					style = MaterialTheme.typography.bodyMedium
				)
				Switch(
					checked = repeatEnabled,
					onCheckedChange = { enabled ->
						onRulesChange(
							rules.copy(
								repeatIntervalMinutes = if (enabled) 10 else null
							)
						)
					}
				)
			}

			if (repeatEnabled) {
				Slider(
					value = rules.repeatIntervalMinutes.toFloat(),
					onValueChange = { value ->
						onRulesChange(
							rules.copy(
								repeatIntervalMinutes = value.toInt().coerceIn(5, 60)
							)
						)
					},
					valueRange = 5f..60f
				)
				Text(
					text = "We'll repeat the alert at this interval until you plug in / unplug.",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)
			}
		}
	}
}

@Composable
fun SnoozeCard(
	isSnoozed: Boolean,
	onToggleSnooze: () -> Unit
) {
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
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				text = "Pause alerts",
				style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
			)
			Text(
				text = if (isSnoozed) {
					"Alerts are paused. You won't receive low or high battery alerts until snooze ends."
				} else {
					"Temporarily pause low and high battery alerts if you don't want to be interrupted."
				},
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
			Button(
				onClick = onToggleSnooze
			) {
				Text(if (isSnoozed) "Resume alerts" else "Snooze for 1 hour")
			}
		}
	}
}
