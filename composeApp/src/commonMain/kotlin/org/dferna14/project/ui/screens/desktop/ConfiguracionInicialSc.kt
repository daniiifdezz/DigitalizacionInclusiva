package org.dferna14.project.ui.screens.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Group
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.domain.model.EquipoAplicacion
import org.dferna14.project.domain.model.Explotacion
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.Titular
import org.dferna14.project.domain.model.Usuario
import org.dferna14.project.ui.components.desktop.DesktopFormField
import org.dferna14.project.ui.components.desktop.DesktopSelectField
import org.dferna14.project.ui.components.desktop.DesktopTableColumn
import org.dferna14.project.ui.components.desktop.DesktopTableHeader
import org.dferna14.project.ui.components.desktop.DesktopTableRow
import org.dferna14.project.ui.components.desktop.DesktopTabBar
import org.dferna14.project.ui.components.desktop.DesktopTopBar
import org.dferna14.project.ui.components.desktop.DesktopTopBarAction
import org.dferna14.project.ui.components.desktop.DesktopWrapper
import org.dferna14.project.ui.components.desktop.InlineCreateCard
import org.dferna14.project.ui.components.formatearFecha
import org.dferna14.project.ui.theme.BordeNormal
import org.dferna14.project.ui.theme.RojoEliminar
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.outlined.Close
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.OlivaTint
import org.dferna14.project.ui.theme.SuperficieSepia
import org.dferna14.project.ui.theme.TerracotaAccent
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.extraTypography
import org.dferna14.project.ui.viewmodel.AjustesVm
import org.dferna14.project.ui.viewmodel.ConfiguracionVm
import org.dferna14.project.ui.viewmodel.EquipoVm
import org.dferna14.project.ui.viewmodel.UsuarioVm
import org.dferna14.project.util.isEmailValido
import org.koin.compose.viewmodel.koinViewModel

// ── Columnas de tablas ────────────────────────────────────────────────────────

private val COLS_EQUIPOS = listOf(
    DesktopTableColumn("Tipo",               weight = 1.5f),
    DesktopTableColumn("Marca / Modelo",     weight = 2.0f),
    DesktopTableColumn("Nº ROMA",            weight = 1.0f),
    DesktopTableColumn("Año",                weight = 0.7f),
    DesktopTableColumn("Últ. inspección",    weight = 1.2f),
)

private val COLS_APLICADORES = listOf(
    DesktopTableColumn("Nombre",      weight = 1.5f),
    DesktopTableColumn("Email",       weight = 2.0f),
    DesktopTableColumn("Carnet ROPO", weight = 1.5f),
)

// ── Form state data classes ───────────────────────────────────────────────────

private data class TitularFs(
    val id: Int = 0,
    val nombre: String = "",
    val apellidos: String = "",
    val nif: String = "",
    val direccion: String = "",
    val localidad: String = "",
    val codigoPostal: String = "",
    val provincia: String = "",
    val telefono: String = "",
    val email: String = "",
)

private data class ExplotacionFs(
    val id: Int = 0,
    val nombre: String = "",
    val nifEmpresa: String = "",
    val registroNacional: String = "",
    val registroAutonomico: String = "",
    val direccion: String = "",
    val municipio: String = "",
    val provincia: String = "",
    val codigoPostal: String = "",
    val telefonoFijo: String = "",
    val telefonoMovil: String = "",
    val email: String = "",
)

private data class NuevoEquipoFs(
    val tipo: String = "",
    val marca: String = "",
    val modelo: String = "",
    val numeroRoma: String = "",
    val anyo: String = "",
    val fechaInspeccion: String = "",
)

private data class NuevoAplicadorFs(
    val nombre: String = "",
    val apellidos: String = "",
    val email: String = "",
    val password: String = "",
    val tipoCarnetRopo: String = "",
)

