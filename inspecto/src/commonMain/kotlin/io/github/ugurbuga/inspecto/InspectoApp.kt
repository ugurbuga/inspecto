package io.github.ugurbuga.inspecto

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily

@Composable
fun InspectoApp(
    fontFamily: FontFamily? = null,
    content: @Composable () -> Unit
) {
    if (fontFamily != null) {
        Inspecto.customFontFamily = fontFamily
    }
    val isOpen by Inspecto.isInspectoOpen.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (isOpen) {
            InspectoScreen(
                onDismiss = { Inspecto.dismiss() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
