package com.nolly.surecharge.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.data.battery.BatteryRulesStore
import com.nolly.surecharge.data.history.ChargeSession
import com.nolly.surecharge.presentation.HistoryUiState
import com.nolly.surecharge.ui.components.BatteryHealthCard
import com.nolly.surecharge.ui.components.ChargeHistoryChart
import com.nolly.surecharge.ui.components.ChargeSessionItem
import com.nolly.surecharge.ui.components.HistoryRangeChips
import com.nolly.surecharge.ui.components.TrendsCard
import com.nolly.surecharge.ui.theme.SureChargeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
	state: HistoryUiState,
	onHistoryRangeChange: (Int) -> Unit,
	modifier: Modifier = Modifier
) {
	val context = LocalContext.current
	val rulesStore = remember(context) { BatteryRulesStore(context.applicationContext) }
	val scope = rememberCoroutineScope()

	Column(
		modifier = modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Text(
			text = "Charge history",
			style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
		)
		Text(
			text = "SureCharge learns from your real charging sessions so you can see patterns and tune alerts.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
		)

		LiveBatteryStatusHeader(
			modifier = Modifier.fillMaxWidth()
		)

		HistoryRangeChips(
			selectedDays = state.historyDays,
			onSelected = onHistoryRangeChange
		)

		if (!state.hasData) {
			Spacer(Modifier.height(12.dp))
			Text(
				text = "We'll start showing history after a few charge cycles.",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
			return
		}

		state.health?.let { healthSummary ->
			BatteryHealthCard(
				summary = healthSummary,
				modifier = Modifier.fillMaxWidth()
			)
		}

		ChargeHistoryChart(
			sessions = state.sessions,
			modifier = Modifier
				.fillMaxWidth()
				.height(180.dp)
		)

		state.averageStartLevel?.let { avgStart ->
			state.averageEndLevel?.let { avgEnd ->
				TrendsCard(
					averageStart = avgStart,
					averageEnd = avgEnd,
					onApplySuggestedRange = { suggestedLow, suggestedHigh ->
						scope.launch {
							val currentRules: BatteryRules = rulesStore.rulesFlow.first()
							val updated = currentRules.copy(
								lowLevelEnabled = true,
								lowLevelPercentage = suggestedLow,
								highLevelEnabled = true,
								highLevelPercentage = suggestedHigh
							)
							rulesStore.setRules(updated)
						}
					}
				)
			}
		}

		Spacer(Modifier.height(8.dp))

		Text(
			text = "Recent sessions",
			style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
		)

		state.sessions.forEach { session ->
			ChargeSessionItem(session = session)
		}

		Spacer(Modifier.height(24.dp))
	}
}

@Composable
private fun LiveBatteryStatusHeader(
	modifier: Modifier = Modifier
) {
	val context = LocalContext.current

	var level by remember { mutableStateOf<Int?>(null) }
	var isCharging by remember { mutableStateOf<Boolean?>(null) }

	DisposableEffect(context) {
		val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

		val receiver = object : BroadcastReceiver() {
			override fun onReceive(ctx: Context?, intent: Intent?) {
				if (intent == null) return
				val lvl = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
				val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
				val status = intent.getIntExtra(
					BatteryManager.EXTRA_STATUS,
					BatteryManager.BATTERY_STATUS_UNKNOWN
				)

				if (lvl >= 0 && scale > 0) {
					val pct = (lvl * 100f / scale.toFloat())
						.toInt()
						.coerceIn(0, 100)
					level = pct
				}

				isCharging = when (status) {
					BatteryManager.BATTERY_STATUS_CHARGING,
					BatteryManager.BATTERY_STATUS_FULL -> true

					BatteryManager.BATTERY_STATUS_DISCHARGING,
					BatteryManager.BATTERY_STATUS_NOT_CHARGING -> false

					else -> null
				}
			}
		}

		context.registerReceiver(receiver, filter)

		onDispose {
			try {
				context.unregisterReceiver(receiver)
			} catch (_: IllegalArgumentException) {
				// Receiver was not registered, ignore
			}
		}
	}

	if (level == null && isCharging == null) {
		return
	}

	Card(
		modifier = modifier,
		shape = MaterialTheme.shapes.medium,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface
		),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			Text(
				text = "Right now",
				style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)

			val levelText = level?.let { "$it%" } ?: "—"
			val statusText = when (isCharging) {
				true -> "Charging"
				false -> "Not charging"
				null -> "Status unavailable"
			}

			Text(
				text = "$levelText · $statusText",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
	SureChargeTheme {
		HistoryScreen(
			state = HistoryUiState(
				historyDays = 30,
				sessions = listOf(
					ChargeSession(
						id = 1L,
						startTimeMillis = System.currentTimeMillis() - 3 * 60 * 60 * 1000L,
						endTimeMillis = System.currentTimeMillis() - 2 * 60 * 60 * 1000L,
						startLevel = 20,
						endLevel = 80
					),
					ChargeSession(
						id = 2L,
						startTimeMillis = System.currentTimeMillis() - 24 * 60 * 60 * 1000L,
						endTimeMillis = System.currentTimeMillis() - 23 * 60 * 60 * 1000L,
						startLevel = 10,
						endLevel = 95
					)
				)
			),
			onHistoryRangeChange = {}
		)
	}
}
