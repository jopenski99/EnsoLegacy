package com.ensolegacy.mobile

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ensolegacy.mobile.data.AppPreferences
import com.ensolegacy.mobile.data.ImageStore
import com.ensolegacy.mobile.data.SpeciesCatalog
import com.ensolegacy.mobile.data.local.EnsoDatabase
import com.ensolegacy.mobile.data.repository.BonsaiRepository
import com.ensolegacy.mobile.data.repository.CareReminderRepository
import com.ensolegacy.mobile.data.repository.MilestoneRepository
import com.ensolegacy.mobile.data.repository.StageTransitionRepository
import com.ensolegacy.mobile.feature.CareReminderCheckWorker
import java.util.concurrent.TimeUnit

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
            .addMigrations(
                EnsoDatabase.MIGRATION_1_2,
                EnsoDatabase.MIGRATION_2_3,
                EnsoDatabase.MIGRATION_3_4,
                EnsoDatabase.MIGRATION_4_5,
                EnsoDatabase.MIGRATION_5_6,
            )
            .build()
    }

    val imageStore: ImageStore by lazy {
        ImageStore(this)
    }

    val bonsaiRepository: BonsaiRepository by lazy {
        BonsaiRepository(database.bonsaiDao())
    }

    val careReminderRepository: CareReminderRepository by lazy {
        CareReminderRepository(database.careReminderDao())
    }

    val milestoneRepository: MilestoneRepository by lazy {
        MilestoneRepository(database.milestoneDao(), database.photoDao(), imageStore)
    }

    val stageTransitionRepository: StageTransitionRepository by lazy {
        StageTransitionRepository(database.stageTransitionDao())
    }

    val speciesCatalog: SpeciesCatalog by lazy {
        SpeciesCatalog(this)
    }

    val appPreferences: AppPreferences by lazy {
        AppPreferences(this)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleDailyCareCheck()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CareReminderCheckWorker.CHANNEL_ID,
                "Care Reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Reminders for bonsai care tasks that are due or overdue"
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun scheduleDailyCareCheck() {
        val req = PeriodicWorkRequestBuilder<CareReminderCheckWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            CareReminderCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            req,
        )
    }
}
