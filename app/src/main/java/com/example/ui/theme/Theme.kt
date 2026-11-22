package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

private val GrammarColorScheme = lightColorScheme(
    primary = DeepBlueBanner,
    onPrimary = OnDeepBlueText,
    primaryContainer = DeepBluePill,
    onPrimaryContainer = OnDeepBlueText,
    secondary = EmeraldDeep,
    onSecondary = OnDeepBlueText,
    secondaryContainer = PlateYellowElevated,
    onSecondaryContainer = DeepBlueInk,
    tertiary = OceanBlue,
    onTertiary = OnDeepBlueText,
    background = OceanBlue,
    onBackground = OnDeepBlueText,
    surface = PlateYellow,
    onSurface = DeepBlueInk,
    surfaceVariant = PlateYellowElevated,
    onSurfaceVariant = DeepBlueSecondaryInk,
    outline = PlateBorderGold,
    error = ErrorCoral,
    onError = OnDeepBlueText
)

val GrammarShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

val GreenBlueBackdropBrush = Brush.verticalGradient(
    colors = listOf(
        EmeraldDeep,
        TealForest,
        OceanBlue,
        DeepCanvasBlue
    )
)

@Composable
fun GrammarCraftTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GrammarColorScheme,
        typography = GrammarTypography,
        shapes = GrammarShapes,
        content = content
    )
}
