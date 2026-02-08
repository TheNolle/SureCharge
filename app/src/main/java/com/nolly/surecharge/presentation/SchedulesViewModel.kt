package com.nolly.surecharge.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nolly.surecharge.data.schedule.Schedule
import com.nolly.surecharge.data.schedule.SchedulesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SchedulesUiState(
	val schedules: List<Schedule> = emptyList()
)

class SchedulesViewModel(application: Application) : AndroidViewModel(application) {
	private val repo = SchedulesRepository(application)

	val state: StateFlow<SchedulesUiState> = repo.schedulesFlow
		.map { list -> SchedulesUiState(schedules = list) }
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = SchedulesUiState()
		)

	fun saveSchedule(schedule: Schedule) {
		viewModelScope.launch {
			repo.upsert(schedule)
		}
	}

	fun deleteSchedule(id: Long) {
		viewModelScope.launch {
			repo.delete(id)
		}
	}

	fun setScheduleEnabled(id: Long, enabled: Boolean) {
		viewModelScope.launch {
			repo.setEnabled(id, enabled)
		}
	}
}
