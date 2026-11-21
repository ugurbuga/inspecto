package io.github.ugurbuga.inspecto

internal expect object InspectoStorage {
    fun saveLogsJson(json: String)
    fun loadLogsJson(): String?
    fun clearLogs()
}
