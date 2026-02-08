package com.nolly.surecharge.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nolly.surecharge.data.history.ChargeSessionDao
import com.nolly.surecharge.data.history.ChargeSessionEntity
import com.nolly.surecharge.data.profile.BatteryProfileDao
import com.nolly.surecharge.data.profile.BatteryProfileEntity
import com.nolly.surecharge.data.schedule.ScheduleDao
import com.nolly.surecharge.data.schedule.ScheduleEntity

@Database(
	entities = [
		ChargeSessionEntity::class,
		BatteryProfileEntity::class,
		ScheduleEntity::class
	],
	version = 3,
	exportSchema = false
)
abstract class SureChargeDatabase : RoomDatabase() {
	abstract fun chargeSessionDao(): ChargeSessionDao
	abstract fun batteryProfileDao(): BatteryProfileDao
	abstract fun scheduleDao(): ScheduleDao

	companion object {
		@Volatile
		private var INSTANCE: SureChargeDatabase? = null

		private val MIGRATION_1_2 = object : Migration(1, 2) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL(
					"""
                    CREATE TABLE IF NOT EXISTS `battery_profiles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `lowLevelEnabled` INTEGER NOT NULL,
                        `lowLevelPercentage` INTEGER NOT NULL,
                        `highLevelEnabled` INTEGER NOT NULL,
                        `highLevelPercentage` INTEGER NOT NULL,
                        `repeatIntervalMinutes` INTEGER,
                        `builtIn` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL
                    )
                    """.trimIndent()
				)
			}
		}

		private val MIGRATION_2_3 = object : Migration(2, 3) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL(
					"""
                    CREATE TABLE IF NOT EXISTS `schedules` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `enabled` INTEGER NOT NULL,
                        `daysMask` INTEGER NOT NULL,
                        `startMinutes` INTEGER NOT NULL,
                        `endMinutes` INTEGER NOT NULL,
                        `useProfileId` INTEGER,
                        `overrideLowEnabled` INTEGER,
                        `overrideLowPercent` INTEGER,
                        `overrideHighEnabled` INTEGER,
                        `overrideHighPercent` INTEGER,
                        `overrideRepeatMinutes` INTEGER,
                        `priority` INTEGER NOT NULL
                    )
                    """.trimIndent()
				)
			}
		}

		fun getInstance(context: Context): SureChargeDatabase {
			return INSTANCE ?: synchronized(this) {
				INSTANCE ?: Room.databaseBuilder(
					context.applicationContext,
					SureChargeDatabase::class.java,
					"surecharge.db"
				)
					.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
					.build()
					.also { INSTANCE = it }
			}
		}
	}
}
