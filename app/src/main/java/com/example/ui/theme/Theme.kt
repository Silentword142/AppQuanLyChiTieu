package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. OCEAN THEME (Đại dương Cổ điển)
private val LightOcean = lightColorScheme(
    primary = Color(0xFF1B4965),
    secondary = Color(0xFFB75B53),
    tertiary = Color(0xFF3E7CB1),
    background = Color(0xFFEAF3F9),
    surface = Color(0xFFF9FBFD),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF112233),
    onSurface = Color(0xFF112233),
    surfaceVariant = Color(0xFFD4E5F2),
    onSurfaceVariant = Color(0xFF1B4965),
    outline = Color(0xFFB0CEDA)
)

private val DarkOcean = darkColorScheme(
    primary = Color(0xFF8ED1FC),
    secondary = Color(0xFFFFAEAE),
    tertiary = Color(0xFFB3E5FC),
    background = Color(0xFF09121A),
    surface = Color(0xFF121E2A),
    onPrimary = Color(0xFF09121A),
    onSecondary = Color(0xFF09121A),
    onTertiary = Color(0xFF09121A),
    onBackground = Color(0xFFF0F5FA),
    onSurface = Color(0xFFF0F5FA),
    surfaceVariant = Color(0xFF1A2A38),
    onSurfaceVariant = Color(0xFF8ED1FC),
    outline = Color(0xFF2E4456)
)

// 2. EMERALD THEME (Lục bảo Ngọc)
private val LightEmerald = lightColorScheme(
    primary = Color(0xFF1B5E20),
    secondary = Color(0xFFD84315),
    tertiary = Color(0xFF2E7D32),
    background = Color(0xFFE8F5E9),
    surface = Color(0xFFF9FDF9),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0A220B),
    onSurface = Color(0xFF0A220B),
    surfaceVariant = Color(0xFFC8E6C9),
    onSurfaceVariant = Color(0xFF1B5E20),
    outline = Color(0xFFA5D6A7)
)

private val DarkEmerald = darkColorScheme(
    primary = Color(0xFF69F0AE),
    secondary = Color(0xFFFF8A65),
    tertiary = Color(0xFFB9F6CA),
    background = Color(0xFF051206),
    surface = Color(0xFF0C240E),
    onPrimary = Color(0xFF051206),
    onSecondary = Color(0xFF051206),
    onTertiary = Color(0xFF051206),
    onBackground = Color(0xFFE8F5E9),
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = Color(0xFF143B17),
    onSurfaceVariant = Color(0xFF69F0AE),
    outline = Color(0xFF1B5E20)
)

// 3. TERRACOTTA THEME (Đất nung Ấm áp)
private val LightTerracotta = lightColorScheme(
    primary = Color(0xFFA54B1A),
    secondary = Color(0xFF1D5C5A),
    tertiary = Color(0xFFD87D56),
    background = Color(0xFFFFF8F2),
    surface = Color(0xFFFFFDFB),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF2E1505),
    onSurface = Color(0xFF2E1505),
    surfaceVariant = Color(0xFFFBE9E7),
    onSurfaceVariant = Color(0xFFA54B1A),
    outline = Color(0xFFECA38A)
)

private val DarkTerracotta = darkColorScheme(
    primary = Color(0xFFFFAB91),
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFFFFCCBC),
    background = Color(0xFF140D09),
    surface = Color(0xFF221611),
    onPrimary = Color(0xFF140D09),
    onSecondary = Color(0xFF140D09),
    onTertiary = Color(0xFF140D09),
    onBackground = Color(0xFFFFEBE3),
    onSurface = Color(0xFFFFEBE3),
    surfaceVariant = Color(0xFF332019),
    onSurfaceVariant = Color(0xFFFFAB91),
    outline = Color(0xFF5C382C)
)

// 4. ROYAL VIOLET THEME (Thạch anh Hoàng gia)
private val LightPurple = lightColorScheme(
    primary = Color(0xFF4A148C),
    secondary = Color(0xFF00796B),
    tertiary = Color(0xFF7B1FA2),
    background = Color(0xFFFBF4FC),
    surface = Color(0xFFFFFDFC),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1F003E),
    onSurface = Color(0xFF1F003E),
    surfaceVariant = Color(0xFFF3E5F5),
    onSurfaceVariant = Color(0xFF4A148C),
    outline = Color(0xFFE1BEE7)
)

private val DarkPurple = darkColorScheme(
    primary = Color(0xFFE040FB),
    secondary = Color(0xFF00E676),
    tertiary = Color(0xFFEA80FC),
    background = Color(0xFF12091A),
    surface = Color(0xFF1F122A),
    onPrimary = Color(0xFF12091A),
    onSecondary = Color(0xFF12091A),
    onTertiary = Color(0xFF12091A),
    onBackground = Color(0xFFFBE9FF),
    onSurface = Color(0xFFFBE9FF),
    surfaceVariant = Color(0xFF2F1B3E),
    onSurfaceVariant = Color(0xFFE040FB),
    outline = Color(0xFF5A2A7B)
)

// 5. MONOCHROME MINIMALIST (Đen Trắng Tối giản)
private val LightMinimalist = lightColorScheme(
    primary = Color(0xFF111111),
    secondary = Color(0xFF444444),
    tertiary = Color(0xFF666666),
    background = Color(0xFFFAF9F8),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF111111),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF111111),
    outline = Color(0xFFCCCCCC)
)

private val DarkMinimalist = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    secondary = Color(0xFFCCCCCC),
    tertiary = Color(0xFF888888),
    background = Color(0xFF111111),
    surface = Color(0xFF1A1A1A),
    onPrimary = Color(0xFF111111),
    onSecondary = Color(0xFF111111),
    onTertiary = Color(0xFF111111),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = Color(0xFFFFFFFF),
    outline = Color(0xFF444444)
)

@Composable
fun MyApplicationTheme(
    themeName: String = "ocean",
    themeMode: String = "system",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> darkTheme
    }

    val colorScheme = when (themeName.lowercase()) {
        "emerald" -> if (isDark) DarkEmerald else LightEmerald
        "terracotta" -> if (isDark) DarkTerracotta else LightTerracotta
        "purple" -> if (isDark) DarkPurple else LightPurple
        "minimalist" -> if (isDark) DarkMinimalist else LightMinimalist
        else -> if (isDark) DarkOcean else LightOcean
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
