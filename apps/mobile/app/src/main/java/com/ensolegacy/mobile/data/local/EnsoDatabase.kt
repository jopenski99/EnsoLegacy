package com.ensolegacy.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BonsaiEntity::class, CareReminderEntity::class],
    version = 4,
    exportSchema = true,
)
abstract class EnsoDatabase : RoomDatabase() {
    abstract fun bonsaiDao(): BonsaiDao
    abstract fun careReminderDao(): CareReminderDao

    companion object {
        /** v2: add the `health` care-status column (defaults to healthy). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE bonsai ADD COLUMN health TEXT NOT NULL DEFAULT 'healthy'",
                )
            }
        }

        /** v3: add `careScheduleSet` — existing trees start as not-yet-set (0). */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE bonsai ADD COLUMN careScheduleSet INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        /** v4: add the `care_reminder` table (DDL mirrors Room's generated schema). */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `care_reminder` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`bonsaiId` INTEGER NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`intervalDays` INTEGER NOT NULL, " +
                        "`nextDueAt` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`bonsaiId`) REFERENCES `bonsai`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE )",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_care_reminder_bonsaiId` " +
                        "ON `care_reminder` (`bonsaiId`)",
                )
            }
        }
    }
}
