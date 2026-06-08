package fr.epf.sni2.velib_android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = VelibBlue,
    onPrimary = Color.White,
    primaryContainer = VelibBlueLight,
    onPrimaryContainer = VelibBlueDark,

    secondary = Color(0xFF4E7E94),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2E6EF),
    onSecondaryContainer = Color(0xFF0C3140),

    tertiary = VelibGreen,
    onTertiary = Color.White,
    tertiaryContainer = VelibGreenLight,
    onTertiaryContainer = VelibGreenDark,

    background = VelibBackground,
    onBackground = Color(0xFF161D21),
    surface = VelibSurface,
    onSurface = Color(0xFF161D21),
    surfaceVariant = VelibSurfaceVariant,
    onSurfaceVariant = Color(0xFF42525A),
    outline = VelibOutline,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FCBF2),
    onPrimary = Color(0xFF003549),
    primaryContainer = Color(0xFF0A5277),
    onPrimaryContainer = VelibBlueLight,

    secondary = Color(0xFFA9CCDB),
    onSecondary = Color(0xFF11333F),
    secondaryContainer = Color(0xFF2E4C58),
    onSecondaryContainer = Color(0xFFD2E6EF),

    tertiary = Color(0xFFA6D480),
    onTertiary = Color(0xFF1F3508),
    tertiaryContainer = VelibGreenDark,
    onTertiaryContainer = VelibGreenLight,

    background = VelibDarkBackground,
    onBackground = Color(0xFFDEE4E8),
    surface = VelibDarkSurface,
    onSurface = Color(0xFFDEE4E8),
    surfaceVariant = VelibDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBFC9CF),
    outline = Color(0xFF6A7A82),
)

@Composable
fun VelibTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Palette de marque fixe (pas de Material You) pour une identité Vélib' cohérente partout
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = VelibShapes,
        content = content
    )
}
