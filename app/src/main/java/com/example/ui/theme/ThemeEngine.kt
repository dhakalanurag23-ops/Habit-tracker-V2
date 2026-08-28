package com.example.ui.theme

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1. Design Aesthetics (Visual Language)
 * Independent of layout density.
 */
enum class DesignAesthetic(
    val title: String,
    val description: String,
    val aliasName: String
) {
    NOTHING_UI(
        title = "Nothing UI",
        description = "Monochromatic, dot-matrix monospace, stark industrial lines & red LED accents",
        aliasName = "MainActivityAliasNothing"
    ),
    LIQUID_GLASS(
        title = "Liquid Glass",
        description = "Translucent frosted glass, fluid glows & ambient depth",
        aliasName = "MainActivityAliasLiquid"
    ),
    MATERIAL_YOU(
        title = "Material You",
        description = "Dynamic expressive tonal palettes & adaptive M3 rounded pills",
        aliasName = "MainActivityAliasMaterial"
    ),
    SKEUOMORPHIC(
        title = "Skeuomorphic",
        description = "Tactile physical depth, realistic bevels, inner shadows & brushed emboss",
        aliasName = "MainActivityAliasMaximal"
    )
}

/**
 * 2. Layout Density (Information Architecture)
 * Independent of visual aesthetic.
 */
enum class LayoutDensity(
    val title: String,
    val description: String,
    val itemSpacing: Dp,
    val cardPadding: Dp,
    val cornerRadius: Dp
) {
    MINIMALISM(
        title = "Minimalism",
        description = "Generous whitespace, hidden clutter, laser-focused single elements & relaxed spacing",
        itemSpacing = 20.dp,
        cardPadding = 18.dp,
        cornerRadius = 24.dp
    ),
    MAXIMALISM(
        title = "Maximalism",
        description = "Balanced rich telemetry, full data badges, active stats dashboard & bold visual energy",
        itemSpacing = 12.dp,
        cardPadding = 14.dp,
        cornerRadius = 16.dp
    ),
    CLUTTERED(
        title = "Cluttered",
        description = "Ultra-dense retro internet chaotic layout, stamp badges & sticker tags",
        itemSpacing = 6.dp,
        cardPadding = 8.dp,
        cornerRadius = 8.dp
    )
}

/**
 * Legacy compatibility preset mapping for Launcher Icon Switcher
 */
enum class AppThemePreset(
    val title: String,
    val description: String,
    val aliasName: String
) {
    LIQUID_GLASS("Liquid Glass", "Frosted glassmorphism, fluid glows & ambient depth", "MainActivityAliasLiquid"),
    NOTHING_UI("Nothing UI", "Dot-matrix styling, monochrome & signature red accents", "MainActivityAliasNothing"),
    MATERIAL_YOU("Material You", "Dynamic expressive tonal palettes & adaptive M3 pills", "MainActivityAliasMaterial"),
    MINIMALISM("Minimalism", "Monochrome clarity, razor-thin outlines & clean focus", "MainActivityAliasMinimal"),
    MAXIMALISM("Maximalism", "Hyper-vibrant neon gradients & playful bold contrast", "MainActivityAliasMaximal");

    fun toAesthetic(): DesignAesthetic = when (this) {
        NOTHING_UI -> DesignAesthetic.NOTHING_UI
        LIQUID_GLASS -> DesignAesthetic.LIQUID_GLASS
        MATERIAL_YOU -> DesignAesthetic.MATERIAL_YOU
        MINIMALISM -> DesignAesthetic.NOTHING_UI
        MAXIMALISM -> DesignAesthetic.SKEUOMORPHIC
    }
}

/**
 * Customizable Google AI-style Accent Colors
 */
