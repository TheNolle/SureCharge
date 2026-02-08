package com.nolly.surecharge.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nolly.surecharge.data.AutoSettings
import com.nolly.surecharge.data.AutoSettingsStore
import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.data.battery.BatteryRulesStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SureChargeViewModel(application: Application) : AndroidViewModel(application) {
	private val rulesStore = BatteryRulesStore(application)
	private val autoSettingsStore = AutoSettingsStore(application)

	val rules: StateFlow<BatteryRules> = rulesStore.rulesFlow
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5_000),
			BatteryRules()
		)

	val autoSettings: StateFlow<AutoSettings> = autoSettingsStore.settingsFlow
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5_000),
			AutoSettings()
		)

	val effectiveRules: StateFlow<EffectiveRulesState?> =
		rulesStore.rulesFlow
			.map { baseRules ->
				EffectiveRulesState(
					baseRules = baseRules
				)
			}
			.stateIn(
				viewModelScope,
				SharingStarted.WhileSubscribed(5_000),
				null
			)

	fun setRules(newRules: BatteryRules) {
		viewModelScope.launch {
			rulesStore.setRules(newRules)

			val settings = autoSettingsStore.settingsFlow.first()
			if (settings.enabled) {
				autoSettingsStore.updateBaselineFromRules(newRules)
			}
		}
	}

	fun setAutoEnabled(enabled: Boolean) {
		viewModelScope.launch {
			autoSettingsStore.setEnabled(enabled)
			if (enabled) {
				val currentRules = rulesStore.rulesFlow.first()
				autoSettingsStore.updateBaselineFromRules(currentRules)
			}
		}
	}
}

data class EffectiveRulesState(
	val baseRules: BatteryRules
)
