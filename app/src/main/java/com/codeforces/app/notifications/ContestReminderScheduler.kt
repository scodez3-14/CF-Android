package com.codeforces.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.codeforces.app.data.api.ContestDto

class ContestReminderScheduler(private val context: Context) {

    private val scheduledIds = mutableSetOf<Int>()

    fun scheduleAll(contests: List<ContestDto>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        contests.forEach { contest ->
            val startTimeSeconds = contest.startTimeSeconds ?: return@forEach
            val fireAt = startTimeSeconds * 1000 - REMINDER_LEAD_MILLIS
            if (fireAt > now) {
                scheduleOne(alarmManager, contest.id, contest.name, startTimeSeconds, fireAt)
                scheduledIds.add(contest.id)
            }
        }
    }

    fun cancelAll() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduledIds.forEach { alarmManager.cancel(pendingIntent(it)) }
        scheduledIds.clear()
    }

    private fun scheduleOne(
        alarmManager: AlarmManager,
        contestId: Int,
        contestName: String,
        startTimeSeconds: Long,
        fireAt: Long
    ) {
        val pending = pendingIntent(contestId, contestName, startTimeSeconds)
        val canExact = Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pending)
        }
    }

    private fun pendingIntent(contestId: Int, contestName: String = "", startTimeSeconds: Long = 0): PendingIntent {
        val intent = Intent(context, ContestReminderReceiver::class.java).apply {
            putExtra("contest_id", contestId)
            putExtra("contest_name", contestName)
            putExtra("start_time", startTimeSeconds)
        }
        return PendingIntent.getBroadcast(
            context, contestId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        private const val REMINDER_LEAD_MILLIS = 30 * 60 * 1000L
    }
}
