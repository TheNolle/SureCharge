package com.nolly.surecharge.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.nolly.surecharge.data.device.DeviceTip
import com.nolly.surecharge.data.device.DeviceTipsCatalog
import com.nolly.surecharge.data.device.DeviceTipsStore
import com.nolly.surecharge.presentation.ChecklistState
import com.nolly.surecharge.system.ReliabilityStatus
import com.nolly.surecharge.ui.components.ReliabilityItem
import com.nolly.surecharge.ui.theme.SureChargeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ReliabilityScreen(
	status: ReliabilityStatus,
	modifier: Modifier = Modifier,
	onBatteryOptimizationClick: () -> Unit,
	onExactAlarmClick: () -> Unit,
	onBackgroundActivityClick: () -> Unit
) {
	val context = LocalContext.current

	Column(
		modifier = modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Text(
			text = "Reliability checklist",
			style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
		)
		Text(
			text = "Android can restrict background apps. SureCharge surfaces this so you always know where you stand.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
		)

		val batteryLabel = when (status.batteryOptimization) {
			ChecklistState.Ok -> "Optimized"
			else -> "Action needed"
		}
		val batteryDescription = when (status.batteryOptimization) {
			ChecklistState.Ok ->
				"SureCharge is exempt from battery optimization on this device."

			else ->
				"Disable battery optimization for SureCharge so alerts can fire on time."
		}
		ReliabilityItem(
			title = "Battery optimization",
			description = batteryDescription,
			label = batteryLabel,
			state = status.batteryOptimization,
			onClick = onBatteryOptimizationClick
		)

		val exactState = status.exactAlarm
		if (exactState != null) {
			val exactLabel = when (exactState) {
				ChecklistState.Ok -> "Allowed"
				else -> "Action needed"
			}
			val exactDescription = when (exactState) {
				ChecklistState.Ok ->
					"SureCharge can schedule exact alarms, even in deep sleep."

				else ->
					"Allow exact alarms so SureCharge can wake up reliably for alerts."
			}
			ReliabilityItem(
				title = "Exact alarms",
				description = exactDescription,
				label = exactLabel,
				state = exactState,
				onClick = onExactAlarmClick
			)
		}

		val bgState = status.backgroundActivity
		if (bgState != null) {
			val bgLabel = when (bgState) {
				ChecklistState.Ok -> "Unrestricted"
				ChecklistState.Warning -> "Restricted"
				else -> "Info"
			}
			val bgDescription = when (bgState) {
				ChecklistState.Ok ->
					"SureCharge is allowed to run in the background on this device."

				ChecklistState.Warning ->
					"This device restricts background activity. Adjust settings so SureCharge can run."

				else ->
					"Some devices may aggressively restrict background apps."
			}
			ReliabilityItem(
				title = "Background activity",
				description = bgDescription,
				label = bgLabel,
				state = bgState,
				onClick = onBackgroundActivityClick
			)
		}

		DeviceTipsSection(
			context = context,
			modifier = Modifier.fillMaxWidth()
		)

		Spacer(modifier = Modifier.height(24.dp))
	}
}

@Composable
private fun DeviceTipsSection(
	context: Context,
	modifier: Modifier = Modifier
) {
	val tipsStore = remember(context) { DeviceTipsStore(context.applicationContext) }
	val baseTips = remember { DeviceTipsCatalog.tipsForCurrentDevice() }

	var dismissedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
	var hasLoadedDismissed by remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()

	LaunchedEffect(tipsStore) {
		val initial = tipsStore.dismissedTipsFlow.first()
		dismissedIds = initial
		hasLoadedDismissed = true
	}

	val visibleTips: List<DeviceTip> = remember(baseTips, dismissedIds) {
		baseTips
			.filterNot { dismissedIds.contains(it.id) }
			.take(3)
	}

	if (!hasLoadedDismissed || visibleTips.isEmpty()) {
		return
	}

	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Text(
			text = "This device",
			style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
		)
		Text(
			text = "Some manufacturers are more aggressive than others with background apps. " +
					"These tips are tailored to how your phone usually behaves.",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
		)

		visibleTips.forEach { tip ->
			DeviceTipCard(
				tip = tip,
				onDismiss = {
					scope.launch {
						tipsStore.dismissTip(tip.id)
						dismissedIds = dismissedIds + tip.id
					}
				},
				onLearnMore = {
					val url = tip.learnMoreUrl
					if (!url.isNullOrBlank()) {
						val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
							addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
						}
						context.startActivity(intent)
					}
				}
			)
		}

		val firstWithUrl = visibleTips.firstOrNull { !it.learnMoreUrl.isNullOrBlank() }
		if (firstWithUrl != null) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.End
			) {
				TextButton(
					onClick = {
						val intent = Intent(
							Intent.ACTION_VIEW,
							firstWithUrl.learnMoreUrl!!.toUri()
						).apply {
							addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
						}
						context.startActivity(intent)
					}
				) {
					Text("Learn more about this OEM")
				}
			}
		}
	}
}

@Composable
private fun DeviceTipCard(
	tip: DeviceTip,
	onDismiss: () -> Unit,
	onLearnMore: () -> Unit,
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
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = tip.title,
				style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
			)
			Text(
				text = tip.description,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
			)

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.End
			) {
				TextButton(onClick = onDismiss) {
					Text("Got it")
				}
				if (!tip.learnMoreUrl.isNullOrBlank()) {
					Spacer(Modifier.width(8.dp))
					Button(onClick = onLearnMore) {
						Text("Learn more")
					}
				}
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
private fun ReliabilityScreenPreview() {
	SureChargeTheme {
		ReliabilityScreen(
			status = ReliabilityStatus(
				batteryOptimization = ChecklistState.Ok,
				exactAlarm = ChecklistState.Pending,
				backgroundActivity = ChecklistState.Warning
			),
			onBatteryOptimizationClick = {},
			onExactAlarmClick = {},
			onBackgroundActivityClick = {}
		)
	}
}
