package com.ugurbuga.inspecto

expect object InspectoNotification {
    fun initialize()
    fun showServiceCallNotification(entry: InspectoLogEntry)
    fun requestPermission(onResult: (Boolean) -> Unit)
}
