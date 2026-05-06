package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Explotacion
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.Titular
import org.dferna14.project.ui.viewmodel.ConfiguracionVm
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionSc(
    onVolver: () -> Unit,
    viewModel: ConfiguracionVm = koinViewModel()
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
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Titular") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Explotación") }
                )
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
