package com.nolly.surecharge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nolly.surecharge.data.AutoSettings
import com.nolly.surecharge.data.SnoozeManager
import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.data.profile.BatteryProfile
import com.nolly.surecharge.data.schedule.Schedule
import com.nolly.surecharge.presentation.HistoryUiState
import com.nolly.surecharge.presentation.ProfilesUiState
import com.nolly.surecharge.presentation.SchedulesUiState
import com.nolly.surecharge.ui.components.AutoCard
import com.nolly.surecharge.ui.components.HighAlertCard
import com.nolly.surecharge.ui.components.LowAlertCard
import com.nolly.surecharge.ui.components.ProfilesCard
import com.nolly.surecharge.ui.components.RepeatAlertsCard
import com.nolly.surecharge.ui.components.ScheduleEditorDialog
import com.nolly.surecharge.ui.components.SchedulesCard
import com.nolly.surecharge.ui.components.SnoozeCard
import com.nolly.surecharge.ui.components.createDefaultSchedule
import com.nolly.surecharge.ui.components.weekdaysMask
import com.nolly.surecharge.ui.theme.SureChargeTheme

@Composable
fun RulesScreen(
	rules: BatteryRules,
	onRulesChange: (BatteryRules) -> Unit,
	historyState: HistoryUiState,
	profilesState: ProfilesUiState,
	onCreateProfileFromRules: (String) -> Unit,
	onDeleteProfile: (Long) -> Unit,
	schedulesState: SchedulesUiState,
	onSaveSchedule: (Schedule) -> Unit,
	onDeleteSchedule: (Long) -> Unit,
	onToggleScheduleEnabled: (Long, Boolean) -> Unit,
	autoSettings: AutoSettings,
	onToggleAuto: (Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
	val context = LocalContext.current
	val snoozeManager = remember(context) { SnoozeManager.from(context) }
	var isSnoozed by remember { mutableStateOf(snoozeManager.isSnoozed()) }

	var showCreateDialog by remember { mutableStateOf(false) }
	var newProfileName by remember { mutableStateOf("") }

	var editingSchedule by remember { mutableStateOf<Schedule?>(null) }

	Column(
		modifier = modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Text(
			text = "Battery alerts that actually fire.",
			style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
		)
		Text(
			text = "SureCharge watches your battery and lets you know before it's too late or when it's time to unplug.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
		)

		AutoCard(
			historyState = historyState,
			autoSettings = autoSettings,
			onToggleAuto = onToggleAuto,
			onRulesChange = onRulesChange
		)

		ProfilesCard(
			rules = rules,
			profiles = profilesState.profiles,
			onApplyProfile = { profile ->
				onRulesChange(profile.toRules())
			},
			onCreateProfileClick = {
				newProfileName = ""
				showCreateDialog = true
			},
			onDeleteProfile = onDeleteProfile
		)

		SchedulesCard(
			schedulesState = schedulesState,
			onAddSchedule = {
				editingSchedule = createDefaultSchedule()
			},
			onEditSchedule = { schedule ->
				editingSchedule = schedule
			},
			onDeleteSchedule = onDeleteSchedule,
			onToggleScheduleEnabled = onToggleScheduleEnabled
		)

		LowAlertCard(
			rules = rules,
			onRulesChange = onRulesChange
		)

		HighAlertCard(
			rules = rules,
			onRulesChange = onRulesChange
		)

		RepeatAlertsCard(
			rules = rules,
			onRulesChange = onRulesChange
		)

		SnoozeCard(
			isSnoozed = isSnoozed,
			onToggleSnooze = {
				if (isSnoozed) {
					snoozeManager.clearSnooze()
					isSnoozed = false
				} else {
					snoozeManager.snoozeForMinutes(60)
					isSnoozed = true
				}
			}
		)

		Spacer(modifier = Modifier.height(24.dp))
	}

	if (showCreateDialog) {
		AlertDialog(
			onDismissRequest = { showCreateDialog = false },
			title = { Text("Save profile") },
			text = {
				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					Text(
						text = "Give this set of rules a name so you can switch to it in one tap."
					)
					OutlinedTextField(
						value = newProfileName,
						onValueChange = { newProfileName = it },
						singleLine = true,
						label = { Text("Profile name") }
					)
				}
			},
			confirmButton = {
				TextButton(
					onClick = {
						onCreateProfileFromRules(newProfileName)
						showCreateDialog = false
					}
				) {
					Text("Save")
				}
			},
			dismissButton = {
				TextButton(onClick = { showCreateDialog = false }) {
					Text("Cancel")
				}
			}
		)
	}

	if (editingSchedule != null) {
		ScheduleEditorDialog(
			initialSchedule = editingSchedule!!,
			profiles = profilesState.profiles,
			onDismiss = { editingSchedule = null },
			onSave = { updated ->
				onSaveSchedule(updated)
				editingSchedule = null
			}
		)
	}
}

@Preview(showBackground = true)
@Composable
private fun RulesScreenPreview() {
	SureChargeTheme {
		RulesScreen(
			rules = BatteryRules(),
			onRulesChange = {},
			historyState = HistoryUiState(),
			profilesState = ProfilesUiState(
				profiles = listOf(
					BatteryProfile(
						id = 1,
						name = "Balanced daily",
						lowLevelEnabled = true,
						lowLevelPercentage = 20,
						highLevelEnabled = true,
						highLevelPercentage = 80,
						repeatIntervalMinutes = 15,
						isBuiltIn = true
					),
					BatteryProfile(
						id = 2,
						name = "Health-first",
						lowLevelEnabled = true,
						lowLevelPercentage = 25,
						highLevelEnabled = true,
						highLevelPercentage = 75,
						repeatIntervalMinutes = 30,
						isBuiltIn = true
					)
				)
			),
			onCreateProfileFromRules = {},
			onDeleteProfile = {},
			schedulesState = SchedulesUiState(
				schedules = listOf(
					Schedule(
						id = 1,
						name = "Weeknights · Health-first",
						enabled = true,
						daysMask = weekdaysMask(),
						startMinutes = 22 * 60,
						endMinutes = 7 * 60,
						useProfileId = 2,
						overrideLowEnabled = null,
						overrideLowPercent = null,
						overrideHighEnabled = null,
						overrideHighPercent = null,
						overrideRepeatMinutes = null,
						priority = 1
					)
				)
			),
			onSaveSchedule = {},
			onDeleteSchedule = {},
			onToggleScheduleEnabled = { _, _ -> },
			autoSettings = AutoSettings(),
			onToggleAuto = {}
		)
	}
}
