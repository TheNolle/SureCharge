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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nolly.surecharge.data.AutoSettings
import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.presentation.HistoryUiState
import com.nolly.surecharge.system.AutoRules

@Composable
fun AutoCard(
	historyState: HistoryUiState,
	autoSettings: AutoSettings,
	onToggleAuto: (Boolean) -> Unit,
	onRulesChange: (BatteryRules) -> Unit,
) {
	val hasStats =
		historyState.hasData && historyState.averageStartLevel != null && historyState.averageEndLevel != null

	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.medium,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
		),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Column(
					verticalArrangement = Arrangement.spacedBy(2.dp)
				) {
					Text(
						text = "Auto mode",
						style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
					)
					Text(
						text = if (autoSettings.enabled) {
							"Tuning alerts from your recent charge history."
						} else {
							"Off · Alerts stay exactly where you set them."
						},
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
					)
				}
				Switch(
					checked = autoSettings.enabled,
					onCheckedChange = { enabled ->
						onToggleAuto(enabled)
					}
				)
			}

			val description = if (hasStats) {
				"With auto mode on, SureCharge nudges low/high alerts around the way you actually charge, " +
						"while keeping you in a battery-healthy range."
			} else {
				"SureCharge will start adapting alerts once it has seen a few full charge sessions. " +
						"Until then, it uses a balanced default."
			}

			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
			)

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.End
			) {
				TextButton(
					onClick = {
						val autoRules = AutoRules.fromHistory(historyState)
						onRulesChange(autoRules)
					}
				) {
					Text("Apply once")
				}
				if (autoSettings.enabled) {
					Button(
						onClick = {
							val autoRules = AutoRules.fromHistory(historyState)
							onRulesChange(autoRules)
						}
					) {
						Text("Tune from history now")
					}
				}
			}
		}
	}
}
