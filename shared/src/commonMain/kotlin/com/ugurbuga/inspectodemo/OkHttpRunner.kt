package io.github.ugurbuga.inspectodemo

expect fun performOkHttpTestRequest(url: String, onResult: (String) -> Unit)
expect val isOkHttpSupported: Boolean
