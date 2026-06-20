package org.dferna14.project.ui.screens.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.domain.model.EquipoAplicacion
import org.dferna14.project.domain.model.EstadoActividad
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.Usuario
import org.dferna14.project.ui.components.CampoAvisoInfo
import org.dferna14.project.ui.components.CampoField
import org.dferna14.project.ui.components.EstadoBadge
import org.dferna14.project.ui.components.desktop.DesktopFormField
import org.dferna14.project.ui.components.desktop.DesktopSelectField
import org.dferna14.project.ui.components.desktop.DesktopTableColumn
import org.dferna14.project.ui.components.desktop.DesktopTableHeader
import org.dferna14.project.ui.components.desktop.DesktopTableRow
import org.dferna14.project.ui.components.desktop.DesktopTabBar
import org.dferna14.project.ui.components.desktop.DesktopTextareaField
import org.dferna14.project.ui.components.desktop.DesktopTopBar
import org.dferna14.project.ui.components.desktop.DesktopWrapper
import org.dferna14.project.ui.components.formatearFecha
import org.dferna14.project.ui.theme.BordeNormal
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.OlivaOscuro
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.theme.SuperficieSepia
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.extraTypography
import org.dferna14.project.ui.viewmodel.ActividadDetalleVm
import org.dferna14.project.ui.viewmodel.AjustesVm
import org.dferna14.project.ui.viewmodel.EquipoVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.dferna14.project.ui.viewmodel.UsuarioVm
import org.koin.compose.viewmodel.koinViewModel

private val EFICACIA_VALORES = listOf("ALTA", "MEDIA", "BAJA", "NULA")

private val COLS_PRODUCTOS = listOf(
    DesktopTableColumn("Producto",       weight = 2.0f),
    DesktopTableColumn("Nº Registro",    weight = 1.2f),
    DesktopTableColumn("Materia activa", weight = 2.0f),
    DesktopTableColumn("Dosis",          weight = 0.8f),
)

// ── Form state ────────────────────────────────────────────────────────────────

