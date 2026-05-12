package org.dferna14.project.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tema de la app — colores cálidos, alta accesibilidad. Los tamaños de
 * tipografía están aumentados respecto a los defaults de Material3 para que
 * el contenido sea cómodo de leer en campo por usuarios mayores.
 */

private val AppColorScheme = lightColorScheme(
    primary           = NaranjaPrimario,
    onPrimary         = BlancoPuro,
    primaryContainer  = NaranjaClaro,
    onPrimaryContainer = NaranjaOscuro,
    secondary         = NaranjaOscuro,
    onSecondary       = BlancoPuro,
    background        = CremaPrincipal,
    onBackground      = TextoPrimario,
    surface           = BlancoPuro,
    onSurface         = TextoPrimario,
    surfaceVariant    = CremaSecundario,
    onSurfaceVariant  = TextoSecundario,
    outline           = BordeSuave,
    outlineVariant    = BordeMedio,
    error             = RojoEliminar,
    onError           = BlancoPuro,
    errorContainer    = RojoFondoEliminar,
    onErrorContainer  = RojoEliminar
)

/**
 * Tipografía con tamaños mínimos accesibles. Los tres bodies suben respecto
 * al default. titleMedium/Large suben para cabeceras claras. labelSmall queda
 * en 11sp para metadatos.
 */
private val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleSmall = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
    )
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
