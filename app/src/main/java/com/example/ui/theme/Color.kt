package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme

// --- BASE DEFAULTS & HARDCODED BACKUPS ---
val VintageSageGreen = Color(0xFF1B4965)       // Deep Ocean Blue
val VintageDuskyRose = Color(0xFFB75B53)       // Warm Deep Sunset Rose
val VintagePastelBlue = Color(0xFF3E7CB1)      // Elegant Slate Sky Blue
val VintageWarmPaper = Color(0xFFEAF3F9)       // Soothing Light Pastel Blue Background Paper
val VintageIvoryCard = Color(0xFFF6FAF2)       // Clean Soft Pastel-Ice Off-White Cards
val VintageWoodDark = Color(0xFF112233)         // Solid Deep Navy Ink
val VintageWoodMedium = Color(0xFF2E4456)       // Readable Midnight Slate
val VintageWoodLight = Color(0xFF5D7A94)        // Soothing Steel Blue
val VintageBorder = Color(0xFFC7DAE6)          // Clear soft blue separator bounds
val VintageTonalVariant = Color(0xFFD4E5F2)     // Warm-tinted Pastel Blue Container Backdrops

// --- DYNAMIC COMPATIBILITY / SEMANTIC MAPPINGS ---
// These are now evaluated as Composable getters mapping to the selected theme dynamically.
val SleekDeepPurple: Color
    @Composable get() = MaterialTheme.colorScheme.primary

val SleekRoyalPurple: Color
    @Composable get() = MaterialTheme.colorScheme.primary

val SleekLightLavender: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant

val SleekHighlightLavender: Color
    @Composable get() = MaterialTheme.colorScheme.secondary

val SleekBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background

val SleekCardBg: Color
    @Composable get() = MaterialTheme.colorScheme.surface

val SleekSurfaceVariant: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant

val SleekTextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground

val SleekTextSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)

val SleekBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outline

val EmeraldPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.primary

val EmeraldSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.secondary

val TextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)

// --- HIGH-CONTRAST RETRO-PASTEL STATUS SIGNALS ---
val ExpenseRed = Color(0xFFD32F2F)             // High Contrast Crimson Red for expenses
val IncomeGreen = Color(0xFF2E7D32)            // High Contrast Forest Green for income
val AccentBlue = Color(0xFF1976D2)             // Vivid Slate Blue
val AccentOrange = Color(0xFFE64A19)           // Terracotta Orange
val AccentYellow = Color(0xFFFBC02D)           // High Contrast Golden Amber Honey
val AccentPurple = Color(0xFF7B1FA2)           // Deep Lavender-Plum Orchid
