package com.ugurbuga.inspecto

import android.content.Intent

actual fun shareText(text: String, title: String?) {
    val context = InspectoContextHolder.applicationContext ?: return
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title ?: "Inspecto Log")
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, title ?: "Share Network Log").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (_: Exception) {
    }
}
