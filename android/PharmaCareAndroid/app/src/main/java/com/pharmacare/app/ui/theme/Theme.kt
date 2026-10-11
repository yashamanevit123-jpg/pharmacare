package com.pharmacare.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Mint,
    onPrimary = PineDark,
    primaryContainer = Pine,
    onPrimaryContainer = Mint,
    secondary = MintDark,
    background = BackgroundDark,
    onBackground = InkDark,
    surface = PanelDark,
    onSurface = InkDark,
    outline = LineDark
)

private val LightColorScheme = lightColorScheme(
    primary = Pine,
    onPrimary = PanelLight,
    primaryContainer = Mint,
    onPrimaryContainer = Pine,
    secondary = Mint,
    background = BackgroundLight,
    onBackground = InkLight,
    surface = PanelLight,
    onSurface = InkLight,
    outline = LineLight
)

@Composable
fun PharmaCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
