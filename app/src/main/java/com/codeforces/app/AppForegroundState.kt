package com.codeforces.app

/**
 * Whether the app is currently visible to the user. Maintained by
 * [MainActivity] lifecycle callbacks (the app is single-activity, so the
 * activity's resumed state is the app's foreground state).
 */
object AppForegroundState {
    @Volatile
    var isForeground: Boolean = false
        private set

    fun setForeground(foreground: Boolean) {
        isForeground = foreground
    }
}
