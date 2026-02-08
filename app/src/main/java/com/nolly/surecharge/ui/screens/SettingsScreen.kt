package com.nolly.surecharge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nolly.surecharge.data.AutoSettings
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	autoSettings: AutoSettings,
	historyDays: Int,
	onHistoryDaysChange: (Int) -> Unit,
	onToggleAuto: (Boolean) -> Unit,
	onClose: () -> Unit,
	modifier: Modifier = Modifier
) {
	var localHistoryDays by remember(historyDays) {
		mutableIntStateOf(historyDays)
	}

	Surface(
		modifier = modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
	) {
		Column {
			TopAppBar(
				title = {
					Text(
						text = "Settings",
						style = MaterialTheme.typography.titleLarge.copy(
							fontWeight = FontWeight.SemiBold
						)
					)
				},
				navigationIcon = {
					TextButton(onClick = onClose) {
						Text("Back")
					}
				}
			)

			Column(
				modifier = Modifier
					.weight(1f)
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp, vertical = 8.dp)
			) {
				SettingsSectionTitle("Automatic tuning")

				SettingsToggleRow(
					title = "Learn from my habits",
					description = "Use your current rules as a baseline and let SureCharge refine them over time.",
					checked = autoSettings.enabled,
					onCheckedChange = { enabled ->
						onToggleAuto(enabled)
					}
				)

				HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

				SettingsSectionTitle("History & statistics")

				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 4.dp)
				) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							text = "History range",
							style = MaterialTheme.typography.bodyLarge
						)
						Text(
							text = "${localHistoryDays} days",
							style = MaterialTheme.typography.bodyMedium
						)
					}
					Slider(
						value = localHistoryDays.toFloat(),
						onValueChange = { newValue ->
							localHistoryDays = newValue.roundToInt().coerceIn(7, 90)
						},
						onValueChangeFinished = {
							onHistoryDaysChange(localHistoryDays)
						},
						valueRange = 7f..90f,
						steps = 83,
						modifier = Modifier.fillMaxWidth(),
						colors = SliderDefaults.colors()
					)
					Text(
						text = "Controls how many days of past charging sessions are included in the overview.",
						style = MaterialTheme.typography.bodySmall
					)
				}

				HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

				SettingsSectionTitle("Privacy")

				Text(
					text = "SureCharge never collects analytics, never sends your data to a server, and never reads your personal content. All charging history and statistics stay locally on your device.",
					style = MaterialTheme.typography.bodyMedium,
					modifier = Modifier.padding(vertical = 4.dp)
				)

				Spacer(modifier = Modifier.padding(bottom = 16.dp))
			}
		}
	}
}

@Composable
private fun SettingsSectionTitle(text: String) {
	Text(
		text = text,
		style = MaterialTheme.typography.titleMedium.copy(
			fontWeight = FontWeight.SemiBold
		),
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 8.dp)
	)
}

@Composable
private fun SettingsToggleRow(
	title: String,
	description: String?,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier.fillMaxWidth()
		) {
			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.bodyLarge
				)
				if (description != null) {
					Text(
						text = description,
						style = MaterialTheme.typography.bodySmall,
						modifier = Modifier.padding(top = 2.dp)
					)
				}
			}
			Spacer(modifier = Modifier.padding(horizontal = 8.dp))
			Switch(
				checked = checked,
				onCheckedChange = onCheckedChange
			)
		}
	}
}
