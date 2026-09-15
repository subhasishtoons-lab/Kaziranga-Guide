package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenPrimaryDark,
    onPrimary = ForestGreenOnPrimaryDark,
    primaryContainer = ForestGreenContainerDark,
    onPrimaryContainer = ForestGreenOnContainerDark,
    secondary = AmberSecondaryDark,
    onSecondary = AmberOnSecondaryDark,
    secondaryContainer = AmberSecondaryContainerDark,
    onSecondaryContainer = AmberOnSecondaryContainerDark,
    tertiary = RiverTealTertiaryDark,
    onTertiary = RiverTealOnTertiaryDark,
    tertiaryContainer = RiverTealContainerDark,
    onTertiaryContainer = RiverTealOnContainerDark,
    background = NatureBackgroundDark,
    onBackground = NatureOnBackgroundDark,
    surface = NatureSurfaceDark,
    onSurface = NatureOnSurfaceDark,
    surfaceVariant = NatureSurfaceVariantDark,
    onSurfaceVariant = NatureOnSurfaceVariantDark,
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = ForestGreenOnPrimary,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = ForestGreenOnContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberSecondaryContainer,
    onSecondaryContainer = AmberOnSecondaryContainer,
    tertiary = RiverTealTertiary,
    onTertiary = RiverTealOnTertiary,
    tertiaryContainer = RiverTealContainer,
    onTertiaryContainer = RiverTealOnContainer,
    background = NatureBackground,
    onBackground = NatureOnBackground,
    surface = NatureSurface,
    onSurface = NatureOnSurface,
    surfaceVariant = NatureSurfaceVariant,
    onSurfaceVariant = NatureOnSurfaceVariant,
)

@Composable
fun KazirangaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep bespoke Kaziranga nature branding dominant
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
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

// Keep backward compatibility alias if needed
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = KazirangaTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
