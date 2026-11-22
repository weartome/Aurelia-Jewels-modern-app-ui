package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Aurelia Jewels — Restrained Luxury Indian Fine Jewellery Palette
val IvoryBackground = Color(0xFFFAF7F2)
val SecondaryCream = Color(0xFFF3EDE5)
val WarmCardTint = Color(0xFFF5EFE7)
val CardSurface = Color(0xFFFFFFFF)
val SoftChampagne = Color(0xFFEADCC9)
val MutedAntiqueGold = Color(0xFFAD853C)
val WarmGoldHighlight = Color(0xFFD4AF6A)
val PrimaryDeepOlive = Color(0xFF27351A)
val DeepOliveDark = Color(0xFF1A2411)
val DeepWarmBrown = Color(0xFF36271F)
val MainText = Color(0xFF211C17)
val SecondaryText = Color(0xFF6B635B)
val HairlineBorder = Color(0xFFE6DDD2)
val ErrorCrimson = Color(0xFFA13B32)
val SuccessForest = Color(0xFF335E3B)

fun parseHexColorOrDefault(hex: String, fallback: Color): Color {
    val cleaned = hex.trim().removePrefix("#")
    if (cleaned.length != 6 && cleaned.length != 8) return fallback
    return try {
        val parsed = cleaned.toLong(16)
        if (cleaned.length == 6) {
            Color((0xFF000000L or parsed).toInt())
        } else {
            Color(parsed.toInt())
        }
    } catch (_: NumberFormatException) {
        fallback
    }
}

fun isValidHexColor(hex: String): Boolean {
    val cleaned = hex.trim().removePrefix("#")
    return cleaned.matches(Regex("^[0-9A-Fa-f]{6}$"))
}
