package io.github.ugurbuga.inspecto

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

internal class InspectoClearReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_CLEAR) {
            Inspecto.clear()
            InspectoNotification.clearRecentRequests()
            val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.cancel(8821)
        }
    }

    companion object {
        const val ACTION_CLEAR = "io.github.ugurbuga.inspecto.ACTION_CLEAR_LOGS"
    }
}
