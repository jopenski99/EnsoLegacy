package com.ensolegacy.mobile.feature

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ensolegacy.mobile.EnsoApp
import com.ensolegacy.mobile.MainActivity
import com.ensolegacy.mobile.domain.CareType

/**
 * Daily worker that fires a notification for each overdue care task.
 * Capped at 5 notifications per run to avoid overwhelming the owner.
 */
class CareReminderCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as EnsoApp
            val now = System.currentTimeMillis()
            Log.d("CareWorker", "Checking for overdue reminders at $now")
            val overdue = app.careReminderRepository.getOverdueWithBonsai(now)
            Log.d("CareWorker", "Found ${overdue.size} overdue reminders")
            if (overdue.isEmpty()) return Result.success()

            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val items = overdue.take(5)
            items.forEachIndexed { i, item ->
                Log.d("CareWorker", "Creating notification for ${item.bonsaiName}: ${item.reminder.type}")
                val type = CareType.fromValue(item.reminder.type)
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pi = PendingIntent.getActivity(
                    applicationContext,
                    NOTIF_BASE_ID + i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle("Ensō Legacy")
                    .setContentText("${type.label} due for ${item.bonsaiName}")
                    .setContentIntent(pi)
                    .setGroup(GROUP_KEY)
                    .setAutoCancel(true)
                    .build()
                nm.notify(NOTIF_BASE_ID + i, notif)
            }
            // Summary notification so multiple items collapse into one.
            val summaryIntent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val summaryPi = PendingIntent.getActivity(
                applicationContext,
                SUMMARY_PI_REQUEST_CODE,
                summaryIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val summary = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Ensō Legacy")
                .setContentText("${items.size} care task${if (items.size == 1) "" else "s"} due")
                .setContentIntent(summaryPi)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .build()
            nm.notify(SUMMARY_NOTIF_ID, summary)
            Log.d("CareWorker", "Successfully created ${items.size} notifications")
            Result.success()
        } catch (e: Exception) {
            Log.e("CareWorker", "Worker failed", e)
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val CHANNEL_ID = "care_reminders"
        const val WORK_NAME = "care_reminder_check"
        private const val NOTIF_BASE_ID = 1000
        private const val SUMMARY_NOTIF_ID = 999
        private const val SUMMARY_PI_REQUEST_CODE = 900
        private const val GROUP_KEY = "enso_care_reminders"
    }
}