private data class ValidacionFs(
    val fechaFin: String = "",
    val eficacia: String = "",
    val equipo: EquipoAplicacion? = null,
    val aplicador: Usuario? = null,
    val observaciones: String = "",
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun ValidarActividadSc(
    actividadId: Int,
    onVolver: () -> Unit,
    onVerInicio: () -> Unit,
    onVerActividades: () -> Unit,
    onVerParcelas: () -> Unit,
    onVerProductos: () -> Unit,
    onVerAjustes: () -> Unit,
    onVerConfiguracion: () -> Unit,
    detalleVm: ActividadDetalleVm = koinViewModel(),
    equipoVm: EquipoVm = koinViewModel(),
    usuarioVm: UsuarioVm = koinViewModel(),
    parcelaVm: ParcelaVm = koinViewModel(),
    ajustesVm: AjustesVm = koinViewModel(),
) {
    val actividadResult   by detalleVm.actividadActual.collectAsState()
    val productosResult   by detalleVm.productosActividad.collectAsState()
    val equiposResult     by equipoVm.equipos.collectAsState()
    val usuariosResult    by usuarioVm.usuarios.collectAsState()
    val parcelaResult     by parcelaVm.parcelaCompleta.collectAsState()
    val operacionExitosa  by detalleVm.operacionExitosa.collectAsState()

    var activeTab         by remember { mutableStateOf(0) }
    var fs                by remember { mutableStateOf(ValidacionFs()) }
    var datosCargados     by remember { mutableStateOf(false) }

    LaunchedEffect(actividadId) {
        detalleVm.cargarActividad(actividadId)
        detalleVm.cargarProductosActividad(actividadId)
        equipoVm.cargarEquipos()
        usuarioVm.cargarUsuarios(rol = "APLICADOR")
    }

    // Sync form fields the first time the activity loads
    LaunchedEffect(actividadResult) {
        val act = (actividadResult as? Result.Success)?.data ?: return@LaunchedEffect
        if (act.parcelaId > 0) parcelaVm.cargarParcelaCompleta(act.parcelaId)
        if (!datosCargados) {
            fs = ValidacionFs(
                fechaFin     = act.fechaFin ?: "",
                eficacia     = act.eficacia ?: "",
                observaciones = act.observaciones ?: "",
            )
            datosCargados = true
        }
    }

    // Sync equipo/aplicador once both actividad and lists are ready
    LaunchedEffect(actividadResult, equiposResult) {
        val act     = (actividadResult as? Result.Success)?.data ?: return@LaunchedEffect
        val equipos = (equiposResult  as? Result.Success)?.data ?: return@LaunchedEffect
        if (fs.equipo == null && act.equipoId != null) {
            fs = fs.copy(equipo = equipos.find { it.id == act.equipoId })
        }
    }

    LaunchedEffect(actividadResult, usuariosResult) {
        val act      = (actividadResult as? Result.Success)?.data ?: return@LaunchedEffect
        val usuarios = (usuariosResult  as? Result.Success)?.data ?: return@LaunchedEffect
        if (fs.aplicador == null && act.aplicadorId != null) {
            fs = fs.copy(aplicador = usuarios.find { it.id == act.aplicadorId })
        }
    }

    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            detalleVm.resetOperacionExitosa()
            onVolver()
        }
    }

    val actividad = (actividadResult as? Result.Success)?.data
    val equipos   = (equiposResult  as? Result.Success)?.data ?: emptyList()
    val aplicadores = (usuariosResult as? Result.Success)?.data ?: emptyList()

    val topBarSubtitle = buildString {
        actividad?.parcelaAlias?.let { append(it).append(" · ") }
        actividad?.fechaInicio?.let { append(formatearFecha(it)) }
    }

    DesktopWrapper(
        activeIndex   = 1,
        onNavigate    = { idx ->
            when (idx) {
                0    -> onVerInicio()
                2    -> onVerParcelas()
                3    -> onVerProductos()
                4    -> onVerConfiguracion()
                5    -> onVerAjustes()
                else -> {}
            }
        },
        nombreUsuario = ajustesVm.nombreMostrado,
        rolUsuario    = ajustesVm.rolUsuario,
    ) {
        DesktopTopBar(
            title    = "Validar actividad",
            subtitle = topBarSubtitle,
        )
        DesktopTabBar(
            tabs          = listOf("Datos", "Productos", "Parcela"),
            activeIndex   = activeTab,
            onTabSelected = { activeTab = it },
        )

        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // ── Columna izquierda: contenido del tab ──────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                when (activeTab) {
                    0 -> TabDatos(
                        fs           = fs,
                        onFsChange   = { fs = it },
                        equipos      = equipos,
                        aplicadores  = aplicadores,
                        actividad    = actividad,
                        onValidar    = {
                            val act = actividad ?: return@TabDatos
                            detalleVm.actualizarActividad(
                                act.copy(
                                    fechaFin      = fs.fechaFin.ifBlank { null },
                                    eficacia      = fs.eficacia.ifBlank { null }?.uppercase(),
                                    equipoId      = fs.equipo?.id,
                                    aplicadorId   = fs.aplicador?.id,
                                    observaciones = fs.observaciones.ifBlank { null },
                                    estado        = EstadoActividad.VALIDADA,
                                )
                            )
                            onVolver()
                        },
                        onDevolver   = { detalleVm.devolverActividad(actividadId) },
                    )
                    1 -> TabProductos(productosResult = productosResult)
                    2 -> TabParcela(parcelaResult = parcelaResult)
                }
            }

            // ── Columna derecha: resumen fijo ─────────────────────────────
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .drawBehind {
                        drawLine(BordeNormal, Offset(0f, 0f), Offset(0f, size.height), 1.dp.toPx())
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PanelResumen(actividad = actividad)
            }
        }
    }
}

// ── Tab 0: Datos ──────────────────────────────────────────────────────────────

