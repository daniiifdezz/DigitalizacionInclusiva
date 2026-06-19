package org.dferna14.project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.domain.model.EquipoAplicacion
import org.dferna14.project.domain.model.Explotacion
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.Titular
import org.dferna14.project.domain.model.Usuario
import org.dferna14.project.ui.components.CampoAvisoInfo
import org.dferna14.project.ui.components.CampoCard
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.theme.NaranjaClaro
import org.dferna14.project.ui.theme.NaranjaOscuro
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.VerdeValidada
import org.dferna14.project.ui.viewmodel.AuthVm
import org.dferna14.project.ui.viewmodel.ConfiguracionVm
import org.dferna14.project.ui.viewmodel.EquipoVm
import org.dferna14.project.ui.viewmodel.UsuarioVm
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionSc(
    onVolver: () -> Unit,
    viewModel: ConfiguracionVm = koinViewModel(),
    usuarioVm: UsuarioVm = koinViewModel(),
) {
    val titularState by viewModel.titular.collectAsState()
    val explotacionState by viewModel.explotacion.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    LaunchedEffect(Unit) {
        viewModel.guardadoExitoso.collect { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    LaunchedEffect(Unit) {
        usuarioVm.mensajeError.collect { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración Inicial") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("< Menú")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Titular", "Explotación", "Equipos", "Aplicadores")
                    .forEachIndexed { index, titulo ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(titulo) }
                        )
                    }
            }

            when (selectedTab) {
                0 -> TitularTab(
                    estado = titularState,
                    onReintentar = { viewModel.cargarDatos() },
                    onGuardar = { viewModel.guardarTitular(it) }
                )
                1 -> ExplotacionTab(
                    estado = explotacionState,
                    onReintentar = { viewModel.cargarDatos() },
                    onGuardar = { viewModel.guardarExplotacion(it) }
                )
                2 -> PestanaEquipos(snackbarHostState = snackbarHostState)
                3 -> PestanaAplicadores(snackbarHostState = snackbarHostState)
            }
        }
    }
}

@Composable
private fun TitularTab(
    estado: Result<Titular?>,
    onReintentar: () -> Unit,
    onGuardar: (Titular) -> Unit
) {
    when (estado) {
        is Result.Loading -> CenteredProgress()
        is Result.Error -> ErrorState(mensaje = estado.message, onReintentar = onReintentar)
        is Result.Success -> TitularForm(titularExistente = estado.data, onGuardar = onGuardar)
    }
}

@Composable
private fun ExplotacionTab(
    estado: Result<Explotacion?>,
    onReintentar: () -> Unit,
    onGuardar: (Explotacion) -> Unit
) {
    when (estado) {
        is Result.Loading -> CenteredProgress()
        is Result.Error -> ErrorState(mensaje = estado.message, onReintentar = onReintentar)
        is Result.Success -> ExplotacionForm(explotacionExistente = estado.data, onGuardar = onGuardar)
    }
}