//pantalla
@Composable
fun ConfiguracionInicialSc(
    onVerInicio: () -> Unit,
    onVerActividades: () -> Unit,
    onVerParcelas: () -> Unit,
    onVerProductos: () -> Unit,
    onVerAjustes: () -> Unit,
    configuracionVm: ConfiguracionVm = koinViewModel(),
    equipoVm: EquipoVm = koinViewModel(),
    usuarioVm: UsuarioVm = koinViewModel(),
    ajustesVm: AjustesVm = koinViewModel(),
) {
    val titularResult     by configuracionVm.titular.collectAsState()
    val explotacionResult by configuracionVm.explotacion.collectAsState()
    val equiposResult     by equipoVm.equipos.collectAsState()
    val usuariosResult    by usuarioVm.usuarios.collectAsState()

    var activeTab         by remember { mutableStateOf(0) }
    val scrollState       = rememberScrollState()
    var titularFs         by remember { mutableStateOf(TitularFs()) }
    var explotacionFs     by remember { mutableStateOf(ExplotacionFs()) }
    var nuevoEquipoFs     by remember { mutableStateOf(NuevoEquipoFs()) }
    var nuevoAplicadorFs  by remember { mutableStateOf(NuevoAplicadorFs()) }
    var mensajeErrorAplicador by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        configuracionVm.cargarDatos()
        equipoVm.cargarEquipos()
        usuarioVm.cargarUsuarios(rol = "APLICADOR")
    }

    LaunchedEffect(activeTab) { scrollState.scrollTo(0) }

    LaunchedEffect(Unit) {
        usuarioVm.mensajeError.collect { error ->
            mensajeErrorAplicador = error
            delay(4_000)
            mensajeErrorAplicador = null
        }
    }

    // Sync form state when remote data arrives
    LaunchedEffect(titularResult) {
        val t = (titularResult as? Result.Success)?.data ?: return@LaunchedEffect
        titularFs = TitularFs(
            id           = t.id,
            nombre       = t.nombre,
            apellidos    = t.apellidos    ?: "",
            nif          = t.nif,
            direccion    = t.direccion    ?: "",
            localidad    = t.localidad    ?: "",
            codigoPostal = t.codigoPostal ?: "",
            provincia    = t.provincia    ?: "",
            telefono     = t.telefono     ?: "",
            email        = t.email        ?: "",
        )
    }

    LaunchedEffect(explotacionResult) {
        val e = (explotacionResult as? Result.Success)?.data ?: return@LaunchedEffect
        explotacionFs = ExplotacionFs(
            id                 = e.id,
            nombre             = e.nombre,
            nifEmpresa         = e.nifEmpresa         ?: "",
            registroNacional   = e.registroNacional   ?: "",
            registroAutonomico = e.registroAutonomico ?: "",
            direccion          = e.direccion          ?: "",
            municipio          = e.municipio          ?: "",
            provincia          = e.provincia          ?: "",
            codigoPostal       = e.codigoPostal       ?: "",
            telefonoFijo       = e.telefonoFijo       ?: "",
            telefonoMovil      = e.telefonoMovil      ?: "",
            email              = e.email              ?: "",
        )
    }

    val equipos: List<EquipoAplicacion> = (equiposResult as? Result.Success)?.data ?: emptyList()
    val aplicadores: List<Usuario>       = (usuariosResult as? Result.Success)?.data ?: emptyList()
    val explotacionIdActual: Int          = (explotacionResult as? Result.Success)?.data?.id ?: 0

    val topActions: List<DesktopTopBarAction> = when (activeTab) {
        0 -> listOf(DesktopTopBarAction(
            label   = "Guardar cambios",
            icon    = Icons.Outlined.Check,
            primary = true,
            onClick = {
                if (titularFs.nombre.isNotBlank() && titularFs.nif.isNotBlank()) {
                    configuracionVm.guardarTitular(
                        Titular(
                            id           = titularFs.id,
                            nombre       = titularFs.nombre,
                            apellidos    = titularFs.apellidos.ifBlank { null },
                            nif          = titularFs.nif,
                            direccion    = titularFs.direccion.ifBlank { null },
                            localidad    = titularFs.localidad.ifBlank { null },
                            codigoPostal = titularFs.codigoPostal.ifBlank { null },
                            provincia    = titularFs.provincia.ifBlank { null },
                            telefono     = titularFs.telefono.ifBlank { null },
                            email        = titularFs.email.ifBlank { null },
                        )
                    )
                }
            },
        ))
        1 -> listOf(DesktopTopBarAction(
            label   = "Guardar cambios",
            icon    = Icons.Outlined.Check,
            primary = true,
            onClick = {
                if (explotacionFs.nombre.isNotBlank()) {
                    configuracionVm.guardarExplotacion(
                        Explotacion(
                            id                 = explotacionFs.id,
                            nombre             = explotacionFs.nombre,
                            nifEmpresa         = explotacionFs.nifEmpresa.ifBlank { null },
                            registroNacional   = explotacionFs.registroNacional.ifBlank { null },
                            registroAutonomico = explotacionFs.registroAutonomico.ifBlank { null },
                            direccion          = explotacionFs.direccion.ifBlank { null },
                            municipio          = explotacionFs.municipio.ifBlank { null },
                            provincia          = explotacionFs.provincia.ifBlank { null },
                            codigoPostal       = explotacionFs.codigoPostal.ifBlank { null },
                            telefonoFijo       = explotacionFs.telefonoFijo.ifBlank { null },
                            telefonoMovil      = explotacionFs.telefonoMovil.ifBlank { null },
                            email              = explotacionFs.email.ifBlank { null },
                        )
                    )
                }
            },
        ))
        else -> emptyList()
    }

    DesktopWrapper(
        activeIndex   = 4,
        onNavigate    = { idx ->
            when (idx) {
                0    -> onVerInicio()
                1    -> onVerActividades()
                2    -> onVerParcelas()
                3    -> onVerProductos()
                5    -> onVerAjustes()
                else -> {}
            }
        },
        nombreUsuario = ajustesVm.nombreMostrado,
        rolUsuario    = ajustesVm.rolUsuario,
    ) {
        DesktopTopBar(
            title    = "Configuración de la explotación",
            subtitle = "Datos legales y de registro · RD 1311/2012",
            actions  = topActions,
        )
        DesktopTabBar(
            tabs          = listOf("Titular", "Explotación", "Equipos", "Aplicadores"),
            activeIndex   = activeTab,
            onTabSelected = { activeTab = it },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            when (activeTab) {
                0 -> TabTitular(fs = titularFs, onChange = { titularFs = it })
                1 -> TabExplotacion(fs = explotacionFs, onChange = { explotacionFs = it })
                2 -> TabEquipos(
                    equipos      = equipos,
                    nuevoEquipo  = nuevoEquipoFs,
                    onNuevoChange = { nuevoEquipoFs = it },
                    onAñadir     = {
                        if (nuevoEquipoFs.tipo.isNotBlank()) {
                            equipoVm.crearEquipo(
                                EquipoAplicacion(
                                    explotacionId         = explotacionIdActual,
                                    tipo                  = nuevoEquipoFs.tipo,
                                    marca                 = nuevoEquipoFs.marca.ifBlank { null },
                                    modelo                = nuevoEquipoFs.modelo.ifBlank { null },
                                    numeroRoma            = nuevoEquipoFs.numeroRoma.ifBlank { null },
                                    anyoFabricacion       = nuevoEquipoFs.anyo.toIntOrNull(),
                                    fechaUltimaInspeccion = nuevoEquipoFs.fechaInspeccion.ifBlank { null },
                                )
                            )
                            nuevoEquipoFs = NuevoEquipoFs()
                        }
                    },
                )
                3 -> {
                    mensajeErrorAplicador?.let { msg ->
                        FeedbackBanner(msg, esError = true) { mensajeErrorAplicador = null }
                    }
                    TabAplicadores(
                        aplicadores      = aplicadores,
                        nuevoAplicador   = nuevoAplicadorFs,
                        onNuevoChange    = { nuevoAplicadorFs = it },
                        onCrear          = {
                            if (nuevoAplicadorFs.nombre.isNotBlank() && isEmailValido(nuevoAplicadorFs.email)) {
                                usuarioVm.crearAplicador(
                                    usuario   = Usuario(
                                        nombre         = nuevoAplicadorFs.nombre,
                                        apellidos      = nuevoAplicadorFs.apellidos.ifBlank { null },
                                        email          = nuevoAplicadorFs.email,
                                        rol            = "APLICADOR",
                                        tipoCarnetRopo = nuevoAplicadorFs.tipoCarnetRopo.ifBlank { null },
                                    ),
                                    contrasena = nuevoAplicadorFs.password.ifBlank { null },
                                )
                                nuevoAplicadorFs = NuevoAplicadorFs()
                            }
                        },
                    )
                }
            }
        }
    }
}

