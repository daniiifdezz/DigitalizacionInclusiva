package org.dferna14.project.ui.components.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.dferna14.project.ui.theme.BordeNormal
import org.dferna14.project.ui.theme.SuperficieSepia
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.extraTypography

// ════════════════════════════════════════════════════════════════════════
//  DesktopStatCard — Card + Column(Text + Text)  M3
//  Tarjeta de estadística con número grande, etiqueta y hint opcional.
//
//  value       → número o texto grande (ej. "42", "—", "?")
//  label       → etiqueta eyebrow en mayúsculas
//  hint        → texto secundario debajo del número (opcional)
//  icon        → icono en la esquina superior derecha
//  accentColor → color del número y del icono (primary, accent, secondary…)
// ════════════════════════════════════════════════════════════════════════

@Composable
fun DesktopStatCard(
    value: String,
    label: String,
    hint: String = "",
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .shadow(
                elevation   = 2.dp,
                shape       = cardShape,
                ambientColor = Color(0x122C1A0E),
                spotColor   = Color(0x122C1A0E),
            )
            .background(SuperficieSepia, cardShape)
            .border(1.dp, BordeNormal, cardShape)
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Column {
            // ── Eyebrow + icono ─────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text  = label.uppercase(),
                    style = MaterialTheme.extraTypography.eyebrow,
                    color = TextoTerciario,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector      = icon,
                    contentDescription = null,
                    tint             = accentColor,
                    modifier         = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Número grande ───────────────────────────────────────────
            Text(
                text  = value,
                style = TextStyle(
                    fontFamily    = MaterialTheme.extraTypography.display.fontFamily,
                    fontSize      = 48.sp,
                    fontWeight    = FontWeight.Normal,
                    letterSpacing = (-0.02).em,
                    lineHeight    = 48.sp,
                ),
                color = accentColor,
            )

            // ── Hint ────────────────────────────────────────────────────
            if (hint.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text     = hint,
                    fontSize = 12.sp,
                    color    = TextoTerciario,
                )
            }
        }
    }
}
