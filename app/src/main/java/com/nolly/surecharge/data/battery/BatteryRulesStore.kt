package com.nolly.surecharge.data.battery

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "battery_rules"

private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

class BatteryRulesStore(private val context: Context) {
	private object Keys {
		val LOW_ENABLED = booleanPreferencesKey("low_enabled")
		val LOW_PERCENTAGE = intPreferencesKey("low_percentage")
		val HIGH_ENABLED = booleanPreferencesKey("high_enabled")
		val HIGH_PERCENTAGE = intPreferencesKey("high_percentage")
		val REPEAT_MINUTES = intPreferencesKey("repeat_minutes")
	}

	val rulesFlow: Flow<BatteryRules> = context.dataStore.data.map { prefs ->
		BatteryRules(
			lowLevelEnabled = prefs[Keys.LOW_ENABLED] ?: true,
			lowLevelPercentage = prefs[Keys.LOW_PERCENTAGE] ?: 15,
			highLevelEnabled = prefs[Keys.HIGH_ENABLED] ?: true,
			highLevelPercentage = prefs[Keys.HIGH_PERCENTAGE] ?: 80,
			repeatIntervalMinutes = prefs[Keys.REPEAT_MINUTES]
		)
	}

	suspend fun setRules(newRules: BatteryRules) {
		context.dataStore.edit { prefs ->
			prefs[Keys.LOW_ENABLED] = newRules.lowLevelEnabled
			prefs[Keys.LOW_PERCENTAGE] = newRules.lowLevelPercentage
			prefs[Keys.HIGH_ENABLED] = newRules.highLevelEnabled
			prefs[Keys.HIGH_PERCENTAGE] = newRules.highLevelPercentage
			newRules.repeatIntervalMinutes?.let { prefs[Keys.REPEAT_MINUTES] = it } ?: prefs.remove(
				Keys.REPEAT_MINUTES
			)
		}
	}
}
