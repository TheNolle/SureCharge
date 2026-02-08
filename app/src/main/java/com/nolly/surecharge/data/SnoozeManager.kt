package com.nolly.surecharge.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SnoozeManager private constructor(context: Context) {

	private val prefs: SharedPreferences =
		context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

	fun snoozeForMinutes(minutes: Int) {
		val durationMillis = minutes * 60_000L
		val until = System.currentTimeMillis() + durationMillis
		prefs.edit { putLong(KEY_SNOOZE_UNTIL, until) }
	}

	fun clearSnooze() {
		prefs.edit { remove(KEY_SNOOZE_UNTIL) }
	}

	fun isSnoozed(): Boolean {
		val until = prefs.getLong(KEY_SNOOZE_UNTIL, 0L)
		return until > System.currentTimeMillis()
	}

	companion object {
		private const val PREFS_NAME = "surecharge_snooze"
		private const val KEY_SNOOZE_UNTIL = "snooze_until"

		fun from(context: Context): SnoozeManager = SnoozeManager(context.applicationContext)
	}
}
