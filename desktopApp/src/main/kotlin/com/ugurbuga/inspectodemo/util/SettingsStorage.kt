package com.ugurbuga.inspectodemo.util

import java.io.File
import java.util.Properties

class SettingsStorage {
    private val properties = Properties()
    private val file = File(System.getProperty("user.home"), ".inspecto_demo_settings.properties")

    init {
        if (file.exists()) {
            try {
                file.inputStream().use { properties.load(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getString(key: String, defaultValue: String): String {
        return properties.getProperty(key, defaultValue)
    }

    fun putString(key: String, value: String) {
        properties.setProperty(key, value)
        try {
            file.outputStream().use { properties.store(it, "Inspecto Demo Settings") }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
