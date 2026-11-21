package io.github.ugurbuga.inspecto

actual object InspectoNotification {
    actual fun initialize() {}
    actual fun showServiceCallNotification(entry: InspectoLogEntry) {}
    actual fun requestPermission(onResult: (Boolean) -> Unit) {
        onResult(true)
    }
}
