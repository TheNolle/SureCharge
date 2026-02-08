package com.nolly.surecharge.system

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nolly.surecharge.data.AutoSettings
import com.nolly.surecharge.data.AutoSettingsStore
import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.data.battery.BatteryRulesStore
import com.nolly.surecharge.data.history.ChargeHistoryRepository
import com.nolly.surecharge.presentation.HistoryUiState
import com.nolly.surecharge.util.AppLogger
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class AutoTuneWorker(
	appContext: Context,
	params: WorkerParameters
) : CoroutineWorker(appContext, params) {
	override suspend fun doWork(): Result {
		AppLogger.i(TAG, "AutoTuneWorker: doWork() started")

		val appContext = applicationContext

		val autoStore = AutoSettingsStore(appContext)
		val settings: AutoSettings = autoStore.settingsFlow.first()
		AppLogger.d(TAG, "Auto settings: enabled=${settings.enabled}, baselineLow=${settings.baselineLow}, " +
				"baselineHigh=${settings.baselineHigh}, baselineRepeat=${settings.baselineRepeatMinutes}")

		if (!settings.enabled) {
			AppLogger.i(TAG, "Auto mode disabled; skipping tuning")
			return Result.success()
		}

		val historyRepo = ChargeHistoryRepository(appContext)
		val sessions = historyRepo.sessionsForLastDays(30).first()
		AppLogger.d(TAG, "Loaded sessions for auto-tune: count=${sessions.size}")

		if (sessions.isEmpty()) {
			AppLogger.i(TAG, "No history sessions; nothing to tune")
			return Result.success()
		}

		val startLevels = sessions.map { it.startLevel }
		val endLevels = sessions.map { it.endLevel }

		if (startLevels.isEmpty() || endLevels.isEmpty()) {
			AppLogger.w(TAG, "Sessions have no start/end levels; aborting tuning")
			return Result.success()
		}

		val avgStart = startLevels.average().roundToInt()
		val avgEnd = endLevels.average().roundToInt()
		AppLogger.d(TAG, "Computed averages from history: avgStart=$avgStart avgEnd=$avgEnd")

		val historyState = HistoryUiState(
			historyDays = 30,
			sessions = sessions,
			averageStartLevel = avgStart,
			averageEndLevel = avgEnd
		)

		val rulesStore = BatteryRulesStore(appContext)
		val currentRules: BatteryRules = rulesStore.rulesFlow.first()
		AppLogger.d(
			TAG,
			"Current rules before tuning: lowEnabled=${currentRules.lowLevelEnabled} low=${currentRules.lowLevelPercentage}, " +
					"highEnabled=${currentRules.highLevelEnabled} high=${currentRules.highLevelPercentage}, " +
					"repeat=${currentRules.repeatIntervalMinutes}"
		)

		val autoRules = AutoRules.fromHistory(historyState)
		AppLogger.d(
			TAG,
			"AutoRules suggestion from history: low=${autoRules.lowLevelPercentage}, " +
					"high=${autoRules.highLevelPercentage}, repeat=${autoRules.repeatIntervalMinutes}"
		)

		val baselineLow = settings.baselineLow ?: currentRules.lowLevelPercentage
		val baselineHigh = settings.baselineHigh ?: currentRules.highLevelPercentage
		val baselineRepeat = settings.baselineRepeatMinutes
			?: currentRules.repeatIntervalMinutes
			?: 20

		fun blend(baseline: Int, target: Int): Int {
			val b = baseline.toDouble()
			val t = target.toDouble()
			return (0.6 * t + 0.4 * b).roundToInt()
		}

		val low = blend(
			baselineLow,
			autoRules.lowLevelPercentage
		).coerceIn(5, 40)

		val high = blend(
			baselineHigh,
			autoRules.highLevelPercentage
		).coerceIn(60, 95)

		val autoRepeat = autoRules.repeatIntervalMinutes ?: baselineRepeat
		val repeatMinutes = blend(baselineRepeat, autoRepeat).coerceIn(5, 60)

		val tuned = currentRules.copy(
			lowLevelEnabled = true,
			lowLevelPercentage = low,
			highLevelEnabled = true,
			highLevelPercentage = high,
			repeatIntervalMinutes = repeatMinutes
		)

		AppLogger.i(
			TAG,
			"Tuning result: low $baselineLow -> $low, high $baselineHigh -> $high, " +
					"repeat $baselineRepeat -> $repeatMinutes"
		)

		rulesStore.setRules(tuned)
		AppLogger.i(TAG, "AutoTuneWorker: rules updated successfully")

		return Result.success()
	}

	companion object {
		private const val TAG = "AutoTuneWorker"
		private const val UNIQUE_WORK_NAME = "auto_tune_battery_rules"

		fun schedule(context: Context) {
			AppLogger.i(TAG, "Scheduling AutoTuneWorker (periodic 24h)")

			val request = PeriodicWorkRequestBuilder<AutoTuneWorker>(
				24, TimeUnit.HOURS
			)
				.setInitialDelay(6, TimeUnit.HOURS)
				.build()

			WorkManager
				.getInstance(context.applicationContext)
				.enqueueUniquePeriodicWork(
					UNIQUE_WORK_NAME,
					ExistingPeriodicWorkPolicy.KEEP,
					request
				)

			AppLogger.d(TAG, "AutoTuneWorker scheduled with unique name=$UNIQUE_WORK_NAME")
		}
	}
}