// ── Tab 0: Titular ────────────────────────────────────────────────────────────

@Composable
private fun TabTitular(fs: TitularFs, onChange: (TitularFs) -> Unit) {
    SectionEyebrow("Datos del titular legal")
    FormCard {
        FormRow {
            DesktopFormField(
                label         = "Nombre completo",
                value         = fs.nombre,
                onValueChange = { onChange(fs.copy(nombre = it)) },
                modifier      = Modifier.weight(1f),
            )
            DesktopFormField(
                label         = "NIF / DNI",
                value         = fs.nif,
                onValueChange = { onChange(fs.copy(nif = it)) },
                modifier      = Modifier.weight(1f),
            )
        }
        FormRow {
            DesktopFormField(
                label         = "Apellidos",
                value         = fs.apellidos,
                onValueChange = { onChange(fs.copy(apellidos = it)) },
                modifier      = Modifier.weight(1f),
            )
            DesktopFormField(
                label         = "Teléfono",
                value         = fs.telefono,
                onValueChange = { onChange(fs.copy(telefono = it)) },
                modifier      = Modifier.weight(1f),
            )
        }
        FormRow {
            DesktopFormField(
                label         = "Dirección",
                value         = fs.direccion,
                onValueChange = { onChange(fs.copy(direccion = it)) },
                modifier      = Modifier.weight(1f),
            )
            DesktopFormField(
                label         = "Municipio / Localidad",
                value         = fs.localidad,
                onValueChange = { onChange(fs.copy(localidad = it)) },
                modifier      = Modifier.weight(1f),
            )
        }
        FormRow {
            DesktopFormField(
                label         = "Código postal",
                value         = fs.codigoPostal,
                onValueChange = { onChange(fs.copy(codigoPostal = it)) },
                modifier      = Modifier.weight(1f),
            )
            DesktopFormField(
                label         = "Provincia",
                value         = fs.provincia,
                onValueChange = { onChange(fs.copy(provincia = it)) },
                modifier      = Modifier.weight(1f),
            )
        }
        DesktopFormField(
            label         = "Correo electrónico",
            value         = fs.email,
            onValueChange = { onChange(fs.copy(email = it)) },
        )
    }
}

