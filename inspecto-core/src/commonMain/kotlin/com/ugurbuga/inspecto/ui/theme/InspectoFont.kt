package com.ugurbuga.inspecto.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily

internal fun getInspectoFontFamily(): FontFamily {
    return InspectoFontManager.fontFamily ?: FontFamily.Default
}

val LocalInspectoFontFamily = compositionLocalOf { getInspectoFontFamily() }

object InspectoFontManager {
    var fontFamily: FontFamily? = null
}
