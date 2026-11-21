package com.ugurbuga.inspecto

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object Inspecto {
    var customFontFamily: androidx.compose.ui.text.font.FontFamily? = null

    private const val MAX_LOG_CAPACITY = 100
    private val jsonFormatter = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val storageJsonFormatter = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _logs = MutableStateFlow<List<InspectoLogEntry>>(emptyList())
    val logs: StateFlow<List<InspectoLogEntry>> = _logs.asStateFlow()

    private val _isInspectoOpen = MutableStateFlow(false)
    val isInspectoOpen: StateFlow<Boolean> = _isInspectoOpen.asStateFlow()

    internal var onLaunchCallback: (() -> Unit)? = null

    init {
        loadPersistedLogs()
    }

    fun launch(context: Any? = null) {
        launchInspecto(context)
    }

    fun dismiss() {
        _isInspectoOpen.value = false
    }

    internal fun setOpen(open: Boolean) {
        _isInspectoOpen.value = open
    }

    internal fun triggerLaunchCallback() {
        onLaunchCallback?.invoke()
    }

    fun requestNotificationPermission(onResult: (Boolean) -> Unit = {}) {
        InspectoNotification.requestPermission(onResult)
    }

    fun addLog(entry: InspectoLogEntry) {
        val updated = (_logs.value.toMutableList()).apply {
            add(0, entry)
            if (size > MAX_LOG_CAPACITY) {
                removeAt(size - 1)
            }
        }
        _logs.value = updated
        persistLogs()
        runCatching {
            InspectoNotification.showServiceCallNotification(entry)
        }
    }

    fun addLog(
        url: String,
        method: String,
        statusCode: Int,
        durationMs: Long,
        requestHeaders: Map<String, String> = emptyMap(),
        requestQueryParams: Map<String, String> = emptyMap(),
        requestBody: String? = null,
        responseHeaders: Map<String, String> = emptyMap(),
        responseBody: String? = null,
        tag: String = InspectoTag.DEFAULT
    ) {
        val entry = InspectoLogEntry(
            id = generateUniqueId(),
            url = url,
            method = method,
            statusCode = statusCode,
            durationMs = durationMs,
            timestampMs = currentEpochMillis(),
            requestHeaders = requestHeaders,
            requestQueryParams = requestQueryParams,
            requestBody = requestBody,
            responseHeaders = responseHeaders,
            responseBody = responseBody,
            responseSizeByte = responseBody?.length?.toLong() ?: 0L,
            tag = tag
        )
        addLog(entry)
    }

    fun clear() {
        _logs.value = emptyList()
        runCatching {
            InspectoStorage.clearLogs()
        }
    }

    fun formatJson(raw: String): String {
        if (raw.isBlank()) return ""
        return runCatching {
            val element = jsonFormatter.parseToJsonElement(raw)
            jsonFormatter.encodeToString(JsonElement.serializer(), element)
        }.getOrDefault(raw)
    }

    private fun loadPersistedLogs() {
        runCatching {
            val json = InspectoStorage.loadLogsJson()
            if (!json.isNullOrBlank()) {
                val loaded = storageJsonFormatter.decodeFromString<List<InspectoLogEntry>>(json)
                _logs.value = loaded.take(MAX_LOG_CAPACITY)
            }
        }
    }

    private fun persistLogs() {
        runCatching {
            val json = storageJsonFormatter.encodeToString(_logs.value)
            InspectoStorage.saveLogsJson(json)
        }
    }

    private fun generateUniqueId(): String {
        return currentEpochMillis().toString() + "-" + (1000..9999).random()
    }

    private fun currentEpochMillis(): Long {
        return kotlin.time.Clock.System.now().toEpochMilliseconds()
    }
}
