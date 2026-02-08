package com.nolly.surecharge.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nolly.surecharge.data.AutoSettings
import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.presentation.BatteryProfilesViewModel
import com.nolly.surecharge.presentation.HistoryUiState
import com.nolly.surecharge.presentation.HistoryViewModel
import com.nolly.surecharge.presentation.ProfilesUiState
import com.nolly.surecharge.presentation.SchedulesUiState
import com.nolly.surecharge.presentation.SchedulesViewModel
import com.nolly.surecharge.presentation.SureChargeViewModel
import com.nolly.surecharge.system.ReliabilityStatus
import com.nolly.surecharge.system.ReliabilityStatusProvider
import com.nolly.surecharge.ui.screens.HistoryScreen
import com.nolly.surecharge.ui.screens.InfoScreen
import com.nolly.surecharge.ui.screens.ReliabilityScreen
import com.nolly.surecharge.ui.screens.RulesScreen
import com.nolly.surecharge.ui.screens.SettingsScreen
import com.nolly.surecharge.ui.theme.SureChargeTheme

@Composable
fun SureChargeApp(
	rulesViewModel: SureChargeViewModel,
	historyViewModel: HistoryViewModel,
	profilesViewModel: BatteryProfilesViewModel,
	schedulesViewModel: SchedulesViewModel
) {
	val rules by rulesViewModel.rules.collectAsStateWithLifecycle()
	val autoSettings by rulesViewModel.autoSettings.collectAsStateWithLifecycle()
	val historyState by historyViewModel.uiState.collectAsStateWithLifecycle()
	val profilesState by profilesViewModel.state.collectAsStateWithLifecycle()
	val schedulesState by schedulesViewModel.state.collectAsStateWithLifecycle()

	var showSettings by remember { mutableStateOf(false) }
	var showInfo by remember { mutableStateOf(false) }

	SureChargeTheme {
		Surface(
			modifier = Modifier.fillMaxSize(),
			color = MaterialTheme.colorScheme.background
		) {
			Box(modifier = Modifier.fillMaxSize()) {
				SureChargeHome(
					rules = rules,
					onRulesChange = { updated -> rulesViewModel.setRules(updated) },
					autoSettings = autoSettings,
					onToggleAuto = { enabled -> rulesViewModel.setAutoEnabled(enabled) },
					historyState = historyState,
					onHistoryRangeChange = { days -> historyViewModel.updateHistoryDays(days) },
					profilesState = profilesState,
					onCreateProfileFromRules = { name ->
						profilesViewModel.createProfileFromRules(name, rules)
					},
					onDeleteProfile = { id ->
						profilesViewModel.deleteProfile(id)
					},
					schedulesState = schedulesState,
					onSaveSchedule = { schedule ->
						schedulesViewModel.saveSchedule(schedule)
					},
					onDeleteSchedule = { id ->
						schedulesViewModel.deleteSchedule(id)
					},
					onToggleScheduleEnabled = { id, enabled ->
						schedulesViewModel.setScheduleEnabled(id, enabled)
					},
					onInfoClick = { showInfo = true },
					onSettingsClick = { showSettings = true }
				)

				if (showSettings) {
					SettingsScreen(
						autoSettings = autoSettings,
						historyDays = historyState.historyDays,
						onHistoryDaysChange = { days -> historyViewModel.updateHistoryDays(days) },
						onToggleAuto = { enabled -> rulesViewModel.setAutoEnabled(enabled) },
						onClose = { showSettings = false }
					)
				}

				if (showInfo) {
					InfoScreen(
						onClose = { showInfo = false }
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SureChargeHome(
	rules: BatteryRules,
	onRulesChange: (BatteryRules) -> Unit,
	autoSettings: AutoSettings,
	onToggleAuto: (Boolean) -> Unit,
	historyState: HistoryUiState,
	onHistoryRangeChange: (Int) -> Unit,
	profilesState: ProfilesUiState,
	onCreateProfileFromRules: (String) -> Unit,
	onDeleteProfile: (Long) -> Unit,
	schedulesState: SchedulesUiState,
	onSaveSchedule: (com.nolly.surecharge.data.schedule.Schedule) -> Unit,
	onDeleteSchedule: (Long) -> Unit,
	onToggleScheduleEnabled: (Long, Boolean) -> Unit,
	onInfoClick: () -> Unit,
	onSettingsClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	var selectedTab by remember { mutableIntStateOf(0) }
	val context = LocalContext.current
	val reliabilityStatus: ReliabilityStatus = remember(context) {
		ReliabilityStatusProvider.fromContext(context)
	}

	Column(
		modifier = modifier
			.fillMaxSize()
			.systemBarsPadding()
	) {
		TopAppBar(
			title = {
				Text(
					text = "SureCharge",
					style = MaterialTheme.typography.titleLarge.copy(
						fontWeight = FontWeight.SemiBold
					)
				)
			},
			actions = {
				TextButton(onClick = onInfoClick) {
					Text("Info")
				}
				TextButton(onClick = onSettingsClick) {
					Text("Settings")
				}
			}
		)

		SecondaryTabRow(
			selectedTabIndex = selectedTab
		) {
			Tab(
				selected = selectedTab == 0,
				onClick = { selectedTab = 0 },
				text = { Text("Battery rules") }
			)
			Tab(
				selected = selectedTab == 1,
				onClick = { selectedTab = 1 },
				text = { Text("Reliability") }
			)
			Tab(
				selected = selectedTab == 2,
				onClick = { selectedTab = 2 },
				text = { Text("History") }
			)
		}

		Spacer(modifier = Modifier.height(16.dp))

		when (selectedTab) {
			0 -> RulesScreen(
				rules = rules,
				onRulesChange = onRulesChange,
				historyState = historyState,
				profilesState = profilesState,
				onCreateProfileFromRules = onCreateProfileFromRules,
				onDeleteProfile = onDeleteProfile,
				schedulesState = schedulesState,
				onSaveSchedule = onSaveSchedule,
				onDeleteSchedule = onDeleteSchedule,
				onToggleScheduleEnabled = onToggleScheduleEnabled,
				autoSettings = autoSettings,
				onToggleAuto = onToggleAuto,
				modifier = Modifier.weight(1f)
			)

			1 -> ReliabilityScreen(
				status = reliabilityStatus,
				modifier = Modifier.weight(1f),
				onBatteryOptimizationClick = {
					val intent = Intent(
						Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
						"package:${context.packageName}".toUri()
					)
					context.startActivity(intent)
				},
				onExactAlarmClick = {
					val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
						putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
					}
					context.startActivity(intent)
				},
				onBackgroundActivityClick = {
					val intent = Intent(
						Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
						"package:${context.packageName}".toUri()
					)
					context.startActivity(intent)
				}
			)

			2 -> HistoryScreen(
				state = historyState,
				onHistoryRangeChange = onHistoryRangeChange,
				modifier = Modifier.weight(1f)
			)
		}
	}
}
