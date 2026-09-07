package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary PicaComic & MIUIX Accent Colors
val PicaPink = Color(0xFFFF4081)
val PicaPinkDark = Color(0xFFC60055)
val PicaPinkLight = Color(0xFFFF79B0)
val PicaPinkSoft = Color(0x33FF4081)

val MiuixBlue = Color(0xFF007AFF)
val MiuixBlueDark = Color(0xFF0A84FF)
val MiuixCyan = Color(0xFF00C7BE)
val MiuixPurple = Color(0xFF5856D6)
val MiuixOrange = Color(0xFFFF9500)
val MiuixGreen = Color(0xFF34C759)

// Liquid Glass Dark Palette
val DarkLiquidCanvas = Color(0xFF0D0F17)
val DarkLiquidSurface = Color(0xFF161926)
val DarkGlassFill = Color(0x331F2438)
val DarkGlassFillHigh = Color(0x59282E47)
val DarkGlassBorderTop = Color(0x55FFFFFF)
val DarkGlassBorderBottom = Color(0x12FFFFFF)
val DarkGlassGlow = Color(0x1FFF4081)

// Liquid Glass Light Palette
val LightLiquidCanvas = Color(0xFFF4F6FB)
val LightLiquidSurface = Color(0xFFFFFFFF)
val LightGlassFill = Color(0x99FFFFFF)
val LightGlassFillHigh = Color(0xD9FFFFFF)
val LightGlassBorderTop = Color(0xBFFFFFFF)
val LightGlassBorderBottom = Color(0x33D0D7E5)
val LightGlassGlow = Color(0x14FF4081)

// Standard theme colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Liquid Gradient Brushes
val LiquidHeroGradientDark = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E1435),
        Color(0xFF131A2E),
        Color(0xFF0E1C24)
    )
)

val LiquidGlassBorderDark = Brush.verticalGradient(
    colors = listOf(
        DarkGlassBorderTop,
        DarkGlassBorderBottom
    )
)

val LiquidGlassBorderLight = Brush.verticalGradient(
    colors = listOf(
        LightGlassBorderTop,
        LightGlassBorderBottom
    )
)
