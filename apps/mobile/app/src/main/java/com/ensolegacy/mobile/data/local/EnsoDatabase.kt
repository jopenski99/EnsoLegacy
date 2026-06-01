package com.ensolegacy.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BonsaiEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class EnsoDatabase : RoomDatabase() {
    abstract fun bonsaiDao(): BonsaiDao

    companion object {
        /** v2: add the `health` care-status column (defaults to healthy). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE bonsai ADD COLUMN health TEXT NOT NULL DEFAULT 'healthy'",
                )
            }
        }
    }
}
