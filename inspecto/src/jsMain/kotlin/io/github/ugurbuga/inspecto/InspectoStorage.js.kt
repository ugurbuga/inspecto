package io.github.ugurbuga.inspecto

import kotlinx.browser.window

internal actual object InspectoStorage {
    private const val KEY_LOGS_JSON = "key_inspecto_logs_json"

    actual fun saveLogsJson(json: String) {
        runCatching {
            window.localStorage.setItem(KEY_LOGS_JSON, json)
        }
    }

    actual fun loadLogsJson(): String? {
        return runCatching {
            window.localStorage.getItem(KEY_LOGS_JSON)
        }.getOrNull()
    }

    actual fun clearLogs() {
        runCatching {
            window.localStorage.removeItem(KEY_LOGS_JSON)
        }
    }
}
