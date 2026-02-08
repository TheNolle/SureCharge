package com.nolly.surecharge.data.schedule

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
	@Query("SELECT * FROM schedules ORDER BY priority DESC, id ASC")
	fun schedulesFlow(): Flow<List<ScheduleEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(entity: ScheduleEntity): Long

	@Update
	suspend fun update(entity: ScheduleEntity)

	@Delete
	suspend fun delete(entity: ScheduleEntity)

	@Query("SELECT * FROM schedules WHERE id = :id")
	suspend fun getById(id: Long): ScheduleEntity?

	@Query("SELECT COALESCE(MAX(priority), 0) FROM schedules")
	suspend fun maxPriority(): Int
}
