package org.dferna14.project.ui.components.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.dferna14.project.ui.theme.BordeNormal
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.extraTypography

// ════════════════════════════════════════════════════════════════════════
//  InlineCreateCard — Card con borde punteado para formularios inline
//  Fondo CremaPrincipal, borde 1.5dp punteado BordeNormal, radio 12dp.
//  Se usa para "Nuevo agricultor", "Nueva parcela", etc.
// ════════════════════════════════════════════════════════════════════════

@Composable
fun InlineCreateCard(
    title: String = "",
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth  = 1.5.dp.toPx()
                val cornerRadius = 12.dp.toPx()
                val pathEffect   = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                drawRoundRect(
                    color       = BordeNormal,
                    size        = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style       = Stroke(width = strokeWidth, pathEffect = pathEffect),
                )
            }
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        if (title.isNotBlank()) {
            Text(
                text  = title.uppercase(),
                style = MaterialTheme.extraTypography.eyebrow,
                color = TextoTerciario,
            )
            Spacer(Modifier.height(14.dp))
        }
        content()
    }
}
