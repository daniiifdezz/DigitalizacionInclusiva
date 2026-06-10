package org.dferna14.project.ui.components.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.ui.theme.BordeNormal
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.TextoSecundario

// ════════════════════════════════════════════════════════════════════════
//  DesktopTabBar — TabRow + Tab (M3)
//  Barra de pestañas con indicador inferior de 3dp en oliva.
//  El indicador activo se superpone visualmente al borde del contenedor.
// ════════════════════════════════════════════════════════════════════════

@Composable
fun DesktopTabBar(
    tabs: List<String>,
    activeIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CremaPrincipal)
            .drawBehind {
                // Borde inferior del contenedor — queda debajo del indicador activo
                drawLine(
                    color       = BordeNormal,
                    start       = Offset(0f, size.height),
                    end         = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(start = 28.dp),
    ) {
        tabs.forEachIndexed { i, tab ->
            val isActive = i == activeIndex

            Column(
                modifier            = Modifier.clickable { onTabSelected(i) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text       = tab,
                    fontSize   = 14.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isActive) OlivaPrimario else TextoSecundario,
                    modifier   = Modifier.padding(
                        horizontal = 16.dp,
                        top        = 11.dp,
                        bottom     = 9.dp,
                    ),
                )
                // Indicador 3dp — Color.Transparent en tabs inactivas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(if (isActive) OlivaPrimario else Color.Transparent),
                )
            }
        }
    }
}
