package fr.epf.sni2.velib_android.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Effet "verre dépoli" léger : surface translucide + dégradé subtil + fine bordure claire.
 * Pensé pour les éléments qui flottent au-dessus d'un fond (barre de recherche sur la carte…).
 */
@Composable
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(20.dp),
    alpha: Float = if (isSystemInDarkTheme()) 0.55f else 0.72f,
): Modifier {
    val base = MaterialTheme.colorScheme.surface
    val highlight = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.45f)
    return this
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                listOf(
                    base.copy(alpha = (alpha + 0.12f).coerceAtMost(1f)),
                    base.copy(alpha = alpha),
                ),
            ),
            shape = shape,
        )
        .border(width = 1.dp, color = highlight, shape = shape)
}