// ── Tab 1: Explotación ────────────────────────────────────────────────────────

@Composable
private fun TabExplotacion(fs: ExplotacionFs, onChange: (ExplotacionFs) -> Unit) {
    SectionEyebrow("Datos de la explotación agrícola")
    FormCard {
        DesktopFormField(
            label         = "Nombre de la explotación",
            value         = fs.nombre,
            onValueChange = { onChange(fs.copy(nombre = it)) },
        )
        FormRow {
            DesktopFormField(
                label         = "NIF empresa / autónomo",
                value         = fs.nifEmpresa,
                onValueChange = { onChange(fs.copy(nifEmpresa = it)) },
                modifier      = Modifier.weight(1f),
            )
            DesktopFormField(
                label         = "Registro nacional",
                value         = fs.registroNacional,
                onValueChange = { onChange(fs.copy(registroNacional = it)) },
                modifier      = Modifier.weight(1f),
            )
        }
        DesktopFormField(
            label         = "Registro autonómico",
            value         = fs.registroAutonomico,
            onValueChange = { onChange(fs.copy(registroAutonomico = it)) },
        )
        DesktopFormField(
            label         = "Dirección",
            value         = fs.direccion,
            onValueChange = { onChange(fs.copy(direccion = it)) },
        )
        FormRow {
            DesktopFormField(
                label         = "Municipio",
                value         = fs.municipio,
                onValueChange = { onChange(fs.copy(municipio = it)) },
                modifier      = Modifier.weight(1.5f),
            )
            DesktopFormField(
                label         = "Código postal",
                value         = fs.codigoPostal,
                onValueChange = { onChange(fs.copy(codigoPostal = it)) },
                modifier      = Modifier.weight(1f),
            )
            DesktopFormField(
                label         = "Provincia",
                value         = fs.provincia,
                onValueChange = { onChange(fs.copy(provincia = it)) },
                modifier      = Modifier.weight(1f),
            )
        }
        FormRow {
            DesktopFormField(
                label         = "Teléfono fijo",
                value         = fs.telefonoFijo,
                onValueChange = { onChange(fs.copy(telefonoFijo = it)) },
                modifier      = Modifier.weight(1f),
            )
            DesktopFormField(
                label         = "Teléfono móvil",
                value         = fs.telefonoMovil,
                onValueChange = { onChange(fs.copy(telefonoMovil = it)) },
                modifier      = Modifier.weight(1f),
            )
        }
        DesktopFormField(
            label         = "Correo electrónico",
            value         = fs.email,
            onValueChange = { onChange(fs.copy(email = it)) },
        )
    }
}

