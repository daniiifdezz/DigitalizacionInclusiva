package org.dferna14.project.ui.components.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.dferna14.project.ui.theme.OlivaClaro
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.OlivaTint
import org.dferna14.project.ui.theme.SidebarActivo
import org.dferna14.project.ui.theme.SidebarBorde
import org.dferna14.project.ui.theme.SidebarFondo
import org.dferna14.project.ui.theme.SidebarHi
import org.dferna14.project.ui.theme.SidebarLabel
import org.dferna14.project.ui.theme.SidebarTexto
import org.dferna14.project.ui.theme.TerracotaAccent

// ════════════════════════════════════════════════════════════════════════
//  DesktopSidebar — PermanentNavigationDrawer (M3)
//  Sidebar oscuro fijo con 2 grupos de navegación y pie de usuario.
//
//  activeIndex: índice global de 0-6:
//    Cuaderno  → 0 Inicio · 1 Actividades · 2 Parcelas · 3 Productos
//    Gestión   → 4 Agricultores · 5 Configuración · 6 Ajustes
//  badges: mapa índice → número a mostrar en el badge
// ════════════════════════════════════════════════════════════════════════

private data class NavItem(val label: String, val icon: ImageVector)

private val NAV_GROUPS = listOf(
    "Cuaderno" to listOf(
        NavItem("Inicio",        Icons.Outlined.Home),
        NavItem("Actividades",   Icons.Outlined.MenuBook),
        NavItem("Parcelas",      Icons.Outlined.Map),
        NavItem("Productos",     Icons.Outlined.Science),
    ),
    "Gestión" to listOf(
        NavItem("Agricultores",  Icons.Outlined.Person),
        NavItem("Configuración", Icons.Outlined.Settings),
        NavItem("Ajustes",       Icons.Outlined.ManageAccounts),
    ),
)

@Composable
fun DesktopSidebar(
    activeIndex: Int,
    onItemClick: (Int) -> Unit,
    nombreUsuario: String = "",
    rolUsuario: String = "",
    badges: Map<Int, Int> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    // Pre-calcular el índice de inicio de cada grupo para mapear clics correctamente
    val groupStartIndices = NAV_GROUPS.runningFold(0) { acc, (_, items) -> acc + items.size }

    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(SidebarFondo)
            .drawBehind {
                // Borde derecho 1dp
                drawLine(
                    color  = SidebarBorde,
                    start  = Offset(size.width, 0f),
                    end    = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            },
    ) {

        // ── Header ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(SidebarBorde, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                }
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SidebarBorde),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector      = Icons.Outlined.MenuBook,
                    contentDescription = null,
                    tint             = SidebarHi,
                    modifier         = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text       = "Cuaderno de Campo",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = SidebarHi,
                    lineHeight = 16.sp,
                )
                Text(
                    text     = "Panel del técnico",
                    fontSize = 10.5.sp,
                    color    = SidebarTexto,
                )
            }
        }

        // ── Nav groups ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            NAV_GROUPS.forEachIndexed { gi, (groupLabel, items) ->
                val startIdx = groupStartIndices[gi]

                Text(
                    text          = groupLabel.uppercase(),
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = SidebarLabel,
                    letterSpacing = 0.06.em,
                    modifier      = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(top = 4.dp, bottom = 7.dp),
                )

                items.forEachIndexed { li, item ->
                    val idx      = startIdx + li
                    val isActive = idx == activeIndex
                    val badge    = badges[idx]

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) SidebarActivo else SidebarFondo)
                            .clickable { onItemClick(idx) }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector      = item.icon,
                            contentDescription = null,
                            tint             = if (isActive) SidebarHi else SidebarTexto,
                            modifier         = Modifier.size(17.dp),
                        )
                        Text(
                            text       = item.label,
                            fontSize   = 13.5.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color      = if (isActive) SidebarHi else SidebarTexto,
                            modifier   = Modifier.weight(1f),
                        )
                        if (badge != null && badge > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(TerracotaAccent)
                                    .padding(horizontal = 7.dp, vertical = 1.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text       = badge.toString(),
                                    fontSize   = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = SidebarHi,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(1.dp))
                }

                if (gi < NAV_GROUPS.lastIndex) Spacer(Modifier.height(14.dp))
            }
        }

        // ── Footer / usuario ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(SidebarBorde, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                }
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .padding(bottom = 4.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val iniciales = nombreUsuario.split(" ")
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .take(2)
                .joinToString("")
                .ifBlank { "?" }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(OlivaTint, CircleShape)
                    .border(1.dp, OlivaClaro, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = iniciales,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = OlivaPrimario,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = nombreUsuario.ifBlank { "Usuario" },
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = SidebarHi,
                    maxLines   = 1,
                )
                Text(
                    text     = rolUsuario.ifBlank { "Asesor agrícola" },
                    fontSize = 10.5.sp,
                    color    = SidebarTexto,
                )
            }
        }
    }
}
