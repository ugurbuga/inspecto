package com.ugurbuga.inspecto

internal expect object InspectoStorage {
    fun saveLogsJson(json: String)
    fun loadLogsJson(): String?
    fun clearLogs()
}
