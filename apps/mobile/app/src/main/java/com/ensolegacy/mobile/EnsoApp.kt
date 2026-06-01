package com.ensolegacy.mobile

import android.app.Application
import androidx.room.Room
import com.ensolegacy.mobile.data.AppPreferences
import com.ensolegacy.mobile.data.SpeciesCatalog
import com.ensolegacy.mobile.data.local.EnsoDatabase
import com.ensolegacy.mobile.data.repository.BonsaiRepository

/**
 * Application entry point and manual service locator.
 *
 * Holds the process-wide singletons (Room database, repositories, the species
 * catalog) and hands them to ViewModels via their factories. This is the
 * lightweight alternative to Hilt for a solo Phase-2 build — when wiring grows
 * past what this comfortably holds, revisit DI.
 */
class EnsoApp : Application() {

    val database: EnsoDatabase by lazy {
        Room.databaseBuilder(this, EnsoDatabase::class.java, "enso.db")
            .addMigrations(EnsoDatabase.MIGRATION_1_2)
            .build()
    }

    val bonsaiRepository: BonsaiRepository by lazy {
        BonsaiRepository(database.bonsaiDao())
    }

    val speciesCatalog: SpeciesCatalog by lazy {
        SpeciesCatalog(this)
    }

    val appPreferences: AppPreferences by lazy {
        AppPreferences(this)
    }
}
