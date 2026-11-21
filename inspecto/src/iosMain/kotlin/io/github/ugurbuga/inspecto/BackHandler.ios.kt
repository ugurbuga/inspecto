package io.github.ugurbuga.inspecto

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS
}
