package com.nolly.surecharge.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nolly.surecharge.data.history.ChargeHistoryRepository
import com.nolly.surecharge.data.history.ChargeSession
import com.nolly.surecharge.data.history.HistorySettingsStore
import com.nolly.surecharge.system.BatteryHealthAnalyzer
import com.nolly.surecharge.system.BatteryHealthSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class HistoryUiState(
	val historyDays: Int = 30,
	val sessions: List<ChargeSession> = emptyList(),
	val averageStartLevel: Int? = null,
	val averageEndLevel: Int? = null,
	val health: BatteryHealthSummary? = null
) {
	val hasData: Boolean get() = sessions.isNotEmpty()
}

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
	private val historyRepo = ChargeHistoryRepository(application)
	private val settingsStore = HistorySettingsStore(application)

	private val historyDaysFlow = settingsStore.historyDaysFlow

	val uiState: StateFlow<HistoryUiState> = combine(
		historyDaysFlow,
		historyDaysFlow.flatMapLatestWithHistoryRepo()
	) { days: Int, sessions: List<ChargeSession> ->
		val startLevels = sessions.map { it.startLevel }
		val endLevels = sessions.map { it.endLevel }

		val avgStart = if (startLevels.isNotEmpty()) startLevels.average() else null
		val avgEnd = if (endLevels.isNotEmpty()) endLevels.average() else null

		val healthSummary = BatteryHealthAnalyzer.fromSessions(sessions)

		HistoryUiState(
			historyDays = days,
			sessions = sessions,
			averageStartLevel = avgStart?.roundToInt(),
			averageEndLevel = avgEnd?.roundToInt(),
			health = healthSummary
		)
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = HistoryUiState()
	)

	fun updateHistoryDays(days: Int) {
		viewModelScope.launch {
			settingsStore.setHistoryDays(days)
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private fun Flow<Int>.flatMapLatestWithHistoryRepo(): Flow<List<ChargeSession>> =
		this.flatMapLatest { days ->
			historyRepo.sessionsForLastDays(days)
		}
}
