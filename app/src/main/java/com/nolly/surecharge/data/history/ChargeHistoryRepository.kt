package com.nolly.surecharge.data.history

import android.content.Context
import com.nolly.surecharge.data.SureChargeDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

data class ChargeSession(
	val id: Long,
	val startTimeMillis: Long,
	val endTimeMillis: Long,
	val startLevel: Int,
	val endLevel: Int
)

class ChargeHistoryRepository(context: Context) {
	private val dao = SureChargeDatabase.getInstance(context).chargeSessionDao()

	fun sessionsForLastDays(days: Int): Flow<List<ChargeSession>> {
		val now = System.currentTimeMillis()
		val from = now - TimeUnit.DAYS.toMillis(days.toLong())
		return dao.sessionsSince(from).map { list ->
			list.map { entity ->
				ChargeSession(
					id = entity.id,
					startTimeMillis = entity.startTimestamp,
					endTimeMillis = entity.endTimestamp,
					startLevel = entity.startLevel,
					endLevel = entity.endLevel
				)
			}
		}
	}

	suspend fun addSession(
		startTimeMillis: Long,
		endTimeMillis: Long,
		startLevel: Int,
		endLevel: Int
	) {
		val entity = ChargeSessionEntity(
			startTimestamp = startTimeMillis,
			endTimestamp = endTimeMillis,
			startLevel = startLevel,
			endLevel = endLevel
		)
		dao.insert(entity)
	}

	@Suppress("Unused")
	suspend fun pruneOlderThan(days: Int) {
		val now = System.currentTimeMillis()
		val before = now - TimeUnit.DAYS.toMillis(days.toLong())
		dao.deleteBefore(before)
	}
}