enum class GlowAccentColor(
    val title: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
) {
    CYAN_PULSE(
        title = "Electric Cyan",
        primary = Color(0xFF06B6D4),
        secondary = Color(0xFF3B82F6),
        tertiary = Color(0xFF8B5CF6)
    ),
    GEMINI_AURA(
        title = "Google AI Gemini",
        primary = Color(0xFF4285F4),
        secondary = Color(0xFF9B51E0),
        tertiary = Color(0xFFEA4335)
    ),
    EMERALD_FLOW(
        title = "Emerald Zen",
        primary = Color(0xFF10B981),
        secondary = Color(0xFF06B6D4),
        tertiary = Color(0xFF34D399)
    ),
    NOTHING_RED(
        title = "Glyph Red",
        primary = Color(0xFFE11D48),
        secondary = Color(0xFFFF4757),
        tertiary = Color(0xFFFFFFFF)
    ),
    AMBER_SUN(
        title = "Solar Amber",
        primary = Color(0xFFF59E0B),
        secondary = Color(0xFFEC4899),
        tertiary = Color(0xFFEF4444)
    ),
    VIOLET_DREAM(
        title = "Cyber Neon",
        primary = Color(0xFF8B5CF6),
        secondary = Color(0xFFEC4899),
        tertiary = Color(0xFF06B6D4)
    )
}

/**
 * Decoupled Theme Configuration data passed via CompositionLocal
 */
data class HabitPulseThemeConfig(
    val aesthetic: DesignAesthetic = DesignAesthetic.LIQUID_GLASS,
    val density: LayoutDensity = LayoutDensity.MAXIMALISM,
    val glowAccent: GlowAccentColor = GlowAccentColor.CYAN_PULSE,
    val isGlowEnabled: Boolean = true
) {
    // Helper property for backward compatibility
    val preset: AppThemePreset
        get() = when (aesthetic) {
            DesignAesthetic.NOTHING_UI -> AppThemePreset.NOTHING_UI
            DesignAesthetic.LIQUID_GLASS -> AppThemePreset.LIQUID_GLASS
            DesignAesthetic.MATERIAL_YOU -> AppThemePreset.MATERIAL_YOU
            DesignAesthetic.SKEUOMORPHIC -> AppThemePreset.MAXIMALISM
        }
}

val LocalThemeConfig = compositionLocalOf { HabitPulseThemeConfig() }

/**
 * Modifier for Google AI-style Dynamic Animated Border Glow
 */
fun Modifier.googleAiGlow(
    glowAccent: GlowAccentColor,
    enabled: Boolean = true,
    cornerRadius: Dp = 20.dp,
    strokeWidth: Dp = 2.dp
): Modifier = composed {
    if (!enabled) {
        return@composed this.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            shape = RoundedCornerShape(cornerRadius)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ai_glow_anim")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ai_glow_angle"
    )

    this.drawBehind {
        val rad = Math.toRadians(angle.toDouble())
        val cosVal = cos(rad).toFloat()
        val sinVal = sin(rad).toFloat()

        val centerX = size.width / 2
        val centerY = size.height / 2

        val startOffset = Offset(
            x = centerX + cosVal * (size.width / 2),
            y = centerY + sinVal * (size.height / 2)
        )
        val endOffset = Offset(
            x = centerX - cosVal * (size.width / 2),
            y = centerY - sinVal * (size.height / 2)
        )

        val brush = Brush.linearGradient(
            colors = listOf(
                glowAccent.primary,
                glowAccent.secondary,
                glowAccent.tertiary,
                glowAccent.primary
            ),
            start = startOffset,
            end = endOffset
        )

        drawRoundRect(
            brush = brush,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}

/**
 * Modifier for Liquid Glass container styling
 */
fun Modifier.liquidGlassContainer(
    glowAccent: GlowAccentColor,
    cornerRadius: Dp = 20.dp,
    hasGlow: Boolean = true
): Modifier = composed {
    val shape = RoundedCornerShape(cornerRadius)
    this
        .shadow(
            elevation = 8.dp,
            shape = shape,
            ambientColor = glowAccent.primary.copy(alpha = 0.25f),
            spotColor = glowAccent.secondary.copy(alpha = 0.25f)
        )
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1E293B).copy(alpha = 0.82f),
                    Color(0xFF0F172A).copy(alpha = 0.92f)
                )
            ),
            shape = shape
        )
        .googleAiGlow(
            glowAccent = glowAccent,
            enabled = hasGlow,
            cornerRadius = cornerRadius,
            strokeWidth = 1.5.dp
        )
        .clip(shape)
}

/**
 * Modifier for Nothing UI Dot-Matrix container styling
 */
fun Modifier.nothingMatrixContainer(
    cornerRadius: Dp = 8.dp
): Modifier = composed {
    val shape = RoundedCornerShape(cornerRadius)
    this
        .background(Color(0xFF0A0A0A), shape = shape)
        .border(
            BorderStroke(1.dp, Color(0xFF2E2E2E)),
            shape = shape
        )
        .clip(shape)
}

