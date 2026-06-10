package org.dferna14.project.ui.components.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dferna14.project.ui.theme.CremaPrincipal

// ════════════════════════════════════════════════════════════════════════
//  DesktopWrapper — Scaffold con drawer lateral fijo
//  Usa Row: DesktopSidebar (240dp) + Column de contenido (peso 1).
//
//  Úsalo en cada pantalla Desktop como contenedor raíz:
//    DesktopWrapper(activeIndex = 0, onNavigate = { ... }) {
//        DesktopTopBar(...)
//        // contenido scrollable
//    }
// ════════════════════════════════════════════════════════════════════════

@Composable
fun DesktopWrapper(
    activeIndex: Int,
    onNavigate: (Int) -> Unit,
    nombreUsuario: String = "",
    rolUsuario: String = "",
    badges: Map<Int, Int> = emptyMap(),
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(CremaPrincipal),
    ) {
        DesktopSidebar(
            activeIndex   = activeIndex,
            onItemClick   = onNavigate,
            nombreUsuario = nombreUsuario,
            rolUsuario    = rolUsuario,
            badges        = badges,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            content = content,
        )
    }
}
