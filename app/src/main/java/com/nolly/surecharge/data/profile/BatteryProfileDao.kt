package com.nolly.surecharge.data.profile

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryProfileDao {
	@Query("SELECT * FROM battery_profiles ORDER BY orderIndex ASC, id ASC")
	fun profilesFlow(): Flow<List<BatteryProfileEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(entity: BatteryProfileEntity): Long

	@Update
	suspend fun update(entity: BatteryProfileEntity)

	@Delete
	suspend fun delete(entity: BatteryProfileEntity)

	@Query("SELECT COUNT(*) FROM battery_profiles")
	suspend fun count(): Int

	@Query("SELECT COALESCE(MAX(orderIndex), -1) FROM battery_profiles")
	suspend fun maxOrderIndex(): Int

	@Query("SELECT * FROM battery_profiles WHERE id = :id")
	suspend fun getById(id: Long): BatteryProfileEntity?
}