/**
 * Modifier for Skeuomorphic container styling (depth, tactile bevel & emboss)
 */
fun Modifier.skeuomorphicContainer(
    glowAccent: GlowAccentColor,
    cornerRadius: Dp = 16.dp
): Modifier = composed {
    val shape = RoundedCornerShape(cornerRadius)
    this
        .shadow(
            elevation = 6.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.6f),
            spotColor = Color.Black.copy(alpha = 0.8f)
        )
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2A2D34),
                    Color(0xFF181A1F)
                )
            ),
            shape = shape
        )
        .border(
            BorderStroke(
                1.5.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF555B68),
                        Color(0xFF1F2229)
                    )
                )
            ),
            shape = shape
        )
        .clip(shape)
}

/**
 * Modifier for Material You container styling
 */
fun Modifier.materialYouContainer(
    cornerRadius: Dp = 20.dp
): Modifier = composed {
    val shape = RoundedCornerShape(cornerRadius)
    this
        .background(MaterialTheme.colorScheme.surfaceVariant, shape = shape)
        .border(
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            shape = shape
        )
        .clip(shape)
}

/**
 * Adaptive Composite Container Modifier respecting both Aesthetic & Density
 */
fun Modifier.aestheticContainer(
    aesthetic: DesignAesthetic,
    glowAccent: GlowAccentColor,
    hasGlow: Boolean = false,
    cornerRadius: Dp = 16.dp
): Modifier = composed {
    when (aesthetic) {
        DesignAesthetic.LIQUID_GLASS -> Modifier.liquidGlassContainer(glowAccent, cornerRadius, hasGlow)
        DesignAesthetic.NOTHING_UI -> Modifier.nothingMatrixContainer(cornerRadius)
        DesignAesthetic.SKEUOMORPHIC -> Modifier.skeuomorphicContainer(glowAccent, cornerRadius)
        DesignAesthetic.MATERIAL_YOU -> Modifier.materialYouContainer(cornerRadius)
    }
}

/**
 * Theme Builder returning appropriate M3 ColorScheme based on selected aesthetic
 */
