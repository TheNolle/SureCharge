package com.nolly.surecharge.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.data.profile.BatteryProfile
import com.nolly.surecharge.data.profile.BatteryProfilesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfilesUiState(
	val profiles: List<BatteryProfile> = emptyList()
)

class BatteryProfilesViewModel(application: Application) : AndroidViewModel(application) {
	private val repo = BatteryProfilesRepository(application)

	val state: StateFlow<ProfilesUiState> = repo.profilesFlow
		.map { list -> ProfilesUiState(profiles = list) }
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = ProfilesUiState()
		)

	init {
		viewModelScope.launch {
			repo.ensureDefaultProfiles()
		}
	}

	fun createProfileFromRules(name: String, rules: BatteryRules) {
		viewModelScope.launch {
			repo.createProfileFromRules(name, rules)
		}
	}

	fun deleteProfile(id: Long) {
		viewModelScope.launch {
			repo.deleteProfile(id)
		}
	}
}
