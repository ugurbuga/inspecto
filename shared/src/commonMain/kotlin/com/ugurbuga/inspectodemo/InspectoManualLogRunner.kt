package com.ugurbuga.inspectodemo

import com.ugurbuga.inspecto.Inspecto

object InspectoManualLogRunner {
    fun triggerManualLog(): String {
        val currentTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        Inspecto.addLog(
            url = "https://api.inspecto.local/v1/manual-action",
            method = "POST",
            statusCode = 201,
            durationMs = 35L,
            requestHeaders = mapOf("Authorization" to "Bearer secret-token"),
            requestQueryParams = mapOf("source" to "button"),
            requestBody = "{\"action\": \"test\", \"time\": $currentTime}",
            responseBody = "{\"status\": \"success\"}",
            tag = "Manual"
        )
        return "Manual log entry added successfully!"
    }
}
