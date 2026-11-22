package com.ugurbuga.inspecto

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily

@Composable
fun InspectOverlay(
    fontFamily: FontFamily? = null,
    content: @Composable () -> Unit
) {
    val isOpen by Inspecto.isInspectoOpen.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (isOpen) {
            InspectoScreen(
                onDismiss = { Inspecto.dismiss() },
                fontFamily = fontFamily,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