// ── Tab 2: Equipos ────────────────────────────────────────────────────────────

@Composable
private fun TabEquipos(
    equipos: List<EquipoAplicacion>,
    nuevoEquipo: NuevoEquipoFs,
    onNuevoChange: (NuevoEquipoFs) -> Unit,
    onAñadir: () -> Unit,
) {
    SectionEyebrow("Equipos de aplicación registrados")
    DesktopTableHeader(COLS_EQUIPOS)
    equipos.forEachIndexed { i, eq ->
        DesktopTableRow(
            columns = COLS_EQUIPOS,
            last    = i == equipos.lastIndex,
            cells   = listOf(
                {
                    Text(
                        text  = eq.tipo,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextoPrimario),
                    )
                },
                {
                    Text(
                        text  = listOfNotNull(eq.marca, eq.modelo).joinToString(" ").ifBlank { "—" },
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario),
                    )
                },
                {
                    Text(
                        text  = eq.numeroRoma ?: "—",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario),
                    )
                },
                {
                    Text(
                        text  = eq.anyoFabricacion?.toString() ?: "—",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario),
                    )
                },
                {
                    Text(
                        text  = eq.fechaUltimaInspeccion?.let { formatearFecha(it) } ?: "—",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario),
                    )
                },
            ),
        )
    }
    if (equipos.isEmpty()) {
        EmptyTableRow("Sin equipos registrados")
    }

    Spacer(Modifier.height(8.dp))

    InlineCreateCard(title = "Nuevo equipo de aplicación") {
        FormRow {
            DesktopFormField(
                label         = "Tipo de equipo",
                value         = nuevoEquipo.tipo,
                onValueChange = { onNuevoChange(nuevoEquipo.copy(tipo = it)) },
                placeholder   = "Pulverizador, Atomizador…",
                modifier      = Modifier.weight(1.5f),
            )
            DesktopFormField(
                label         = "Marca",
                value         = nuevoEquipo.marca,
                onValueChange = { onNuevoChange(nuevoEquipo.copy(marca = it)) },
                modifier      = Modifier.weight(1f),
            )
            DesktopFormField(
                label         = "Modelo",
                value         = nuevoEquipo.modelo,
                onValueChange = { onNuevoChange(nuevoEquipo.copy(modelo = it)) },
                modifier      = Modifier.weight(1f),
            )
        }
        FormRow {
            DesktopFormField(
                label         = "Nº ROMA",
                value         = nuevoEquipo.numeroRoma,
                onValueChange = { onNuevoChange(nuevoEquipo.copy(numeroRoma = it)) },
                modifier      = Modifier.weight(1f),
            )
            DesktopFormField(
                label         = "Año de fabricación",
                value         = nuevoEquipo.anyo,
                onValueChange = { onNuevoChange(nuevoEquipo.copy(anyo = it)) },
                modifier      = Modifier.weight(1f),
            )
            DesktopFormField(
                label         = "Última inspección (AAAA-MM-DD)",
                value         = nuevoEquipo.fechaInspeccion,
                onValueChange = { onNuevoChange(nuevoEquipo.copy(fechaInspeccion = it)) },
                modifier      = Modifier.weight(1.5f),
            )
        }
        Spacer(Modifier.height(4.dp))
        ActionButton(label = "Añadir equipo", icon = Icons.Outlined.Add, onClick = onAñadir)
    }
}

// ── Tab 3: Aplicadores ────────────────────────────────────────────────────────

