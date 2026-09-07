package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PicaPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5E0025),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = MiuixBlueDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF00325A),
    onSecondaryContainer = Color(0xFFD1E4FF),
    tertiary = MiuixCyan,
    background = DarkLiquidCanvas,
    onBackground = Color(0xFFECEFF5),
    surface = DarkLiquidSurface,
    onSurface = Color(0xFFECEFF5),
    surfaceVariant = Color(0xFF1E2235),
    onSurfaceVariant = Color(0xFFB0B7C6),
    outline = Color(0xFF3B4158),
    outlineVariant = Color(0xFF262C40)
)

private val LightColorScheme = lightColorScheme(
    primary = PicaPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3E0017),
    secondary = MiuixBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E4FF),
    onSecondaryContainer = Color(0xFF001D36),
    tertiary = MiuixCyan,
    background = LightLiquidCanvas,
    onBackground = Color(0xFF151821),
    surface = LightLiquidSurface,
    onSurface = Color(0xFF151821),
    surfaceVariant = Color(0xFFE8EDF5),
    onSurfaceVariant = Color(0xFF5A6273),
    outline = Color(0xFFD4DAE6),
    outlineVariant = Color(0xFFE4E9F2)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default to false to preserve curated PicaComic liquid glass aesthetic
    content: @Composable () -> Unit
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
