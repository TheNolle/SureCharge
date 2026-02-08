package com.nolly.surecharge.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargeSessionDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(session: ChargeSessionEntity): Long

	@Query(
		"""
        SELECT * FROM charge_sessions
        WHERE startTimestamp >= :fromTimestamp
        ORDER BY startTimestamp DESC
    """
	)
	fun sessionsSince(fromTimestamp: Long): Flow<List<ChargeSessionEntity>>

	@Query("DELETE FROM charge_sessions WHERE startTimestamp < :beforeTimestamp")
	suspend fun deleteBefore(beforeTimestamp: Long)
}
