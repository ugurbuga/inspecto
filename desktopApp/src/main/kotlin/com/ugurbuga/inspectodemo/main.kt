package com.ugurbuga.inspectodemo

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ugurbuga.inspectodemo.util.SettingsStorage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
fun main() = application {
    val storage = SettingsStorage()

    // Default size: 720 x 1280 dp
    val savedWidth = storage.getString("window_width", "720").toFloatOrNull()?.dp ?: 720.dp
    val savedHeight = storage.getString("window_height", "1280").toFloatOrNull()?.dp ?: 1280.dp

    val savedX = storage.getString("window_x", "").toFloatOrNull()
    val savedY = storage.getString("window_y", "").toFloatOrNull()

    val initialPosition = if (savedX != null && savedY != null) {
        WindowPosition(savedX.dp, savedY.dp)
    } else {
        WindowPosition.PlatformDefault
    }

    val windowState = rememberWindowState(
        width = savedWidth,
        height = savedHeight,
        position = initialPosition
    )

    // Save window size and position changes
    LaunchedEffect(windowState) {
        snapshotFlow {
            Triple(windowState.size, windowState.position, windowState.isMinimized)
        }
            .filter { (_, _, isMinimized) -> !isMinimized }
            .debounce(300.milliseconds)
            .collect { (size, position, _) ->
                if (size.width > 0.dp && size.height > 0.dp) {
                    storage.putString("window_width", size.width.value.toString())
                    storage.putString("window_height", size.height.value.toString())
                }
                if (position is WindowPosition.Absolute) {
                    storage.putString("window_x", position.x.value.toString())
                    storage.putString("window_y", position.y.value.toString())
                }
            }
    }

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Inspecto Demo",
    ) {
        App()
    }
}
