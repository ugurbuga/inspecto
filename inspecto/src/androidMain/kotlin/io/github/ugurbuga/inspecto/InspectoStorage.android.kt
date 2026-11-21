package io.github.ugurbuga.inspecto

import android.content.Context

internal actual object InspectoStorage {
    private const val PREFS_NAME = "inspecto_storage_prefs"
    private const val KEY_LOGS_JSON = "key_inspecto_logs_json"

    private fun getPrefs() = InspectoContextHolder.applicationContext
        ?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    actual fun saveLogsJson(json: String) {
        getPrefs()?.edit()?.putString(KEY_LOGS_JSON, json)?.apply()
    }

    actual fun loadLogsJson(): String? {
        return getPrefs()?.getString(KEY_LOGS_JSON, null)
    }

    actual fun clearLogs() {
        getPrefs()?.edit()?.remove(KEY_LOGS_JSON)?.apply()
    }
}
