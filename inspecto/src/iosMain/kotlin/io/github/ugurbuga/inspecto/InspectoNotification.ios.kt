package io.github.ugurbuga.inspecto

import platform.Foundation.NSLock
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationActionOptionDestructive
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionCustomDismissAction
import platform.UserNotifications.UNNotificationPresentationOptionBadge
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNNotificationDefaultActionIdentifier
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

private class InspectoNotificationDelegate : NSObject(), UNUserNotificationCenterDelegateProtocol {
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
    ) {
        // Add to Notification Center list without showing intrusive banner popup on screen
        withCompletionHandler(
            UNNotificationPresentationOptionList or
            UNNotificationPresentationOptionBadge
        )
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        val actionId = didReceiveNotificationResponse.actionIdentifier
        if (actionId == UNNotificationDefaultActionIdentifier || actionId == "OPEN_ACTION") {
            launchInspecto()
        } else if (actionId == "CLEAR_ACTION") {
            Inspecto.clear()
        }
        withCompletionHandler()
    }
}

actual object InspectoNotification {
    private val recentRequests = mutableListOf<String>()
    private const val MAX_RECENT_REQUESTS = 5
    private const val NOTIFICATION_IDENTIFIER = "inspecto_ongoing_notification"
    private val lock = NSLock()
    private val notificationDelegate = InspectoNotificationDelegate()

    actual fun initialize() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.delegate = notificationDelegate

        val openAction = UNNotificationAction.actionWithIdentifier(
            identifier = "OPEN_ACTION",
            title = "OPEN",
            options = 0u
        )
        val clearAction = UNNotificationAction.actionWithIdentifier(
            identifier = "CLEAR_ACTION",
            title = "CLEAR",
            options = UNNotificationActionOptionDestructive
        )
        val category = UNNotificationCategory.categoryWithIdentifier(
            identifier = "INSPECTO_TRAFFIC_CATEGORY",
            actions = listOf(openAction, clearAction),
            intentIdentifiers = listOf<String>(),
            options = UNNotificationCategoryOptionCustomDismissAction
        )
        center.setNotificationCategories(setOf(category))
    }

    actual fun showServiceCallNotification(entry: InspectoLogEntry) {
        initialize()
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.getNotificationSettingsWithCompletionHandler { settings ->
            if (settings != null && settings.authorizationStatus >= 2L /* UNAuthorizationStatusAuthorized */) {
                val symbol = if (entry.isSuccess) "✓" else "✕"
                val line = "$symbol ${entry.statusCode} ${entry.method} ${entry.path} (${entry.durationMs}ms)"
                lock.lock()
                try {
                    recentRequests.add(0, line)
                    if (recentRequests.size > MAX_RECENT_REQUESTS) {
                        recentRequests.removeAt(recentRequests.size - 1)
                    }
                } finally {
                    lock.unlock()
                }

                val bodyText = lock.let {
                    it.lock()
                    try {
                        recentRequests.joinToString("\n")
                    } finally {
                        it.unlock()
                    }
                }

                val content = UNMutableNotificationContent().apply {
                    setTitle("Inspecto")
                    setBody(bodyText)
                    setCategoryIdentifier("INSPECTO_TRAFFIC_CATEGORY")
                }
                val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(0.1, false)
                val request = UNNotificationRequest.requestWithIdentifier(
                    identifier = NOTIFICATION_IDENTIFIER,
                    content = content,
                    trigger = trigger
                )
                center.addNotificationRequest(request, null)
            }
        }
    }

    actual fun requestPermission(onResult: (Boolean) -> Unit) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, _ ->
            if (granted) {
                initialize()
            }
            onResult(granted)
        }
    }
}
