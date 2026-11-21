package com.ugurbuga.inspecto

import platform.Foundation.NSUserDefaults

internal actual object InspectoStorage {
    private const val KEY_LOGS_JSON = "key_inspecto_logs_json"

    actual fun saveLogsJson(json: String) {
        NSUserDefaults.standardUserDefaults.setObject(json, forKey = KEY_LOGS_JSON)
    }

    actual fun loadLogsJson(): String? {
        return NSUserDefaults.standardUserDefaults.stringForKey(KEY_LOGS_JSON)
    }

    actual fun clearLogs() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_LOGS_JSON)
    }
}
