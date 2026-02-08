package com.nolly.surecharge.data.profile

import android.content.Context
import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.data.SureChargeDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BatteryProfilesRepository(context: Context) {
	private val dao = SureChargeDatabase.getInstance(context).batteryProfileDao()

	val profilesFlow: Flow<List<BatteryProfile>> =
		dao.profilesFlow().map { list -> list.map { it.toDomain() } }

	suspend fun ensureDefaultProfiles() {
		val count = dao.count()
		if (count > 0) return

		val defaults = listOf(
			BatteryProfileEntity(
				name = "Balanced daily",
				lowLevelEnabled = true,
				lowLevelPercentage = 20,
				highLevelEnabled = true,
				highLevelPercentage = 80,
				repeatIntervalMinutes = 15,
				builtIn = true,
				orderIndex = 0
			),
			BatteryProfileEntity(
				name = "Battery health first",
				lowLevelEnabled = true,
				lowLevelPercentage = 25,
				highLevelEnabled = true,
				highLevelPercentage = 75,
				repeatIntervalMinutes = 30,
				builtIn = true,
				orderIndex = 1
			),
			BatteryProfileEntity(
				name = "Full charge",
				lowLevelEnabled = false,
				lowLevelPercentage = 15,
				highLevelEnabled = true,
				highLevelPercentage = 100,
				repeatIntervalMinutes = null,
				builtIn = true,
				orderIndex = 2
			)
		)

		defaults.forEach { dao.insert(it) }
	}

	suspend fun createProfileFromRules(name: String, rules: BatteryRules) {
		val maxOrder = dao.maxOrderIndex()
		val entity = BatteryProfileEntity(
			name = name.ifBlank { "Profile ${(maxOrder + 2)}" },
			lowLevelEnabled = rules.lowLevelEnabled,
			lowLevelPercentage = rules.lowLevelPercentage,
			highLevelEnabled = rules.highLevelEnabled,
			highLevelPercentage = rules.highLevelPercentage,
			repeatIntervalMinutes = rules.repeatIntervalMinutes,
			builtIn = false,
			orderIndex = maxOrder + 1
		)
		dao.insert(entity)
	}

	suspend fun deleteProfile(id: Long) {
		val existing = dao.getById(id) ?: return
		dao.delete(existing)
	}
}
