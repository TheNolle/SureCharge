package com.nolly.surecharge.data.history

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "history_settings"

private val Context.historyDataStore by preferencesDataStore(name = DATASTORE_NAME)

class HistorySettingsStore(private val context: Context) {
	private object Keys {
		val HISTORY_DAYS = intPreferencesKey("history_days")
	}

	val historyDaysFlow: Flow<Int> = context.historyDataStore.data.map { prefs ->
		prefs[Keys.HISTORY_DAYS] ?: 30
	}

	suspend fun setHistoryDays(days: Int) {
		context.historyDataStore.edit { prefs ->
			prefs[Keys.HISTORY_DAYS] = days
		}
	}
}
