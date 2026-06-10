package org.dferna14.project.ui.components.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.ui.theme.BordeNormal
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.OlivaOscuro
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.SuperficieSepia
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.extraTypography

// ════════════════════════════════════════════════════════════════════════
//  DesktopTopBar — TopAppBar M3
//  Barra superior con título (Playfair), subtítulo opcional y botones
//  de acción primario (relleno oliva) o secundario (outline oliva).
// ════════════════════════════════════════════════════════════════════════

data class DesktopTopBarAction(
    val label: String,
    val icon: ImageVector? = null,
    val primary: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
fun DesktopTopBar(
    title: String,
    subtitle: String = "",
    actions: List<DesktopTopBarAction> = emptyList(),
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(SuperficieSepia)
            .drawBehind {
                drawLine(BordeNormal, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
            }
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Título + subtítulo ──────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text      = title,
                style     = MaterialTheme.extraTypography.display,
                color     = TextoPrimario,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text     = subtitle,
                    fontSize = 11.5.sp,
                    color    = TextoTerciario,
                )
            }
        }

        // ── Botones de acción ───────────────────────────────────────────
        actions.forEach { action ->
            TopBarButton(action = action)
        }
    }
}

@Composable
private fun TopBarButton(action: DesktopTopBarAction) {
    val bgColor    = if (action.primary) OlivaPrimario  else Color.Transparent
    val textColor  = if (action.primary) CremaPrincipal else OlivaPrimario
    val borderColor = if (action.primary) OlivaOscuro   else OlivaPrimario
    val borderWidth = if (action.primary) 1.dp          else 2.dp

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = action.onClick)
            .defaultMinSize(minHeight = 44.dp)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        action.icon?.let {
            Icon(
                imageVector      = it,
                contentDescription = null,
                tint             = textColor,
                modifier         = Modifier.size(15.dp),
            )
        }
        Text(
            text       = action.label,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Bold,
            color      = textColor,
        )
    }
}
