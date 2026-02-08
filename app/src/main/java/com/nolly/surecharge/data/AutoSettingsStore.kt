package com.nolly.surecharge.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nolly.surecharge.data.battery.BatteryRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AutoSettings(
	val enabled: Boolean = false,
	val baselineLow: Int? = null,
	val baselineHigh: Int? = null,
	val baselineRepeatMinutes: Int? = null
)

private const val AUTO_DATASTORE_NAME = "auto_settings"

private val Context.autoSettingsDataStore by preferencesDataStore(
	name = AUTO_DATASTORE_NAME
)

class AutoSettingsStore(private val context: Context) {
	private object Keys {
		val ENABLED = booleanPreferencesKey("enabled")
		val BASELINE_LOW = intPreferencesKey("baseline_low")
		val BASELINE_HIGH = intPreferencesKey("baseline_high")
		val BASELINE_REPEAT = intPreferencesKey("baseline_repeat_minutes")
	}

	val settingsFlow: Flow<AutoSettings> =
		context.autoSettingsDataStore.data.map { prefs ->
			AutoSettings(
				enabled = prefs[Keys.ENABLED] ?: false,
				baselineLow = prefs[Keys.BASELINE_LOW],
				baselineHigh = prefs[Keys.BASELINE_HIGH],
				baselineRepeatMinutes = prefs[Keys.BASELINE_REPEAT]
			)
		}

	suspend fun setEnabled(enabled: Boolean) {
		context.autoSettingsDataStore.edit { prefs ->
			prefs[Keys.ENABLED] = enabled
		}
	}

	suspend fun updateBaselineFromRules(rules: BatteryRules) {
		context.autoSettingsDataStore.edit { prefs ->
			prefs[Keys.BASELINE_LOW] = rules.lowLevelPercentage
			prefs[Keys.BASELINE_HIGH] = rules.highLevelPercentage
			rules.repeatIntervalMinutes?.let { repeat ->
				prefs[Keys.BASELINE_REPEAT] = repeat
			}
		}
	}
}
