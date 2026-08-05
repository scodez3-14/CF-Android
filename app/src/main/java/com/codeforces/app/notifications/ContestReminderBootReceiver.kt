package com.codeforces.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ContestReminderBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var manager: ContestReminderManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            manager.init()
            pendingResult.finish()
        }
    }
}
