package io.github.ugurbuga.inspecto.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

@Composable
internal actual fun SetStatusBarColor(color: Color, darkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            val window = activity?.window
            if (window != null) {
                window.statusBarColor = color.toArgb()
                val insetsController = WindowInsetsControllerCompat(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = darkIcons
            }
        }
    }
}
