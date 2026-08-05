package com.codeforces.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.codeforces.app.MainActivity
import com.codeforces.app.R

object NotificationHelper {
    const val CHANNEL_ID = "contest_reminders"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Contest reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders before Codeforces contests start"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun showReminder(context: Context, contestId: Int, contestName: String, startTimeSeconds: Long) {
        val minutesLeft = ((startTimeSeconds * 1000 - System.currentTimeMillis()) / 60000).coerceAtLeast(0)
        val content = when {
            minutesLeft >= 60 -> "Starts in ${minutesLeft / 60}h ${minutesLeft % 60}m"
            minutesLeft > 0 -> "Starts in $minutesLeft minutes"
            else -> "Starting now"
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, contestId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(contestName)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(contestId, notification)
        }
    }
}