@Composable
private fun TabDatos(
    fs: ValidacionFs,
    onFsChange: (ValidacionFs) -> Unit,
    equipos: List<EquipoAplicacion>,
    aplicadores: List<Usuario>,
    actividad: org.dferna14.project.domain.model.Actividad?,
    onValidar: () -> Unit,
    onDevolver: () -> Unit,
) {
    // Form card
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(SuperficieSepia, RoundedCornerShape(12.dp))
            .border(1.dp, BordeNormal, RoundedCornerShape(12.dp))
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            DesktopFormField(
                label         = "Fecha de finalización",
                value         = fs.fechaFin,
                onValueChange = { onFsChange(fs.copy(fechaFin = it)) },
                placeholder   = "AAAA-MM-DD",
                modifier      = Modifier.weight(1f),
            )
            // Eficacia dropdown
            var showEficacia by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                DesktopSelectField(
                    label   = "Eficacia del tratamiento",
                    value   = fs.eficacia,
                    onClick = { showEficacia = true },
                )
                DropdownMenu(
                    expanded         = showEficacia,
                    onDismissRequest = { showEficacia = false },
                ) {
                    EFICACIA_VALORES.forEach { v ->
                        DropdownMenuItem(
                            text    = { Text(v) },
                            onClick = { onFsChange(fs.copy(eficacia = v)); showEficacia = false },
                        )
                    }
                }
            }
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Equipo dropdown
            var showEquipo by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                DesktopSelectField(
                    label   = "Equipo de aplicación",
                    value   = fs.equipo?.let {
                        listOfNotNull(it.tipo, it.marca, it.numeroRoma?.let { r -> "ROMA $r" }).joinToString(" · ")
                    } ?: "",
                    onClick = { showEquipo = true },
                )
                DropdownMenu(
                    expanded         = showEquipo,
                    onDismissRequest = { showEquipo = false },
                ) {
                    DropdownMenuItem(
                        text    = { Text("Sin asignar") },
                        onClick = { onFsChange(fs.copy(equipo = null)); showEquipo = false },
                    )
                    equipos.forEach { eq ->
                        DropdownMenuItem(
                            text = {
                                Text(listOfNotNull(eq.tipo, eq.marca, eq.modelo).joinToString(" ").ifBlank { "Equipo ${eq.id}" })
                            },
                            onClick = { onFsChange(fs.copy(equipo = eq)); showEquipo = false },
                        )
                    }
                }
            }
            // Aplicador dropdown
            var showAplicador by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                DesktopSelectField(
                    label   = "Aplicador",
                    value   = fs.aplicador?.let {
                        listOfNotNull(it.nombre, it.apellidos).joinToString(" ")
                    } ?: "",
                    onClick = { showAplicador = true },
                )
                DropdownMenu(
                    expanded         = showAplicador,
                    onDismissRequest = { showAplicador = false },
                ) {
                    DropdownMenuItem(
                        text    = { Text("Sin asignar") },
                        onClick = { onFsChange(fs.copy(aplicador = null)); showAplicador = false },
                    )
                    aplicadores.forEach { u ->
                        DropdownMenuItem(
                            text    = { Text(listOfNotNull(u.nombre, u.apellidos).joinToString(" ")) },
                            onClick = { onFsChange(fs.copy(aplicador = u)); showAplicador = false },
                        )
                    }
                }
            }
        }
        DesktopTextareaField(
            label         = "Observaciones técnicas",
            value         = fs.observaciones,
            onValueChange = { onFsChange(fs.copy(observaciones = it)) },
            placeholder   = "Condiciones verificadas, incidencias durante la inspección…",
            minLines      = 4,
        )
    }

    val fechaFinInvalida = fs.fechaFin.isNotBlank() &&
        actividad?.fechaInicio?.let { fs.fechaFin < it } == true

    if (fechaFinInvalida) {
        Text(
            text     = "La fecha de finalización no puede ser anterior a la fecha de inicio",
            color    = RojoEliminar,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    // Aviso legal
    if (actividad?.estado == EstadoActividad.PENDIENTE_VALIDAR) {
        CampoAvisoInfo(
            mensaje = "Al validar confirmas que los datos son correctos según el RD 1311/2012 " +
                    "y que el equipo y aplicador están autorizados.",
        )
    }

    // Botones de acción
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Validar (primary)
        Row(
            modifier              = Modifier
                .weight(1f)
                .height(52.dp)
                .background(OlivaPrimario, RoundedCornerShape(8.dp))
                .border(1.dp, OlivaOscuro, RoundedCornerShape(8.dp))
                .clickable(
                    enabled = actividad?.estado == EstadoActividad.PENDIENTE_VALIDAR && !fechaFinInvalida,
                    onClick = onValidar,
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Check, contentDescription = null, tint = CremaPrincipal, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Validar actividad", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CremaPrincipal)
        }
        // Devolver (secondary)
        Box(
            modifier         = Modifier
                .height(52.dp)
                .border(2.dp, BordeNormal, RoundedCornerShape(8.dp))
                .clickable(
                    enabled = actividad?.estado == EstadoActividad.PENDIENTE_VALIDAR,
                    onClick = onDevolver,
                )
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Devolver", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextoSecundario)
        }
    }
}

// ── Tab 1: Productos ──────────────────────────────────────────────────────────

@Composable
private fun TabProductos(
    productosResult: Result<List<org.dferna14.project.domain.model.ActividadProducto>>,
) {
    Text(
        text     = "Productos aplicados".uppercase(),
        style    = MaterialTheme.extraTypography.eyebrow,
        color    = TextoTerciario,
    )
    DesktopTableHeader(COLS_PRODUCTOS)
    val productos = (productosResult as? Result.Success)?.data ?: emptyList()
    productos.forEachIndexed { i, p ->
        DesktopTableRow(
            columns = COLS_PRODUCTOS,
            last    = i == productos.lastIndex,
            cells   = listOf(
                { Text(p.productoNombreComercial ?: "Producto ${p.productoId}", style = MaterialTheme.typography.bodyMedium.copy(color = TextoPrimario)) },
                { Text(p.productoNumeroRegistro ?: "—", style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario)) },
                { Text(p.productoMateriaActiva  ?: "—", style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario)) },
                { Text("${p.dosis} l/ha",               style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario)) },
            ),
        )
    }
    if (productos.isEmpty()) {
        Box(
            modifier         = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Sin productos registrados", style = MaterialTheme.typography.bodyMedium.copy(color = TextoTerciario))
        }
    }
}

