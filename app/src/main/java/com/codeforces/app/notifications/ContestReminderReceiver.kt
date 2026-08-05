package com.codeforces.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ContestReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val contestId = intent.getIntExtra("contest_id", 0)
        val contestName = intent.getStringExtra("contest_name") ?: "Codeforces contest"
        val startTimeSeconds = intent.getLongExtra("start_time", 0L)
        if (contestId != 0) {
            NotificationHelper.createChannel(context)
            NotificationHelper.showReminder(context, contestId, contestName, startTimeSeconds)
        }
    }
}