fun getAestheticColorScheme(aesthetic: DesignAesthetic, isDark: Boolean): ColorScheme {
    return when (aesthetic) {
        DesignAesthetic.LIQUID_GLASS -> darkColorScheme(
            primary = Color(0xFF38BDF8),
            onPrimary = Color(0xFF082F49),
            primaryContainer = Color(0xFF0C4A6E),
            onPrimaryContainer = Color(0xFFE0F2FE),
            secondary = Color(0xFFA855F7),
            onSecondary = Color(0xFF3B0764),
            secondaryContainer = Color(0xFF581C87),
            onSecondaryContainer = Color(0xFFF3E8FF),
            tertiary = Color(0xFF34D399),
            background = Color(0xFF0B0F17),
            onBackground = Color(0xFFF8FAFC),
            surface = Color(0xFF131B2A),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF1E293B),
            onSurfaceVariant = Color(0xFFCBD5E1),
            outline = Color(0xFF334155),
            outlineVariant = Color(0xFF1E293B)
        )
        DesignAesthetic.NOTHING_UI -> darkColorScheme(
            primary = Color(0xFFE11D48), // Signature Nothing red
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF262626),
            onPrimaryContainer = Color(0xFFFAFAFA),
            secondary = Color(0xFFE5E5E5),
            onSecondary = Color(0xFF000000),
            secondaryContainer = Color(0xFF171717),
            onSecondaryContainer = Color(0xFFE5E5E5),
            tertiary = Color(0xFFEF4444),
            background = Color(0xFF000000), // Pitch black OLED
            onBackground = Color(0xFFFAFAFA),
            surface = Color(0xFF0A0A0A),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF171717),
            onSurfaceVariant = Color(0xFFA3A3A3),
            outline = Color(0xFF404040),
            outlineVariant = Color(0xFF262626)
        )
        DesignAesthetic.MATERIAL_YOU -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFFA8C7FA),
                onPrimary = Color(0xFF042F6B),
                primaryContainer = Color(0xFF0842A0),
                onPrimaryContainer = Color(0xFFD3E3FD),
                secondary = Color(0xFF7FCFFF),
                onSecondary = Color(0xFF00344F),
                secondaryContainer = Color(0xFF004D73),
                onSecondaryContainer = Color(0xFFC2E7FF),
                tertiary = Color(0xFF6DD58C),
                background = Color(0xFF111318),
                onBackground = Color(0xFFE2E2E9),
                surface = Color(0xFF1A1C20),
                onSurface = Color(0xFFE2E2E9),
                surfaceVariant = Color(0xFF44474F),
                onSurfaceVariant = Color(0xFFC4C7D0),
                outline = Color(0xFF8E9099)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF0B57D0),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFD3E3FD),
                onPrimaryContainer = Color(0xFF041E49),
                secondary = Color(0xFF00639B),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFC2E7FF),
                onSecondaryContainer = Color(0xFF001D32),
                tertiary = Color(0xFF146C2E),
                background = Color(0xFFF8F9FF),
                onBackground = Color(0xFF191C20),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF191C20),
                surfaceVariant = Color(0xFFE1E2EC),
                onSurfaceVariant = Color(0xFF44474F),
                outline = Color(0xFF74777F)
            )
        }
        DesignAesthetic.SKEUOMORPHIC -> darkColorScheme(
            primary = Color(0xFFF59E0B), // Warm tactile Amber
            onPrimary = Color(0xFF1E1402),
            primaryContainer = Color(0xFF78350F),
            onPrimaryContainer = Color(0xFFFEF3C7),
            secondary = Color(0xFF94A3B8),
            onSecondary = Color(0xFF0F172A),
            secondaryContainer = Color(0xFF334155),
            onSecondaryContainer = Color(0xFFF1F5F9),
            tertiary = Color(0xFF10B981),
            background = Color(0xFF121316),
            onBackground = Color(0xFFEDEDED),
            surface = Color(0xFF1E2026),
            onSurface = Color(0xFFEDEDED),
            surfaceVariant = Color(0xFF2A2D36),
            onSurfaceVariant = Color(0xFF9CA3AF),
            outline = Color(0xFF4B5563),
            outlineVariant = Color(0xFF374151)
        )
    }
}

/**
 * Custom Typography builder
 */
fun getAestheticTypography(aesthetic: DesignAesthetic): Typography {
    val font = when (aesthetic) {
        DesignAesthetic.NOTHING_UI -> FontFamily.Monospace
        DesignAesthetic.SKEUOMORPHIC -> FontFamily.SansSerif
        else -> FontFamily.Default
    }
    return Typography(
        headlineLarge = TextStyle(
            fontFamily = font,
            fontWeight = if (aesthetic == DesignAesthetic.NOTHING_UI) FontWeight.Medium else FontWeight.Bold,
            fontSize = 30.sp,
            letterSpacing = if (aesthetic == DesignAesthetic.NOTHING_UI) 1.5.sp else 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = font,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            letterSpacing = if (aesthetic == DesignAesthetic.NOTHING_UI) 1.2.sp else 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = font,
            fontWeight = FontWeight.SemiBold,
            fontSize = 19.sp,
            letterSpacing = if (aesthetic == DesignAesthetic.NOTHING_UI) 1.sp else 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = font,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            letterSpacing = if (aesthetic == DesignAesthetic.NOTHING_UI) 0.8.sp else 0.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = font,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = font,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp
        ),
        labelLarge = TextStyle(
            fontFamily = font,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = if (aesthetic == DesignAesthetic.NOTHING_UI) 1.2.sp else 0.5.sp
        )
    )
}

/**
 * Main Dynamic Theme Composable Wrapper
 */
@Composable
fun HabitPulseTheme(
    config: HabitPulseThemeConfig,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = remember(config.aesthetic, darkTheme) {
        getAestheticColorScheme(config.aesthetic, darkTheme)
    }
    val typography = remember(config.aesthetic) {
        getAestheticTypography(config.aesthetic)
    }
    val shapes = remember(config.density, config.aesthetic) {
        val corner = config.density.cornerRadius
        Shapes(
            small = RoundedCornerShape(maxOf(4.dp, corner / 3)),
            medium = RoundedCornerShape(corner),
            large = RoundedCornerShape(corner * 1.5f)
        )
    }

    CompositionLocalProvider(LocalThemeConfig provides config) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}
