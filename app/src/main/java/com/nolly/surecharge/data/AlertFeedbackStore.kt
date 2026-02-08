package com.nolly.surecharge.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "alert_feedback"

private val Context.alertFeedbackDataStore by preferencesDataStore(name = DATASTORE_NAME)

data class AlertFeedback(
	val shortSnoozeCount: Int = 0,
	val longSnoozeCount: Int = 0,
	val disableTodayCount: Int = 0
)

class AlertFeedbackStore(private val context: Context) {
	private object Keys {
		val SHORT_SNOOZE: Preferences.Key<Int> = intPreferencesKey("short_snooze")
		val LONG_SNOOZE: Preferences.Key<Int> = intPreferencesKey("long_snooze")
		val DISABLE_TODAY: Preferences.Key<Int> = intPreferencesKey("disable_today")
	}

	val feedbackFlow: Flow<AlertFeedback> =
		context.alertFeedbackDataStore.data.map { prefs ->
			AlertFeedback(
				shortSnoozeCount = prefs[Keys.SHORT_SNOOZE] ?: 0,
				longSnoozeCount = prefs[Keys.LONG_SNOOZE] ?: 0,
				disableTodayCount = prefs[Keys.DISABLE_TODAY] ?: 0
			)
		}

	suspend fun recordShortSnooze() {
		increment(Keys.SHORT_SNOOZE)
	}

	suspend fun recordLongSnooze() {
		increment(Keys.LONG_SNOOZE)
	}

	suspend fun recordDisableToday() {
		increment(Keys.DISABLE_TODAY)
	}

	private suspend fun increment(key: Preferences.Key<Int>) {
		context.alertFeedbackDataStore.edit { prefs ->
			val current = prefs[key] ?: 0
			prefs[key] = (current + 1).coerceAtMost(1000)
		}
	}
}