@Composable
private fun CenteredProgress() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(mensaje: String, onReintentar: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onReintentar) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun TitularForm(
    titularExistente: Titular?,
    onGuardar: (Titular) -> Unit
) {
    // Cada campo con su propio remember (key=titularExistente para reinicializar
    // si tras un guardado vuelven datos nuevos del backend con id real)
    var nombre       by remember(titularExistente) { mutableStateOf(titularExistente?.nombre ?: "") }
    var apellidos    by remember(titularExistente) { mutableStateOf(titularExistente?.apellidos ?: "") }
    var nif          by remember(titularExistente) { mutableStateOf(titularExistente?.nif ?: "") }
    var direccion    by remember(titularExistente) { mutableStateOf(titularExistente?.direccion ?: "") }
    var localidad    by remember(titularExistente) { mutableStateOf(titularExistente?.localidad ?: "") }
    var codigoPostal by remember(titularExistente) { mutableStateOf(titularExistente?.codigoPostal ?: "") }
    var provincia    by remember(titularExistente) { mutableStateOf(titularExistente?.provincia ?: "") }
    var telefono     by remember(titularExistente) { mutableStateOf(titularExistente?.telefono ?: "") }
    var email        by remember(titularExistente) { mutableStateOf(titularExistente?.email ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text("Apellidos") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = nif,
            onValueChange = { nif = it },
            label = { Text("NIF *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = localidad,
            onValueChange = { localidad = it },
            label = { Text("Localidad") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = codigoPostal,
            onValueChange = { codigoPostal = it },
            label = { Text("Código postal") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = provincia,
            onValueChange = { provincia = it },
            label = { Text("Provincia") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onGuardar(
                    Titular(
                        id           = titularExistente?.id ?: 0,
                        nombre       = nombre.trim(),
                        apellidos    = apellidos.takeIf { it.isNotBlank() },
                        nif          = nif.trim(),
                        direccion    = direccion.takeIf { it.isNotBlank() },
                        localidad    = localidad.takeIf { it.isNotBlank() },
                        codigoPostal = codigoPostal.takeIf { it.isNotBlank() },
                        provincia    = provincia.takeIf { it.isNotBlank() },
                        telefono     = telefono.takeIf { it.isNotBlank() },
                        email        = email.takeIf { it.isNotBlank() }
                    )
                )
            },
            enabled = nombre.isNotBlank() && nif.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Guardar titular")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ExplotacionForm(
    explotacionExistente: Explotacion?,
    onGuardar: (Explotacion) -> Unit
) {
    var nombre             by remember(explotacionExistente) { mutableStateOf(explotacionExistente?.nombre ?: "") }
    var nifEmpresa         by remember(explotacionExistente) { mutableStateOf(explotacionExistente?.nifEmpresa ?: "") }
    var registroNacional   by remember(explotacionExistente) { mutableStateOf(explotacionExistente?.registroNacional ?: "") }
    var registroAutonomico by remember(explotacionExistente) { mutableStateOf(explotacionExistente?.registroAutonomico ?: "") }
    var direccion          by remember(explotacionExistente) { mutableStateOf(explotacionExistente?.direccion ?: "") }
    var localidad          by remember(explotacionExistente) { mutableStateOf(explotacionExistente?.municipio ?: "") }
    var codigoPostal       by remember(explotacionExistente) { mutableStateOf(explotacionExistente?.codigoPostal ?: "") }
    var provincia          by remember(explotacionExistente) { mutableStateOf(explotacionExistente?.provincia ?: "") }
    var telefonoFijo       by remember(explotacionExistente) { mutableStateOf(explotacionExistente?.telefonoFijo ?: "") }
    var telefonoMovil      by remember(explotacionExistente) { mutableStateOf(explotacionExistente?.telefonoMovil ?: "") }
    var email              by remember(explotacionExistente) { mutableStateOf(explotacionExistente?.email ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre / Razón social *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = nifEmpresa,
            onValueChange = { nifEmpresa = it },
            label = { Text("NIF") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = registroNacional,
            onValueChange = { registroNacional = it },
            label = { Text("Registro nacional") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = registroAutonomico,
            onValueChange = { registroAutonomico = it },
            label = { Text("Registro autonómico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = localidad,
            onValueChange = { localidad = it },
            label = { Text("Localidad") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = codigoPostal,
            onValueChange = { codigoPostal = it },
            label = { Text("Código postal") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = provincia,
            onValueChange = { provincia = it },
            label = { Text("Provincia") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = telefonoFijo,
            onValueChange = { telefonoFijo = it },
            label = { Text("Teléfono fijo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = telefonoMovil,
            onValueChange = { telefonoMovil = it },
            label = { Text("Teléfono móvil") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onGuardar(
                    Explotacion(
                        id                 = explotacionExistente?.id ?: 0,
                        nombre             = nombre.trim(),
                        titularId          = explotacionExistente?.titularId,
                        nifEmpresa         = nifEmpresa.takeIf { it.isNotBlank() },
                        registroNacional   = registroNacional.takeIf { it.isNotBlank() },
                        registroAutonomico = registroAutonomico.takeIf { it.isNotBlank() },
                        direccion          = direccion.takeIf { it.isNotBlank() },
                        municipio          = localidad.takeIf { it.isNotBlank() },
                        provincia          = provincia.takeIf { it.isNotBlank() },
                        codigoPostal       = codigoPostal.takeIf { it.isNotBlank() },
                        telefonoFijo       = telefonoFijo.takeIf { it.isNotBlank() },
                        telefonoMovil      = telefonoMovil.takeIf { it.isNotBlank() },
                        email              = email.takeIf { it.isNotBlank() }
                    )
                )
            },
            enabled = nombre.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Guardar explotación")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Pestaña Equipos ─────────────────────────────────────────────────────────

@Composable
private fun PestanaEquipos(
    snackbarHostState: SnackbarHostState,
    equipoVm: EquipoVm = koinViewModel()
) {
    val equiposState by equipoVm.equipos.collectAsState()
    val mensajeError by equipoVm.mensajeError.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var equipoAEliminar by remember { mutableStateOf<EquipoAplicacion?>(null) }

    LaunchedEffect(Unit) { equipoVm.cargarEquipos() }

    LaunchedEffect(mensajeError) {
        mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            equipoVm.limpiarMensajeError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Equipos de aplicación",
                style = MaterialTheme.typography.titleMedium
            )
            CampoPrimaryButton(
                text = "+ Nuevo equipo",
                onClick = { mostrarDialogo = true },
                modifier = Modifier.width(180.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        CampoAvisoInfo(
            mensaje = "Añade los equipos de aplicación (tractores, pulverizadores) con su número de inscripción ROMA"
        )

        Spacer(Modifier.height(12.dp))

        when (val estado = equiposState) {
            is Result.Loading -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = NaranjaPrimario) }
            is Result.Error -> Text(
                text = "Error al cargar equipos: ${estado.message}",
                color = RojoEliminar
            )
            is Result.Success -> {
                if (estado.data.isEmpty()) {
                    Text(
                        text = "No hay equipos registrados",
                        color = TextoTerciario,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(estado.data, key = { it.id }) { equipo ->
                            EquipoConfigCard(
                                equipo = equipo,
                                onEliminar = { equipoAEliminar = equipo }
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        NuevoEquipoDialog(
            onDismiss = { mostrarDialogo = false },
            onCrear = { nuevo ->
                equipoVm.crearEquipo(nuevo)
                mostrarDialogo = false
            }
        )
    }

    equipoAEliminar?.let { equipo ->
        AlertDialog(
            onDismissRequest = { equipoAEliminar = null },
            title = { Text("¿Eliminar equipo?") },
            text = {
                Text("¿Seguro que quieres eliminar \"${equipo.descripcionLegible()}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = {
                    equipoVm.eliminarEquipo(equipo.id)
                    equipoAEliminar = null
                }) {
                    Text("Eliminar", color = RojoEliminar, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { equipoAEliminar = null }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }
}

private fun EquipoAplicacion.descripcionLegible(): String =
    listOfNotNull(tipo, marca, modelo).joinToString(" ").ifBlank { "Equipo $id" }

@Composable
private fun EquipoConfigCard(
    equipo: EquipoAplicacion,
    onEliminar: () -> Unit
) {
    CampoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = equipo.descripcionLegible(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                equipo.numeroRoma?.let {
                    Text(
                        text = "ROMA: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoTerciario
                    )
                }
                equipo.fechaUltimaInspeccion?.let {
                    Text(
                        text = "Última inspección: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoTerciario
                    )
                }
            }
            IconButton(
                onClick = onEliminar,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    tint = TextoTerciario,
                    contentDescription = "Eliminar equipo"
                )
            }
        }
    }
}

@Composable
private fun NuevoEquipoDialog(
    onDismiss: () -> Unit,
    onCrear: (EquipoAplicacion) -> Unit
) {
    var tipo by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var numeroRoma by remember { mutableStateOf("") }
    var anyoFabricacion by remember { mutableStateOf("") }
    var fechaInspeccion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo equipo de aplicación") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoTextField(
                    label = "Tipo *",
                    value = tipo,
                    onValueChange = { tipo = it },
                    placeholder = "Ej: Pulverizador"
                )
                CampoTextField(
                    label = "Marca",
                    value = marca,
                    onValueChange = { marca = it },
                    placeholder = "Ej: John Deere"
                )
                CampoTextField(
                    label = "Modelo",
                    value = modelo,
                    onValueChange = { modelo = it },
                    placeholder = "Ej: 4030"
                )
                CampoTextField(
                    label = "Número ROMA",
                    value = numeroRoma,
                    onValueChange = { numeroRoma = it },
                    placeholder = "Ej: ROMA-12345"
                )
                CampoTextField(
                    label = "Año de fabricación",
                    value = anyoFabricacion,
                    onValueChange = { anyoFabricacion = it.filter { c -> c.isDigit() } },
                    placeholder = "Ej: 2018"
                )
                CampoTextField(
                    label = "Fecha última inspección",
                    value = fechaInspeccion,
                    onValueChange = { fechaInspeccion = it },
                    placeholder = "AAAA-MM-DD"
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = tipo.isNotBlank(),
                onClick = {
                    onCrear(
                        EquipoAplicacion(
                            id                    = 0,
                            tipo                  = tipo.trim(),
                            marca                 = marca.trim().ifBlank { null },
                            modelo                = modelo.trim().ifBlank { null },
                            numeroRoma            = numeroRoma.trim().ifBlank { null },
                            anyoFabricacion       = anyoFabricacion.toIntOrNull(),
                            fechaUltimaInspeccion = fechaInspeccion.trim().ifBlank { null }
                        )
                    )
                }
            ) {
                Text("Crear equipo", color = NaranjaPrimario, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextoSecundario)
            }
        }
    )
}

//  Pestaña Aplicadores

@Composable
private fun PestanaAplicadores(
    snackbarHostState: SnackbarHostState,
    usuarioVm: UsuarioVm = koinViewModel(),
    authVm: AuthVm = koinViewModel()
) {
    val usuariosState by usuarioVm.usuarios.collectAsState()
    val mensajeRol by usuarioVm.mensajeRol.collectAsState()
    val usuarioActual by authVm.usuarioActual.collectAsState()
    val idUsuarioActual = usuarioActual?.id
    var mostrarDialogo by remember { mutableStateOf(false) }
    var aplicadorAEliminar by remember { mutableStateOf<Usuario?>(null) }
    var mostrarDialogoPromocion by remember { mutableStateOf<Int?>(null) }
    var mostrarDialogoDegradacion by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) { usuarioVm.cargarUsuarios() }

    LaunchedEffect(mensajeRol) {
        mensajeRol?.let {
            snackbarHostState.showSnackbar(it)
            usuarioVm.limpiarMensajeRol()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aplicadores",
                style = MaterialTheme.typography.titleMedium
            )
            CampoPrimaryButton(
                text = "+ Nuevo aplicador",
                onClick = { mostrarDialogo = true },
                modifier = Modifier.width(200.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        CampoAvisoInfo(
            mensaje = "Añade los aplicadores autorizados. El email es obligatorio para que el aplicador pueda registrarse después en la app."
        )

        Spacer(Modifier.height(12.dp))

        when (val estado = usuariosState) {
            is Result.Loading -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = NaranjaPrimario) }
            is Result.Error -> Text(
                text = "Error al cargar aplicadores: ${estado.message}",
                color = RojoEliminar
            )
            is Result.Success -> {
                if (estado.data.isEmpty()) {
                    Text(
                        text = "No hay aplicadores registrados",
                        color = TextoTerciario,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(estado.data, key = { it.id }) { usuario ->
                            AplicadorConfigCard(
                                usuario = usuario,
                                idUsuarioActual = idUsuarioActual,
                                onPromover = { mostrarDialogoPromocion = it },
                                onDegradar = { mostrarDialogoDegradacion = it },
                                onEliminar = { aplicadorAEliminar = usuario }
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        NuevoAplicadorDialog(
            onDismiss = { mostrarDialogo = false },
            onCrear = { nuevo ->
                usuarioVm.crearAplicador(nuevo)
                mostrarDialogo = false
            }
        )
    }

    aplicadorAEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = { aplicadorAEliminar = null },
            title = { Text("¿Eliminar aplicador?") },
            text = {
                Text("¿Seguro que quieres eliminar a \"${usuario.nombreLegible()}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = {
                    usuarioVm.eliminarAplicador(usuario.id)
                    aplicadorAEliminar = null
                }) {
                    Text("Eliminar", color = RojoEliminar, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { aplicadorAEliminar = null }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }

    // Diálogo de promoción a técnico
    if (mostrarDialogoPromocion != null) {
        val usuarios = (usuariosState as? Result.Success)?.data.orEmpty()
        val usuarioAPromover = usuarios.firstOrNull { it.id == mostrarDialogoPromocion }
        AlertDialog(
            onDismissRequest = { mostrarDialogoPromocion = null },
            title = { Text("¿Promover a técnico?") },
            text = {
                Column {
                    Text("Vas a otorgar permisos de técnico a:")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${usuarioAPromover?.nombre.orEmpty()} ${usuarioAPromover?.apellidos.orEmpty()}".trim(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(usuarioAPromover?.email.orEmpty(), color = TextoSecundario, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Como técnico podrá validar actividades, gestionar productos " +
                            "y equipos, generar el cuaderno oficial y promover a otros " +
                            "usuarios. Asegúrate de que es alguien de confianza.",
                        fontSize = 13.sp,
                        color = TextoSecundario
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = mostrarDialogoPromocion!!
                    mostrarDialogoPromocion = null
                    usuarioVm.cambiarRolUsuario(id, "TECNICO")
                }) {
                    Text("Promover", color = NaranjaPrimario, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPromocion = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de degradación a agricultor
    if (mostrarDialogoDegradacion != null) {
        val usuarios = (usuariosState as? Result.Success)?.data.orEmpty()
        val usuarioADegradar = usuarios.firstOrNull { it.id == mostrarDialogoDegradacion }
        AlertDialog(
            onDismissRequest = { mostrarDialogoDegradacion = null },
            title = { Text("¿Degradar a agricultor?") },
            text = {
                Column {
                    Text("Vas a retirar los permisos de técnico a:")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${usuarioADegradar?.nombre.orEmpty()} ${usuarioADegradar?.apellidos.orEmpty()}".trim(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(usuarioADegradar?.email.orEmpty(), color = TextoSecundario, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Perderá acceso a la aplicación de escritorio y a las " +
                            "funciones de técnico. Podrá seguir usando la app móvil " +
                            "como agricultor.",
                        fontSize = 13.sp,
                        color = TextoSecundario
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = mostrarDialogoDegradacion!!
                    mostrarDialogoDegradacion = null
                    usuarioVm.cambiarRolUsuario(id, "AGRICULTOR")
                }) {
                    Text("Degradar", color = RojoEliminar, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoDegradacion = null }) { Text("Cancelar") }
            }
        )
    }
}

private fun Usuario.nombreLegible(): String =
    listOfNotNull(nombre, apellidos).joinToString(" ").ifBlank { email }

@Composable
private fun AplicadorConfigCard(
    usuario: Usuario,
    idUsuarioActual: Int?,
    onPromover: (Int) -> Unit,
    onDegradar: (Int) -> Unit,
    onEliminar: () -> Unit
) {
    CampoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombreLegible(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoTerciario
                )
                Spacer(Modifier.height(6.dp))
                // Rol: badge TECNICO (con opción de degradar) o botón "Promover a técnico"
                when (usuario.rol) {
                    "TECNICO" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(VerdeValidada, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.White
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "TECNICO",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            // Un técnico no puede degradarse a sí mismo (protección anti-suicidio)
                            if (usuario.id != idUsuarioActual) {
                                IconButton(
                                    onClick = { onDegradar(usuario.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Degradar a agricultor",
                                        tint = TextoSecundario,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    "AGRICULTOR" -> {
                        OutlinedButton(
                            onClick = { onPromover(usuario.id) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NaranjaPrimario),
                            border = BorderStroke(1.dp, NaranjaPrimario),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Promover a técnico", fontSize = 12.sp)
                        }
                    }
                    else -> {
                        Text("Rol: ${usuario.rol}", color = TextoSecundario, fontSize = 13.sp)
                    }
                }
                usuario.tipoCarnetRopo?.let { tipo ->
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(NaranjaClaro, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Carné: ${etiquetaCortaCarnet(tipo)}",
                            fontSize = 10.sp,
                            color = NaranjaOscuro,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            IconButton(
                onClick = onEliminar,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    tint = TextoTerciario,
                    contentDescription = "Eliminar aplicador"
                )
            }
        }
    }
}

private fun etiquetaCortaCarnet(tipo: String): String = when (tipo) {
    "BASICO"      -> "Básico"
    "CUALIFICADO" -> "Cualificado"
    "FUMIGADOR"   -> "Fumigador"
    "PILOTO"      -> "Piloto"
    else          -> tipo
}

private val OPCIONES_CARNET_ROPO = listOf(
    "BASICO"      to "Básico — manipulación general",
    "CUALIFICADO" to "Cualificado — productos tóxicos",
    "FUMIGADOR"   to "Fumigador — gases y vapores",
    "PILOTO"      to "Piloto — aplicación aérea"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuevoAplicadorDialog(
    onDismiss: () -> Unit,
    onCrear: (Usuario) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var tipoCarnet by remember { mutableStateOf<String?>(null) }
    var desplegableCarnet by remember { mutableStateOf(false) }

    val emailValido = email.isBlank() || email.contains("@")
    val confirmHabilitado = nombre.isNotBlank() && email.isNotBlank() && emailValido

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo aplicador") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoTextField(
                    label = "Nombre *",
                    value = nombre,
                    onValueChange = { nombre = it }
                )
                CampoTextField(
                    label = "Apellidos",
                    value = apellidos,
                    onValueChange = { apellidos = it }
                )
                CampoTextField(
                    label = "Email *",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "aplicador@dominio.com"
                )
                if (email.isNotBlank() && !emailValido) {
                    Text(
                        text = "Introduce un email válido",
                        color = RojoEliminar,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = desplegableCarnet,
                    onExpandedChange = { desplegableCarnet = it }
                ) {
                    OutlinedTextField(
                        value = OPCIONES_CARNET_ROPO.find { it.first == tipoCarnet }?.second
                            ?: "Selecciona tipo de carné (opcional)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de carné ROPO") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegableCarnet)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NaranjaPrimario
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = desplegableCarnet,
                        onDismissRequest = { desplegableCarnet = false }
                    ) {
                        OPCIONES_CARNET_ROPO.forEach { (valor, etiqueta) ->
                            DropdownMenuItem(
                                text = { Text(etiqueta, fontSize = 13.sp) },
                                onClick = {
                                    tipoCarnet = valor
                                    desplegableCarnet = false
                                }
                            )
                        }
                        if (tipoCarnet != null) {
                            DropdownMenuItem(
                                text = { Text("Quitar selección", fontSize = 13.sp, color = TextoTerciario) },
                                onClick = {
                                    tipoCarnet = null
                                    desplegableCarnet = false
                                }
                            )
                        }
                    }
                }
                CampoAvisoInfo(
                    mensaje = "El carné ROPO es obligatorio para aplicar fitosanitarios según el RD 1311/2012"
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = confirmHabilitado,
                onClick = {
                    onCrear(
                        Usuario(
                            id             = 0,
                            nombre         = nombre.trim(),
                            apellidos      = apellidos.trim().ifBlank { null },
                            email          = email.trim().lowercase(),
                            rol            = "AGRICULTOR",
                            tipoCarnetRopo = tipoCarnet
                        )
                    )
                }
            ) {
                Text("Crear aplicador", color = NaranjaPrimario, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextoSecundario)
            }
        }
    )
}
