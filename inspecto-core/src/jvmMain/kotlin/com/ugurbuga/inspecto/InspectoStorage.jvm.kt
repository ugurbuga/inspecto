package com.ugurbuga.inspecto

import java.io.File

internal actual object InspectoStorage {
    private val storageFile: File by lazy {
        val userHome = System.getProperty("user.home") ?: "."
        val dir = File(userHome, ".inspecto")
        if (!dir.exists()) dir.mkdirs()
        File(dir, "inspecto_logs.json")
    }

    actual fun saveLogsJson(json: String) {
        runCatching {
            storageFile.writeText(json)
        }
    }

    actual fun loadLogsJson(): String? {
        return runCatching {
            if (storageFile.exists()) storageFile.readText() else null
        }.getOrNull()
    }

    actual fun clearLogs() {
        runCatching {
            if (storageFile.exists()) storageFile.delete()
        }
    }
}
