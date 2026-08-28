package com.helper.heretoosmand.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = BlueContainer,
    onPrimaryContainer = BlueOnContainer,
    secondary = OrangeSecondary,
    onSecondary = OrangeOnSecondary,
    secondaryContainer = OrangeContainer,
    onSecondaryContainer = OrangeOnContainer,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = CardLight,
    onSurface = OnSurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = BlueContainer,
    onPrimaryContainer = BlueOnContainer,
    secondary = OrangeSecondary,
    onSecondary = OrangeOnSecondary,
    secondaryContainer = OrangeContainer,
    onSecondaryContainer = OrangeOnContainer,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = CardDark,
    onSurface = OnSurfaceDark
)

@Composable
fun HereToOsmAndTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
