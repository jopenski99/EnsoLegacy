package com.ensolegacy.mobile.feature

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
        val app = applicationContext as EnsoApp
        val now = System.currentTimeMillis()
        val overdue = app.careReminderRepository.getOverdueWithBonsai(now)
        if (overdue.isEmpty()) return Result.success()

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        overdue.take(5).forEachIndexed { i, item ->
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
                .setContentTitle("${type.emoji} ${type.label} due")
                .setContentText(item.bonsaiName)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
            nm.notify(NOTIF_BASE_ID + i, notif)
        }
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "care_reminders"
        const val WORK_NAME = "care_reminder_check"
        private const val NOTIF_BASE_ID = 1000
    }
}
