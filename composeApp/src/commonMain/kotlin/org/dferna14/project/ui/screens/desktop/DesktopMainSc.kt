package org.dferna14.project.ui.screens.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.EstadoBadge
import org.dferna14.project.ui.components.desktop.DesktopStatCard
import org.dferna14.project.ui.components.desktop.DesktopTableColumn
import org.dferna14.project.ui.components.desktop.DesktopTableHeader
import org.dferna14.project.ui.components.desktop.DesktopTableRow
import org.dferna14.project.ui.components.desktop.DesktopTopBar
import org.dferna14.project.ui.components.desktop.DesktopTopBarAction
import org.dferna14.project.ui.components.desktop.DesktopWrapper
import org.dferna14.project.ui.components.formatearFecha
import org.dferna14.project.ui.theme.OcreSecundario
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.TerracotaAccent
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.viewmodel.ActividadListaVm
import org.dferna14.project.ui.viewmodel.AjustesVm
import org.dferna14.project.ui.viewmodel.UsuarioVm
import org.koin.compose.viewmodel.koinViewModel

private val COLS_RECIENTES = listOf(
    DesktopTableColumn("Parcela",   weight = 1.2f),
    DesktopTableColumn("Problema",  weight = 2.2f),
    DesktopTableColumn("Fecha",     weight = 1.0f),
    DesktopTableColumn("Estado",    weight = 1.8f),
)

private val COLS_PENDIENTES = listOf(
    DesktopTableColumn("Parcela",   weight = 1.2f),
    DesktopTableColumn("Problema",  weight = 2.2f),
    DesktopTableColumn("Fecha",     weight = 1.0f),
    DesktopTableColumn("",          fixedWidth = 100.dp),
)

