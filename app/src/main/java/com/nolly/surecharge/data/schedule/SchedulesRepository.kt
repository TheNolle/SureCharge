package com.nolly.surecharge.data.schedule

import android.content.Context
import com.nolly.surecharge.data.SureChargeDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SchedulesRepository(context: Context) {
	private val dao: ScheduleDao = SureChargeDatabase.getInstance(context).scheduleDao()

	val schedulesFlow: Flow<List<Schedule>> =
		dao.schedulesFlow().map { list -> list.map { it.toDomain() } }

	suspend fun upsert(schedule: Schedule) {
		if (schedule.id == 0L) {
			val maxPriority = dao.maxPriority()
			val entity = schedule.copy(priority = maxPriority + 1).toEntity()
			dao.insert(entity)
		} else {
			val existing = dao.getById(schedule.id) ?: return
			val entity = schedule.copy(priority = existing.priority).toEntity()
			dao.update(entity)
		}
	}

	suspend fun delete(id: Long) {
		val existing = dao.getById(id) ?: return
		dao.delete(existing)
	}

	suspend fun setEnabled(id: Long, enabled: Boolean) {
		val existing = dao.getById(id) ?: return
		dao.update(existing.copy(enabled = enabled))
	}
}
