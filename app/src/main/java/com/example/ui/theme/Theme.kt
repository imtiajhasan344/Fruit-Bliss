package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FruitBlissColorScheme = lightColorScheme(
    primary = GreenBottom,
    onPrimary = Color.White,
    primaryContainer = OrangeTop,
    onPrimaryContainer = TextBrownDark,
    secondary = StorePurpleMid,
    onSecondary = Color.White,
    secondaryContainer = CardCream,
    onSecondaryContainer = TextBrownDark,
    tertiary = BlueBtnTop,
    onTertiary = Color.White,
    background = SkyHorizonBlue,
    onBackground = TextBrownDark,
    surface = CardCream,
    onSurface = TextBrownDark,
    surfaceVariant = Color(0xFFF3E5CC),
    onSurfaceVariant = TextBrownDark,
    error = RedCloseBottom,
    onError = Color.White
)

@Composable
fun FruitBlissTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FruitBlissColorScheme,
        typography = Typography,
        content = content
    )
}