// ── Tab 2: Parcela ────────────────────────────────────────────────────────────

@Composable
private fun TabParcela(
    parcelaResult: Result<org.dferna14.project.domain.model.ParcelaCompleta?>,
) {
    val sigpac      = (parcelaResult as? Result.Success)?.data?.referenciaSigpac
    val agronomico  = (parcelaResult as? Result.Success)?.data?.datosAgronomicos

    Text(
        text     = "Referencia SIGPAC".uppercase(),
        style    = MaterialTheme.extraTypography.eyebrow,
        color    = TextoTerciario,
    )
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(SuperficieSepia, RoundedCornerShape(12.dp))
            .border(1.dp, BordeNormal, RoundedCornerShape(12.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DesktopFormField("Provincia (cód.)",   sigpac?.provincia         ?: "", {}, readOnly = true, modifier = Modifier.weight(1f))
            DesktopFormField("Término municipal",  sigpac?.terminoMunicipal  ?: "", {}, readOnly = true, modifier = Modifier.weight(1f))
            DesktopFormField("Municipio (cód.)",   sigpac?.codigoAgregado    ?: "", {}, readOnly = true, modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DesktopFormField("Polígono", sigpac?.numeroPoligono ?: "", {}, readOnly = true, modifier = Modifier.weight(1f))
            DesktopFormField("Parcela",  sigpac?.numeroParcela  ?: "", {}, readOnly = true, modifier = Modifier.weight(1f))
            DesktopFormField("Recinto",  sigpac?.numeroRecinto  ?: "", {}, readOnly = true, modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DesktopFormField("Uso SIGPAC",       sigpac?.usoSigpac            ?: "—",           {}, readOnly = true, modifier = Modifier.weight(1f))
            DesktopFormField("Superficie (ha)",  sigpac?.superficieHa?.toString() ?: "—",      {}, readOnly = true, modifier = Modifier.weight(1f))
        }
    }

    Text(
        text     = "Datos agronómicos".uppercase(),
        style    = MaterialTheme.extraTypography.eyebrow,
        color    = TextoTerciario,
    )
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(SuperficieSepia, RoundedCornerShape(12.dp))
            .border(1.dp, BordeNormal, RoundedCornerShape(12.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DesktopFormField("Especie / variedad",  agronomico?.especieVariedad    ?: "—", {}, readOnly = true, modifier = Modifier.weight(1f))
            DesktopFormField("Régimen hídrico",     agronomico?.secanoRegadio       ?: "—", {}, readOnly = true, modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DesktopFormField("Fecha de siembra",    agronomico?.fechaInicio?.let { formatearFecha(it) } ?: "—", {}, readOnly = true, modifier = Modifier.weight(1f))
            DesktopFormField("Fecha prevista cosecha", agronomico?.fechaFin?.let { formatearFecha(it) } ?: "—", {}, readOnly = true, modifier = Modifier.weight(1f))
        }
    }
}

// ── Panel derecho: resumen de la actividad ────────────────────────────────────

@Composable
private fun PanelResumen(actividad: org.dferna14.project.domain.model.Actividad?) {
    Text(
        text     = "Resumen de la actividad".uppercase(),
        style    = MaterialTheme.extraTypography.eyebrow,
        color    = TextoTerciario,
    )
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(SuperficieSepia, RoundedCornerShape(12.dp))
            .border(1.dp, BordeNormal, RoundedCornerShape(12.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (actividad != null) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top,
            ) {
                Text(
                    text       = actividad.problemaFitosanitario?.take(40) ?: "Tratamiento fitosanitario",
                    style      = MaterialTheme.extraTypography.display.copy(fontSize = 15.sp),
                    color      = TextoPrimario,
                    modifier   = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                EstadoBadge(actividad.estado)
            }
            Spacer(Modifier.height(4.dp))
            CampoField(label = "Parcela",    value = actividad.parcelaAlias ?: "Parcela ${actividad.parcelaId}")
            CampoField(label = "Fecha",      value = formatearFecha(actividad.fechaInicio))
            actividad.superficieTratada?.let {
                CampoField(label = "Superficie",  value = "$it ha")
            }
            CampoField(label = "Problema",   value = actividad.problemaFitosanitario ?: "—")
            CampoField(label = "Observaciones", value = actividad.observaciones ?: "—")
        } else {
            Text("Cargando…", style = MaterialTheme.typography.bodyMedium.copy(color = TextoTerciario))
        }
    }
}
