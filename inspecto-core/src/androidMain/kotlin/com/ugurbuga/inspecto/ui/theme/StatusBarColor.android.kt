package com.ugurbuga.inspecto.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
internal actual fun SetStatusBarIconColor(darkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            LaunchedEffect(darkIcons) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    darkIcons
            }
        }
    }
}
