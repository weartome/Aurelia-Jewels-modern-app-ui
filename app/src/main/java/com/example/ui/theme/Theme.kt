package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class AureliaExtendedColors(
    val champagne: Color = SoftChampagne,
    val antiqueGold: Color = MutedAntiqueGold,
    val deepWarmBrown: Color = DeepWarmBrown,
    val secondaryCream: Color = SecondaryCream,
    val hairlineBorder: Color = HairlineBorder,
    val success: Color = SuccessForest
)

val LocalAureliaColors = staticCompositionLocalOf { AureliaExtendedColors() }

val AureliaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun AureliaJewelsTheme(
    primaryColorHex: String = "#27351A",
    accentColorHex: String = "#AD853C",
    content: @Composable () -> Unit
) {
    val primary = parseHexColorOrDefault(primaryColorHex, PrimaryDeepOlive)
    val accent = parseHexColorOrDefault(accentColorHex, MutedAntiqueGold)

    val colorScheme = lightColorScheme(
        primary = primary,
        onPrimary = CardSurface,
        primaryContainer = SoftChampagne,
        onPrimaryContainer = DeepWarmBrown,
        secondary = accent,
        onSecondary = CardSurface,
        secondaryContainer = SecondaryCream,
        onSecondaryContainer = DeepWarmBrown,
        tertiary = DeepWarmBrown,
        onTertiary = CardSurface,
        background = IvoryBackground,
        onBackground = MainText,
        surface = CardSurface,
        onSurface = MainText,
        surfaceVariant = SecondaryCream,
        onSurfaceVariant = SecondaryText,
        outline = HairlineBorder,
        outlineVariant = SoftChampagne,
        error = ErrorCrimson,
        onError = CardSurface
    )

    val extended = AureliaExtendedColors(
        champagne = SoftChampagne,
        antiqueGold = accent,
        deepWarmBrown = DeepWarmBrown,
        secondaryCream = SecondaryCream,
        hairlineBorder = HairlineBorder,
        success = SuccessForest
    )

    CompositionLocalProvider(LocalAureliaColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AureliaTypography,
            shapes = AureliaShapes,
            content = content
        )
    }
}
