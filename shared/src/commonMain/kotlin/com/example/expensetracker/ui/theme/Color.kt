package com.example.expensetracker.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Category colors
val CategoryFood = Color(0xFFE74C3C)
val CategoryTransport = Color(0xFF3498DB)
val CategoryShopping = Color(0xFF9B59B6)
val CategoryEntertainment = Color(0xFFF39C12)
val CategoryHousing = Color(0xFF2ECC71)
val CategoryUtilities = Color(0xFF1ABC9C)
val CategoryHealthcare = Color(0xFFE91E63)
val CategoryEducation = Color(0xFF00BCD4)
val CategoryOther = Color(0xFF95A5A6)

data class ThemePreset(
    val name: String,
    val primary: Color,
    val onPrimary: Color = Color.White,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color
)

val themePresets = listOf(
    ThemePreset(
        name = "暖橙",
        primary = Color(0xFFE67E22),
        primaryContainer = Color(0xFFFFDCC0),
        onPrimaryContainer = Color(0xFF311300),
        secondary = Color(0xFF755845),
        secondaryContainer = Color(0xFFFFDCC0),
        onSecondaryContainer = Color(0xFF2B1700),
        tertiary = Color(0xFF656039),
        tertiaryContainer = Color(0xFFEDE4B8),
        onTertiaryContainer = Color(0xFF201D00),
        surfaceVariant = Color(0xFFF3DFD1),
        onSurfaceVariant = Color(0xFF52443B),
        outline = Color(0xFF857369)
    ),
    ThemePreset(
        name = "海蓝",
        primary = Color(0xFF1976D2),
        primaryContainer = Color(0xFFD1E4FF),
        onPrimaryContainer = Color(0xFF001D36),
        secondary = Color(0xFF535F70),
        secondaryContainer = Color(0xFFD7E3F7),
        onSecondaryContainer = Color(0xFF101C2B),
        tertiary = Color(0xFF0D82B0),
        tertiaryContainer = Color(0xFFC2E8FF),
        onTertiaryContainer = Color(0xFF001E2C),
        surfaceVariant = Color(0xFFDFE2EB),
        onSurfaceVariant = Color(0xFF43474E),
        outline = Color(0xFF73777F)
    ),
    ThemePreset(
        name = "青绿",
        primary = Color(0xFF2E7D32),
        primaryContainer = Color(0xFFC8E6C9),
        onPrimaryContainer = Color(0xFF002106),
        secondary = Color(0xFF526350),
        secondaryContainer = Color(0xFFD5E8D1),
        onSecondaryContainer = Color(0xFF111F10),
        tertiary = Color(0xFF39656C),
        tertiaryContainer = Color(0xFFBCEBF3),
        onTertiaryContainer = Color(0xFF001F24),
        surfaceVariant = Color(0xFFDDE5D9),
        onSurfaceVariant = Color(0xFF424940),
        outline = Color(0xFF72796F)
    ),
    ThemePreset(
        name = "绛紫",
        primary = Color(0xFF7B1FA2),
        primaryContainer = Color(0xFFE8D5F5),
        onPrimaryContainer = Color(0xFF2D004F),
        secondary = Color(0xFF635970),
        secondaryContainer = Color(0xFFE9DDF8),
        onSecondaryContainer = Color(0xFF1F172A),
        tertiary = Color(0xFF7D5260),
        tertiaryContainer = Color(0xFFFFD9E3),
        onTertiaryContainer = Color(0xFF31101D),
        surfaceVariant = Color(0xFFECDFEA),
        onSurfaceVariant = Color(0xFF4C444E),
        outline = Color(0xFF7E747F)
    ),
    ThemePreset(
        name = "桃粉",
        primary = Color(0xFFE91E63),
        primaryContainer = Color(0xFFFFD9E3),
        onPrimaryContainer = Color(0xFF3F001A),
        secondary = Color(0xFF735761),
        secondaryContainer = Color(0xFFFFD9E3),
        onSecondaryContainer = Color(0xFF2A161F),
        tertiary = Color(0xFF815343),
        tertiaryContainer = Color(0xFFFFDBD1),
        onTertiaryContainer = Color(0xFF321208),
        surfaceVariant = Color(0xFFF3DDE3),
        onSurfaceVariant = Color(0xFF524348),
        outline = Color(0xFF857379)
    ),
    ThemePreset(
        name = "石墨",
        primary = Color(0xFF546E7A),
        primaryContainer = Color(0xFFCFD8DC),
        onPrimaryContainer = Color(0xFF0D1F29),
        secondary = Color(0xFF546E7A),
        secondaryContainer = Color(0xFFCFD8DC),
        onSecondaryContainer = Color(0xFF0D1F29),
        tertiary = Color(0xFF5D7B7F),
        tertiaryContainer = Color(0xFFB1D0D4),
        onTertiaryContainer = Color(0xFF0D1F29),
        surfaceVariant = Color(0xFFDBE4E8),
        onSurfaceVariant = Color(0xFF3F484C),
        outline = Color(0xFF6F797D)
    )
)

fun themePresetToColorScheme(preset: ThemePreset) = lightColorScheme(
    primary = preset.primary,
    onPrimary = Color.White,
    primaryContainer = preset.primaryContainer,
    onPrimaryContainer = preset.onPrimaryContainer,
    secondary = preset.secondary,
    onSecondary = Color.White,
    secondaryContainer = preset.secondaryContainer,
    onSecondaryContainer = preset.onSecondaryContainer,
    tertiary = preset.tertiary,
    onTertiary = Color.White,
    tertiaryContainer = preset.tertiaryContainer,
    onTertiaryContainer = preset.onTertiaryContainer,
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1F1B16),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = preset.surfaceVariant,
    onSurfaceVariant = preset.onSurfaceVariant,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    outline = preset.outline
)