@Composable
fun DesktopMainSc(
    onVerActividades: () -> Unit,
    onVerParcelas: () -> Unit,
    onVerProductos: () -> Unit,
    onVerAjustes: () -> Unit,
    onVerConfiguracion: () -> Unit,
    onVerValidar: (Int) -> Unit,
    onNuevaEntrada: () -> Unit = {},
    onExportarPdf: () -> Unit = {},
    actividadListaVm: ActividadListaVm = koinViewModel(),
    usuarioVm: UsuarioVm = koinViewModel(),
    ajustesVm: AjustesVm = koinViewModel(),
) {
    val actividadesResult by actividadListaVm.actividades.collectAsState()
    val pendientesResult  by actividadListaVm.actividadesPendientes.collectAsState()
    val usuariosResult    by usuarioVm.usuarios.collectAsState()

    LaunchedEffect(Unit) {
        actividadListaVm.cargarActividades()
        actividadListaVm.cargarActividadesPendientes()
        usuarioVm.cargarUsuarios()
    }

    val actividades: List<Actividad> = when (val r = actividadesResult) {
        is Result.Success -> r.data
        else              -> emptyList()
    }
    val pendientes: List<Actividad> = when (val r = pendientesResult) {
        is Result.Success -> r.data
        else              -> emptyList()
    }
    val numAgricultores: Int = when (val r = usuariosResult) {
        is Result.Success -> r.data.count { it.rol == "AGRICULTOR" }
        else              -> 0
    }

    val subtitleBar = buildString {
        ajustesVm.explotacionNombre?.let { append(it) }
        if (ajustesVm.explotacionNombre != null) append(" · ")
        append(ajustesVm.nombreMostrado)
    }

    DesktopWrapper(
        activeIndex   = 0,
        onNavigate    = { idx ->
            when (idx) {
                1    -> onVerActividades()
                2    -> onVerParcelas()
                3    -> onVerProductos()
                4    -> onVerAjustes()
                5    -> onVerConfiguracion()
                6    -> onVerAjustes()
                else -> {}
            }
        },
        nombreUsuario = ajustesVm.nombreMostrado,
        rolUsuario    = ajustesVm.rolUsuario,
        badges        = if (pendientes.isNotEmpty()) mapOf(1 to pendientes.size) else emptyMap(),
    ) {
        DesktopTopBar(
            title    = "Panel del técnico",
            subtitle = subtitleBar,
            actions  = listOf(
                DesktopTopBarAction(
                    label   = "Exportar PDF",
                    icon    = Icons.Outlined.PictureAsPdf,
                    primary = false,
                    onClick = onExportarPdf,
                ),
                DesktopTopBarAction(
                    label   = "Nueva entrada",
                    icon    = Icons.Outlined.Add,
                    primary = true,
                    onClick = onNuevaEntrada,
                ),
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
        ) {
            // ── Stat cards ──────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DesktopStatCard(
                    value       = actividades.size.toString(),
                    label       = "Actividades",
                    hint        = "Registradas en el sistema",
                    icon        = Icons.Outlined.ListAlt,
                    accentColor = OlivaPrimario,
                    modifier    = Modifier.weight(1f),
                )
                DesktopStatCard(
                    value       = pendientes.size.toString(),
                    label       = "Pendientes",
                    hint        = "De validar",
                    icon        = Icons.Outlined.CheckCircle,
                    accentColor = TerracotaAccent,
                    modifier    = Modifier.weight(1f),
                )
                DesktopStatCard(
                    value       = numAgricultores.toString(),
                    label       = "Agricultores",
                    hint        = "Activos",
                    icon        = Icons.Outlined.Group,
                    accentColor = OcreSecundario,
                    modifier    = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Tabla: actividades recientes ─────────────────────────────
            SectionTitle("Actividades recientes")
            Spacer(Modifier.height(10.dp))
            DesktopTableHeader(COLS_RECIENTES)
            actividades.take(10).forEachIndexed { i, act ->
                DesktopTableRow(
                    columns = COLS_RECIENTES,
                    last    = i == (actividades.take(10).lastIndex),
                    cells   = listOf(
                        {
                            Text(
                                text  = act.parcelaAlias ?: "Parcela ${act.parcelaId}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextoPrimario),
                            )
                        },
                        {
                            Text(
                                text  = act.problemaFitosanitario?.take(45) ?: "—",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario),
                            )
                        },
                        {
                            Text(
                                text  = formatearFecha(act.fechaInicio),
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario),
                            )
                        },
                        { EstadoBadge(act.estado) },
                    ),
                )
            }
            if (actividades.isEmpty()) {
                EmptyRow("Sin actividades registradas")
            }

            Spacer(Modifier.height(32.dp))

            // ── Tabla: pendientes de validar ─────────────────────────────
            SectionTitle("Pendientes de validar")
            Spacer(Modifier.height(10.dp))
            DesktopTableHeader(COLS_PENDIENTES)
            pendientes.forEachIndexed { i, act ->
                DesktopTableRow(
                    columns = COLS_PENDIENTES,
                    last    = i == pendientes.lastIndex,
                    onClick = { onVerValidar(act.id) },
                    cells   = listOf(
                        {
                            Text(
                                text  = act.parcelaAlias ?: "Parcela ${act.parcelaId}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextoPrimario),
                            )
                        },
                        {
                            Text(
                                text  = act.problemaFitosanitario?.take(45) ?: "—",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario),
                            )
                        },
                        {
                            Text(
                                text  = formatearFecha(act.fechaInicio),
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario),
                            )
                        },
                        {
                            Text(
                                text       = "Validar →",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color      = OlivaPrimario,
                            )
                        },
                    ),
                )
            }
            if (pendientes.isEmpty()) {
                EmptyRow("Sin actividades pendientes")
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text       = text,
        fontSize   = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color      = TextoPrimario,
    )
}

@Composable
private fun EmptyRow(message: String) {
    Box(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        contentAlignment    = Alignment.Center,
    ) {
        Text(
            text  = message,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextoTerciario),
        )
    }
}
