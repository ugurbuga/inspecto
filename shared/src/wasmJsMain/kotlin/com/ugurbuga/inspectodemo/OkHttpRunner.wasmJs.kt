package io.github.ugurbuga.inspectodemo

actual fun performOkHttpTestRequest(url: String, onResult: (String) -> Unit) {
    onResult("OkHttp is supported on Android and Desktop JVM.")
}

actual val isOkHttpSupported: Boolean = false
