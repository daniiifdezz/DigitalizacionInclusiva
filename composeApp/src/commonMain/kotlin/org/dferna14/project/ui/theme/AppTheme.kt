package org.dferna14.project.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import digitalizacioninclusiva.composeapp.generated.resources.Res
import digitalizacioninclusiva.composeapp.generated.resources.ibm_plex_mono_regular
import digitalizacioninclusiva.composeapp.generated.resources.playfair_display_medium
import digitalizacioninclusiva.composeapp.generated.resources.playfair_display_regular
import digitalizacioninclusiva.composeapp.generated.resources.playfair_display_semibold
import digitalizacioninclusiva.composeapp.generated.resources.source_sans_3_bold
import digitalizacioninclusiva.composeapp.generated.resources.source_sans_3_regular
import digitalizacioninclusiva.composeapp.generated.resources.source_sans_3_semibold
import org.jetbrains.compose.resources.Font

// ─────────────────────────────────────────────────────────────────────────────
//  Color scheme — paleta oliva/vintage
// ─────────────────────────────────────────────────────────────────────────────
private val AppColorScheme = lightColorScheme(
    primary              = OlivaPrimario,
    onPrimary            = CremaPrincipal,
    primaryContainer     = OlivaTint,
    onPrimaryContainer   = OlivaOscuro,
    secondary            = OcreSecundario,
    onSecondary          = BlancoPuro,
    secondaryContainer   = SuperficieSepia,
    onSecondaryContainer = TextoSecundario,
    tertiary             = TerracotaAccent,
    onTertiary           = CremaPrincipal,
    tertiaryContainer    = TerracotaTint,
    onTertiaryContainer  = NaranjaOscuro,
    background           = CremaPrincipal,
    onBackground         = TextoPrimario,
    surface              = SuperficieSepia,
    onSurface            = TextoPrimario,
    surfaceVariant       = SuperficieDk,
    onSurfaceVariant     = TextoSecundario,
    outline              = BordeNormal,
    outlineVariant       = BordeClaro,
    error                = RojoEliminar,
    onError              = BlancoPuro,
    errorContainer       = RojoFondoEliminar,
    onErrorContainer     = RojoEliminar,
)

// ─────────────────────────────────────────────────────────────────────────────
//  AppTheme — punto de entrada único para toda la app
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppTheme(content: @Composable () -> Unit) {

    // ── Familias tipográficas cargadas desde composeResources/font/ ──────────
    val playfairRegular  = Font(Res.font.playfair_display_regular,  FontWeight.Normal)
    val playfairMedium   = Font(Res.font.playfair_display_medium,   FontWeight.Medium)
    val playfairSemibold = Font(Res.font.playfair_display_semibold, FontWeight.SemiBold)
    val playfair = FontFamily(playfairRegular, playfairMedium, playfairSemibold)

    val sansRegular  = Font(Res.font.source_sans_3_regular,  FontWeight.Normal)
    val sansSemibold = Font(Res.font.source_sans_3_semibold, FontWeight.SemiBold)
    val sansBold     = Font(Res.font.source_sans_3_bold,     FontWeight.Bold)
    val sourceSans = FontFamily(sansRegular, sansSemibold, sansBold)

    val monoRegular = Font(Res.font.ibm_plex_mono_regular, FontWeight.Normal)
    val ibmMono = FontFamily(monoRegular)

    // ── Typography Material3 ─────────────────────────────────────────────────
    // display*/headline* → Playfair Display (serif, para títulos de pantalla)
    // title*/body*/label* → Source Sans 3 (sin serifa, UI legible)
    val typography = Typography(
        displayLarge   = TextStyle(fontFamily = playfair,   fontSize = 57.sp, fontWeight = FontWeight.Normal,   letterSpacing = (-0.02).em),
        displayMedium  = TextStyle(fontFamily = playfair,   fontSize = 45.sp, fontWeight = FontWeight.Normal,   letterSpacing = (-0.02).em),
        displaySmall   = TextStyle(fontFamily = playfair,   fontSize = 36.sp, fontWeight = FontWeight.Normal,   letterSpacing = (-0.01).em),
        headlineLarge  = TextStyle(fontFamily = playfair,   fontSize = 32.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em),
        headlineMedium = TextStyle(fontFamily = playfair,   fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
        headlineSmall  = TextStyle(fontFamily = playfair,   fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
        titleLarge     = TextStyle(fontFamily = playfair,   fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
        titleMedium    = TextStyle(fontFamily = sourceSans, fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
        titleSmall     = TextStyle(fontFamily = sourceSans, fontSize = 15.sp, fontWeight = FontWeight.Medium),
        bodyLarge      = TextStyle(fontFamily = sourceSans, fontSize = 16.sp, fontWeight = FontWeight.Normal),
        bodyMedium     = TextStyle(fontFamily = sourceSans, fontSize = 14.sp, fontWeight = FontWeight.Normal),
        bodySmall      = TextStyle(fontFamily = sourceSans, fontSize = 13.sp, fontWeight = FontWeight.Normal),
        labelLarge     = TextStyle(fontFamily = sourceSans, fontSize = 14.sp, fontWeight = FontWeight.Medium),
        labelMedium    = TextStyle(fontFamily = sourceSans, fontSize = 12.sp, fontWeight = FontWeight.Medium),
        labelSmall     = TextStyle(fontFamily = sourceSans, fontSize = 11.sp, fontWeight = FontWeight.Medium),
    )

    // ── Tokens extra (display / eyebrow / mono) fuera de Material3 ───────────
    val extraTypography = AppExtraTypography(
        display = TextStyle(
            fontFamily   = playfair,
            fontSize     = 22.sp,
            fontWeight   = FontWeight.SemiBold,
            letterSpacing = (-0.01).em,
        ),
        eyebrow = TextStyle(
            fontFamily   = sourceSans,
            fontSize     = 10.5.sp,
            fontWeight   = FontWeight.SemiBold,
            letterSpacing = 0.05.em,
        ),
        mono = TextStyle(
            fontFamily = ibmMono,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Normal,
        ),
    )

    CompositionLocalProvider(LocalExtraTypography provides extraTypography) {
        MaterialTheme(
            colorScheme = AppColorScheme,
            typography  = typography,
            shapes      = AppShapes,
            content     = content,
        )
    }
}
