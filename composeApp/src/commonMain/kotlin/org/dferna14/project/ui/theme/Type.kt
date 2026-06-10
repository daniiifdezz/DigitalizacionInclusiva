package org.dferna14.project.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

/**
 * Tokens tipográficos extra que no caben en el Typography de Material3.
 * Accede desde cualquier composable con MaterialTheme.extraTypography.
 *
 *  display → Playfair Display, títulos de pantalla / números grandes
 *  eyebrow → Source Sans 3 SemiBold, etiquetas en mayúsculas
 *  mono    → IBM Plex Mono, fechas, códigos, IDs
 */
data class AppExtraTypography(
    val display: TextStyle,
    val eyebrow: TextStyle,
    val mono: TextStyle,
)

val LocalExtraTypography = staticCompositionLocalOf {
    AppExtraTypography(
        display = TextStyle.Default,
        eyebrow = TextStyle.Default,
        mono    = TextStyle.Default,
    )
}

val MaterialTheme.extraTypography: AppExtraTypography
    @Composable get() = LocalExtraTypography.current
