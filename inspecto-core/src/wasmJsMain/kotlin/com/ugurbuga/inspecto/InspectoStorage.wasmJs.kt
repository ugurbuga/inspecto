package com.ugurbuga.inspecto

internal actual object InspectoStorage {
    actual fun saveLogsJson(json: String) {}
    actual fun loadLogsJson(): String? = null
    actual fun clearLogs() {}
}
