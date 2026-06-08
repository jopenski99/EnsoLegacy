package com.ensolegacy.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        BonsaiEntity::class,
        CareReminderEntity::class,
        MilestoneEntity::class,
        PhotoEntity::class,
        StageTransitionEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
abstract class EnsoDatabase : RoomDatabase() {
    abstract fun bonsaiDao(): BonsaiDao
    abstract fun careReminderDao(): CareReminderDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun photoDao(): PhotoDao
    abstract fun stageTransitionDao(): StageTransitionDao

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

        /**
         * v5: photos. Adds the `coverPhotoPath` column to bonsai, the `milestone`
         * table (timeline events), and the `photo` table (vault + milestone
         * photos). DDL mirrors Room's generated schema for these entities.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bonsai ADD COLUMN coverPhotoPath TEXT")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `milestone` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`bonsaiId` INTEGER NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`notes` TEXT, " +
                        "`occurredAt` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`bonsaiId`) REFERENCES `bonsai`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE )",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_milestone_bonsaiId` " +
                        "ON `milestone` (`bonsaiId`)",
                )

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `photo` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`bonsaiId` INTEGER NOT NULL, " +
                        "`milestoneId` INTEGER, " +
                        "`path` TEXT NOT NULL, " +
                        "`caption` TEXT, " +
                        "`orderIndex` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`bonsaiId`) REFERENCES `bonsai`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                        "FOREIGN KEY(`milestoneId`) REFERENCES `milestone`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE )",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_photo_bonsaiId` " +
                        "ON `photo` (`bonsaiId`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_photo_milestoneId` " +
                        "ON `photo` (`milestoneId`)",
                )
            }
        }

        /**
         * v6: acquisition context + provenance fields on bonsai, and the
         * `stage_transition` table for tracking life-stage history.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bonsai ADD COLUMN acquisitionSource TEXT")
                db.execSQL("ALTER TABLE bonsai ADD COLUMN placement TEXT")
                db.execSQL("ALTER TABLE bonsai ADD COLUMN origin TEXT")
                db.execSQL("ALTER TABLE bonsai ADD COLUMN acquiredFrom TEXT")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `stage_transition` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`bonsaiId` INTEGER NOT NULL, " +
                        "`fromStage` TEXT NOT NULL, " +
                        "`toStage` TEXT NOT NULL, " +
                        "`notes` TEXT, " +
                        "`recordedAt` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`bonsaiId`) REFERENCES `bonsai`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE )",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_stage_transition_bonsaiId` " +
                        "ON `stage_transition` (`bonsaiId`)",
                )
            }
        }
    }
}
