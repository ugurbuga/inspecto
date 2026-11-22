package com.ugurbuga.inspecto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class InspectoNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Inspecto.launch()
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        }
    }
}
