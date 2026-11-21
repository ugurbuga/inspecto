package io.github.ugurbuga.inspecto

actual fun shareText(text: String, title: String?) {
    // On JVM/Desktop, default to clipboard or no-op
}
