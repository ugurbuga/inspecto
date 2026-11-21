package com.ugurbuga.inspecto.ui.theme

import androidx.compose.ui.graphics.Color

object InspectoColors {
    // Status Colors (Green, Amber, Red)
    val StatusSuccess2xx = Color(0xFF10B981) // Emerald Green
    val StatusRedirect3xx = Color(0xFFF59E0B) // Amber Orange
    val StatusError4xx5xx = Color(0xFFEF4444) // Rose Red

    // Method Colors (Completely distinct from Green, Amber, Red)
    val MethodGet = Color(0xFF0284C7)    // Sky Blue
    val MethodPost = Color(0xFF0EA5E9)   // Deep Cyan / Electric Blue
    val MethodPut = Color(0xFF8B5CF6)    // Violet / Purple
    val MethodDelete = Color(0xFFEC4899) // Magenta / Pink
    val MethodPatch = Color(0xFF6366F1)  // Indigo
    val MethodOptions = Color(0xFF475569)// Slate Gray
    val MethodHead = Color(0xFF334155)   // Dark Slate

    val TagKtor = Color(0xFF8B5CF6)
    val TagOkHttp = Color(0xFFF59E0B)
    val TagGraphQL = Color(0xFFEC4899)
    val TagNSURLSession = Color(0xFF10B981)
    val TagManual = Color(0xFF0EA5E9)

    val ActionTheme = Color(0xFFFBBF24)
    val ActionThemeLight = Color(0xFFD97706)
    val ActionClear = Color(0xFFEF4444)
    val ActionClearLight = Color(0xFFE11D48)
    val ActionClearContainer = Color(0xFF7F1D1D)
    val ActionClearContainerLight = Color(0xFFFEF2F2)
    val ActionFilter = Color(0xFF3B82F6)
    val ActionFilterDark = Color(0xFF60A5FA)
    val ActionSort = Color(0xFF06B6D4)
    val ActionSortDark = Color(0xFF34D399)
}