@Composable
private fun TabAplicadores(
    aplicadores: List<Usuario>,
    nuevoAplicador: NuevoAplicadorFs,
    onNuevoChange: (NuevoAplicadorFs) -> Unit,
    onCrear: () -> Unit,
) {
    var ropoExpandido by remember { mutableStateOf(false) }

    SectionEyebrow("Aplicadores habilitados")
    DesktopTableHeader(COLS_APLICADORES)
    aplicadores.forEachIndexed { i, u ->
        DesktopTableRow(
            columns = COLS_APLICADORES,
            last    = i == aplicadores.lastIndex,
            cells   = listOf(
                {
                    Text(
                        text  = listOfNotNull(u.nombre, u.apellidos).joinToString(" "),
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextoPrimario),
                    )
                },
                {
                    Text(
                        text  = u.email,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario),
                    )
                },
                {
                    Text(
                        text  = u.tipoCarnetRopo ?: "—",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario),
                    )
                },
            ),
        )
    }
    if (aplicadores.isEmpty()) {
        EmptyTableRow("Sin aplicadores registrados")
    }

    Spacer(Modifier.height(8.dp))

    InlineCreateCard(title = "Nuevo aplicador habilitado") {
        FormRow {
            DesktopFormField(
                label         = "Nombre",
                value         = nuevoAplicador.nombre,
                onValueChange = { onNuevoChange(nuevoAplicador.copy(nombre = it)) },
                modifier      = Modifier.weight(1f),
            )
            DesktopFormField(
                label         = "Apellidos",
                value         = nuevoAplicador.apellidos,
                onValueChange = { onNuevoChange(nuevoAplicador.copy(apellidos = it)) },
                modifier      = Modifier.weight(1f),
            )
        }
        if (nuevoAplicador.email.isNotBlank() && !isEmailValido(nuevoAplicador.email)) {
            Text(
                text  = "Formato de email no válido",
                color = RojoEliminar,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        FormRow {
            DesktopFormField(
                label         = "Email",
                value         = nuevoAplicador.email,
                onValueChange = { onNuevoChange(nuevoAplicador.copy(email = it)) },
                modifier      = Modifier.weight(1.5f),
            )
            DesktopFormField(
                label         = "Contraseña provisional",
                value         = nuevoAplicador.password,
                onValueChange = { onNuevoChange(nuevoAplicador.copy(password = it)) },
                modifier      = Modifier.weight(1f),
            )
            Box(modifier = Modifier.weight(1f)) {
                DesktopSelectField(
                    label       = "Tipo de carné ROPO",
                    value       = nuevoAplicador.tipoCarnetRopo,
                    placeholder = "Sin carnet ROPO asignado",
                    onClick     = { ropoExpandido = true },
                )
                DropdownMenu(
                    expanded         = ropoExpandido,
                    onDismissRequest = { ropoExpandido = false },
                ) {
                    listOf("BASICO", "CUALIFICADO", "FUMIGADOR", "PILOTO").forEach { opcion ->
                        DropdownMenuItem(
                            text    = { Text(opcion) },
                            onClick = {
                                onNuevoChange(nuevoAplicador.copy(tipoCarnetRopo = opcion))
                                ropoExpandido = false
                            },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        ActionButton(label = "Crear aplicador", icon = Icons.Outlined.Group, onClick = onCrear)
    }
}

//helpers
@Composable
private fun SectionEyebrow(text: String) {
    Text(
        text     = text.uppercase(),
        style    = MaterialTheme.extraTypography.eyebrow,
        color    = TextoTerciario,
        modifier = Modifier.padding(bottom = 2.dp),
    )
}

@Composable
private fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(SuperficieSepia, RoundedCornerShape(12.dp))
            .border(1.dp, BordeNormal, RoundedCornerShape(12.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content             = content,
    )
}

@Composable
private fun FormRow(content: @Composable () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment     = Alignment.Bottom,
    ) { content() }
}

@Composable
private fun ActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier              = Modifier
            .border(2.dp, OlivaPrimario, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = OlivaPrimario,
            modifier           = Modifier.height(16.dp),
        )
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OlivaPrimario)
    }
}

@Composable
private fun EmptyTableRow(message: String) {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = message,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextoTerciario),
        )
    }
}

@Composable
private fun FeedbackBanner(mensaje: String, esError: Boolean, onDismiss: () -> Unit) {
    val color = if (esError) TerracotaAccent else OlivaPrimario
    val bg    = if (esError) TerracotaAccent.copy(alpha = 0.08f) else OlivaTint
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text     = mensaje,
            fontSize = 13.sp,
            color    = color,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector        = Icons.Outlined.Close,
            contentDescription = "Cerrar",
            tint               = color,
            modifier           = Modifier.size(16.dp).clickable(onClick = onDismiss),
        )
    }
}
