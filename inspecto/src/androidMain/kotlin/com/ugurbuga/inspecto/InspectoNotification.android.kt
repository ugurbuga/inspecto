package com.ugurbuga.inspecto

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

actual object InspectoNotification {
    private const val CHANNEL_ID = "inspecto_network_calls_channel"
    private const val CHANNEL_NAME = "Inspecto"
    private const val NOTIFICATION_ID = 8821

    private val recentRequests = mutableListOf<InspectoLogEntry>()
    private const val MAX_RECENT_REQUESTS = 5

    fun clearRecentRequests() {
        synchronized(recentRequests) {
            recentRequests.clear()
        }
    }

    actual fun initialize() {
        val context = InspectoContextHolder.applicationContext ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Inspecto network traffic & performance monitor"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    actual fun showServiceCallNotification(entry: InspectoLogEntry) {
        val context = InspectoContextHolder.applicationContext ?: return

        // Permission check for Android 13+ (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        initialize()

        synchronized(recentRequests) {
            recentRequests.add(0, entry)
            if (recentRequests.size > MAX_RECENT_REQUESTS) {
                recentRequests.removeAt(recentRequests.size - 1)
            }
        }

        val intent = Intent(context, InspectoActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Clear action broadcast intent
        val clearIntent = Intent(context, InspectoClearReceiver::class.java).apply {
            action = InspectoClearReceiver.ACTION_CLEAR
        }
        val clearPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            clearIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("Inspecto")

        synchronized(recentRequests) {
            recentRequests.forEach { req ->
                val symbol = if (req.isSuccess) "✓" else "✕"
                val text = "$symbol ${req.statusCode} ${req.method} ${req.path} (${req.durationMs}ms)"
                val spannable = SpannableString(text)

                // 1. Color symbol icon (Green for success, Red for error)
                val symbolColor = if (req.isSuccess) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
                spannable.setSpan(
                    ForegroundColorSpan(symbolColor),
                    0,
                    1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // 2. Color status code
                val statusColor = when {
                    req.statusCode in 200..299 -> Color.parseColor("#10B981")
                    req.statusCode in 300..399 -> Color.parseColor("#F59E0B")
                    else -> Color.parseColor("#EF4444")
                }
                val statusStr = "${req.statusCode}"
                val statusIndex = text.indexOf(statusStr)
                if (statusIndex >= 0) {
                    spannable.setSpan(
                        ForegroundColorSpan(statusColor),
                        statusIndex,
                        statusIndex + statusStr.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                // 3. Color method (Matching in-app InspectoColors)
                val methodColor = when (req.method.uppercase()) {
                    InspectoMethod.GET -> Color.parseColor("#0284C7")
                    InspectoMethod.POST -> Color.parseColor("#0EA5E9")
                    InspectoMethod.PUT -> Color.parseColor("#8B5CF6")
                    InspectoMethod.DELETE -> Color.parseColor("#EC4899")
                    InspectoMethod.PATCH -> Color.parseColor("#6366F1")
                    InspectoMethod.OPTIONS -> Color.parseColor("#475569")
                    InspectoMethod.HEAD -> Color.parseColor("#334155")
                    else -> Color.parseColor("#6B7280")
                }
                val methodIndex = text.indexOf(req.method)
                if (methodIndex >= 0) {
                    spannable.setSpan(
                        ForegroundColorSpan(methodColor),
                        methodIndex,
                        methodIndex + req.method.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                inboxStyle.addLine(spannable)
            }
        }

        val firstReq = recentRequests.firstOrNull()
        val contentText = if (firstReq != null) {
            "${firstReq.statusCode} ${firstReq.method} ${firstReq.path}"
        } else {
            "Inspecto Network Monitor"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Inspecto")
            .setContentText(contentText)
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false) // Allow swipe to dismiss
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "OPEN", pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "CLEAR", clearPendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, builder.build())
    }

    actual fun requestPermission(onResult: (Boolean) -> Unit) {
        val context = InspectoContextHolder.applicationContext
        if (context == null) {
            onResult(false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            onResult(isGranted)
        } else {
            onResult(true)
        }
    }
}
