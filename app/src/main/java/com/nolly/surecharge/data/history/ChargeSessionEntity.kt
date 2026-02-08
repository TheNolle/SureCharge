package com.nolly.surecharge.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charge_sessions")
data class ChargeSessionEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val startTimestamp: Long,
	val endTimestamp: Long,
	val startLevel: Int,
	val endLevel: Int
)
