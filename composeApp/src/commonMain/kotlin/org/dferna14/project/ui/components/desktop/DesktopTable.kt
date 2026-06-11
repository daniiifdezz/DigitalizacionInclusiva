package org.dferna14.project.ui.components.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.dferna14.project.ui.theme.BordeClaro
import org.dferna14.project.ui.theme.BordeNormal
import org.dferna14.project.ui.theme.SuperficieDk
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.extraTypography

// ════════════════════════════════════════════════════════════════════════
//  DesktopTable — LazyColumn con Row cabecera + item
//  Contiene: DesktopTableColumn · DesktopTableHeader · DesktopTableRow
//
//  Uso habitual:
//    val COLS = listOf(
//        DesktopTableColumn("Agricultor",  weight = 1.3f),
//        DesktopTableColumn("Tipo",        weight = 1.6f),
//        DesktopTableColumn("",            fixedWidth = 60.dp),
//    )
//
//    DesktopTableHeader(COLS)
//    items.forEachIndexed { i, item ->
//        DesktopTableRow(COLS, last = i == items.lastIndex) {
//            listOf({ Text(item.name) }, { EstadoBadge(...) }, { ... })
//        }
//    }
// ════════════════════════════════════════════════════════════════════════

/**
 * Define una columna de tabla.
 * Usa [weight] para columnas fluidas (equivale a "fr" de CSS Grid)
 * o [fixedWidth] para anchos fijos en dp (equivale a "60px").
 * Solo uno de los dos debe estar activo; [fixedWidth] tiene precedencia.
 */
data class DesktopTableColumn(
    val label: String,
    val weight: Float = 1f,
    val fixedWidth: Dp? = null,
)

// ── Cabecera ──────────────────────────────────────────────────────────────

@Composable
fun DesktopTableHeader(
    columns: List<DesktopTableColumn>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SuperficieDk)
            .drawBehind {
                drawLine(BordeNormal, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
            }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEach { col ->
            val cellMod = if (col.fixedWidth != null) {
                Modifier.width(col.fixedWidth)
            } else {
                Modifier.weight(col.weight)
            }
            Text(
                text     = col.label.uppercase(),
                style    = MaterialTheme.extraTypography.eyebrow,
                color    = TextoTerciario,
                modifier = cellMod,
            )
        }
    }
}

// ── Fila de datos ─────────────────────────────────────────────────────────

@Composable
fun DesktopTableRow(
    columns: List<DesktopTableColumn>,
    cells: List<@Composable BoxScope.() -> Unit>,
    last: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .drawBehind {
                if (!last) drawLine(
                    color       = BordeClaro,
                    start       = Offset(0f, size.height),
                    end         = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEachIndexed { i, col ->
            val cellMod = if (col.fixedWidth != null) {
                Modifier.width(col.fixedWidth)
            } else {
                Modifier.weight(col.weight)
            }
            Box(
                modifier  = cellMod,
                content   = cells.getOrElse(i) { {} },
            )
        }
    }
}
