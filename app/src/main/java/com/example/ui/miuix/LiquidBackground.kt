package com.example.ui.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.*

/**
 * Ambient Liquid Canvas Background:
 * Renders an ethereal, fluid background with soft glowing radial gradients
 * that refract gracefully through liquid glass surfaces and floating navbar.
 */
@Composable
fun LiquidCanvasBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) DarkLiquidCanvas else LightLiquidCanvas)
            .drawBehind {
                val width = size.width
                val height = size.height

                if (isDark) {
                    // Top-right diffuse pink/magenta orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PicaPink.copy(alpha = 0.20f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.85f, height * 0.12f),
                            radius = width * 0.75f
                        )
                    )

                    // Mid-left violet orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MiuixPurple.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.15f, height * 0.45f),
                            radius = width * 0.65f
                        )
                    )

                    // Bottom-right cyan/blue liquid glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MiuixBlueDark.copy(alpha = 0.22f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.75f, height * 0.88f),
                            radius = width * 0.70f
                        )
                    )
                } else {
                    // Light mode pastel diffuse glows
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PicaPink.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.85f, height * 0.12f),
                            radius = width * 0.65f
                        )
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MiuixBlue.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.15f, height * 0.50f),
                            radius = width * 0.70f
                        )
                    )
                }
            },
        content = content
    )
}

/**
 * Comic Cover Image with Liquid Glass corner clipping and placeholder
 */
@Composable
fun ComicCoverImage(
    coverUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (isDark) Color(0xFF22283A) else Color(0xFFE2E8F0)),
        contentAlignment = Alignment.Center
    ) {
        // Fallback icon behind the image
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
            modifier = Modifier.size(36.dp)
        )

        AsyncImage(
            model = coverUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
