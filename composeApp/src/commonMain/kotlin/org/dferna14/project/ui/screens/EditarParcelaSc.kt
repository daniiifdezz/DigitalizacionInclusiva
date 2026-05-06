package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Cultivo
import org.dferna14.project.domain.model.DatosAgronomicos
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.ParcelaCompleta
import org.dferna14.project.domain.model.ReferenciaSigpac
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.koin.compose.viewmodel.koinViewModel

private val ECOREGIMEN_OPCIONES = listOf("P1", "P2A", "P3", "P4", "P5", "P6", "P7")
private val SECANO_REGADIO_OPCIONES = listOf("Secano", "Regadío")
private val AIRE_LIBRE_PROTEGIDO_OPCIONES = mapOf(
    "AL"  to "Aire libre",
    "M"   to "Malla",
    "BP"  to "Bajo plástico",
    "INV" to "Invernadero"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarParcelaSc(
    parcelaId: Int,
    onVolver: () -> Unit,
    viewModel: ParcelaVm = koinViewModel()
) {
    val parcelaCompletaState by viewModel.parcelaCompleta.collectAsState()
    val cultivosState by viewModel.cultivos.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(parcelaId) {
        viewModel.cargarParcelaCompleta(parcelaId)
        viewModel.cargarCultivos()
    }

    LaunchedEffect(Unit) {
        viewModel.guardadoExitoso.collect { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Parcela #$parcelaId") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("< Volver")
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
                    text = { Text("Datos básicos") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("SIGPAC") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Agronómicos") }
                )
            }

            when (val estado = parcelaCompletaState) {
                is Result.Loading -> CenteredProgressBox()
                is Result.Error -> ErrorBox(
                    mensaje = estado.message,
                    onReintentar = { viewModel.cargarParcelaCompleta(parcelaId) }
                )
                is Result.Success -> {
                    val completa = estado.data
                    if (completa == null) {
                        ErrorBox(
                            mensaje = "La parcela #$parcelaId no existe",
                            onReintentar = { viewModel.cargarParcelaCompleta(parcelaId) }
                        )
                    } else {
                        when (selectedTab) {
                            0 -> DatosBasicosTab(parcela = completa.parcela)
                            1 -> SigpacTab(
                                parcelaId = parcelaId,
                                sigpacExistente = completa.referenciaSigpac,
                                onGuardar = { viewModel.guardarSigpac(parcelaId, it) }
                            )
                            2 -> AgronomicosTab(
                                parcelaId = parcelaId,
                                agronomicoExistente = completa.datosAgronomicos,
                                cultivosState = cultivosState,
                                onGuardar = { viewModel.guardarAgronomico(parcelaId, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredProgressBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBox(mensaje: String, onReintentar: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(mensaje, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onReintentar) { Text("Reintentar") }
        }
    }
}

// ── Tab 1: Datos básicos ──────────────────────────────────────────────────────

@Composable
private fun DatosBasicosTab(parcela: Parcela) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReadOnlyField("ID parcela", parcela.id.toString())
        ReadOnlyField("Alias", parcela.alias ?: "—")
        ReadOnlyField("Orden", parcela.orden?.toString() ?: "—")
        ReadOnlyField("Sistema asesoramiento", parcela.sistemaAsesoramiento ?: "—")
        ReadOnlyField(
            "Zona nitratos",
            parcela.zonaNitratos?.let { if (it) "Sí" else "No" } ?: "—"
        )
        ReadOnlyField("Explotación", parcela.explotacionId?.toString() ?: "—")

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "La edición de datos básicos se gestiona desde la pantalla de creación. " +
                    "Aquí los mostramos para referencia mientras editas SIGPAC y agronómicos.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReadOnlyField(label: String, valor: String) {
    OutlinedTextField(
        value = valor,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth()
    )
}

// ── Tab 2: SIGPAC ─────────────────────────────────────────────────────────────

@Composable
private fun SigpacTab(
    parcelaId: Int,
    sigpacExistente: ReferenciaSigpac?,
    onGuardar: (ReferenciaSigpac) -> Unit
) {
    var provincia        by remember(sigpacExistente) { mutableStateOf(sigpacExistente?.provincia ?: "") }
    var terminoMunicipal by remember(sigpacExistente) { mutableStateOf(sigpacExistente?.terminoMunicipal ?: "") }
    var codigoAgregado   by remember(sigpacExistente) { mutableStateOf(sigpacExistente?.codigoAgregado ?: "") }
    var zona             by remember(sigpacExistente) { mutableStateOf(sigpacExistente?.zona ?: "") }
    var numeroPoligono   by remember(sigpacExistente) { mutableStateOf(sigpacExistente?.numeroPoligono ?: "") }
    var numeroParcela    by remember(sigpacExistente) { mutableStateOf(sigpacExistente?.numeroParcela ?: "") }
    var numeroRecinto    by remember(sigpacExistente) { mutableStateOf(sigpacExistente?.numeroRecinto ?: "") }
    var usoSigpac        by remember(sigpacExistente) { mutableStateOf(sigpacExistente?.usoSigpac ?: "") }
    var superficieHa     by remember(sigpacExistente) { mutableStateOf(sigpacExistente?.superficieHa?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(value = provincia,        onValueChange = { provincia = it },        label = { Text("Provincia") },         singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = terminoMunicipal, onValueChange = { terminoMunicipal = it }, label = { Text("Término municipal") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = codigoAgregado,   onValueChange = { codigoAgregado = it },   label = { Text("Código agregado") },   singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = zona,             onValueChange = { zona = it },             label = { Text("Zona") },              singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = numeroPoligono,   onValueChange = { numeroPoligono = it },   label = { Text("Número polígono") },   singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = numeroParcela,    onValueChange = { numeroParcela = it },    label = { Text("Número parcela") },    singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = numeroRecinto,    onValueChange = { numeroRecinto = it },    label = { Text("Número recinto") },    singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = usoSigpac,        onValueChange = { usoSigpac = it },        label = { Text("Uso SIGPAC") },        singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = superficieHa,
            onValueChange = { superficieHa = it },
            label = { Text("Superficie (ha)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onGuardar(
                    ReferenciaSigpac(
                        id               = sigpacExistente?.id ?: 0,
                        parcelaId        = parcelaId,
                        provincia        = provincia.takeIf { it.isNotBlank() },
                        terminoMunicipal = terminoMunicipal.takeIf { it.isNotBlank() },
                        codigoAgregado   = codigoAgregado.takeIf { it.isNotBlank() },
                        zona             = zona.takeIf { it.isNotBlank() },
                        numeroPoligono   = numeroPoligono.takeIf { it.isNotBlank() },
                        numeroParcela    = numeroParcela.takeIf { it.isNotBlank() },
                        numeroRecinto    = numeroRecinto.takeIf { it.isNotBlank() },
                        usoSigpac        = usoSigpac.takeIf { it.isNotBlank() },
                        superficieHa     = superficieHa.toDoubleOrNull()
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Guardar datos SIGPAC")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Tab 3: Datos Agronómicos ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgronomicosTab(
    parcelaId: Int,
    agronomicoExistente: DatosAgronomicos?,
    cultivosState: Result<List<Cultivo>>,
    onGuardar: (DatosAgronomicos) -> Unit
) {
    var especieVariedad    by remember(agronomicoExistente) { mutableStateOf(agronomicoExistente?.especieVariedad ?: "") }
    var ecoregimenPractica by remember(agronomicoExistente) { mutableStateOf(agronomicoExistente?.ecoregimenPractica ?: "") }
    var secanoRegadio      by remember(agronomicoExistente) { mutableStateOf(agronomicoExistente?.secanoRegadio ?: "") }
    var cultivoId          by remember(agronomicoExistente) { mutableStateOf(agronomicoExistente?.cultivoId) }
    var fechaInicio        by remember(agronomicoExistente) { mutableStateOf(agronomicoExistente?.fechaInicio ?: "") }
    var fechaFin           by remember(agronomicoExistente) { mutableStateOf(agronomicoExistente?.fechaFin ?: "") }
    var aireLibreProtegido by remember(agronomicoExistente) { mutableStateOf(agronomicoExistente?.aireLibreProtegido ?: "") }

    var ecoOpen      by remember { mutableStateOf(false) }
    var secOpen      by remember { mutableStateOf(false) }
    var cultivoOpen  by remember { mutableStateOf(false) }
    var aireOpen     by remember { mutableStateOf(false) }

    val cultivoSeleccionado = (cultivosState as? Result.Success)?.data?.find { it.id == cultivoId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = especieVariedad,
            onValueChange = { especieVariedad = it },
            label = { Text("Especie / Variedad") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        DropdownTextField(
            value = ecoregimenPractica,
            label = "Eco-régimen / Práctica",
            opciones = ECOREGIMEN_OPCIONES,
            etiquetaPara = { it },
            expanded = ecoOpen,
            onExpandedChange = { ecoOpen = it },
            onSeleccionar = { ecoregimenPractica = it; ecoOpen = false }
        )

        DropdownTextField(
            value = secanoRegadio,
            label = "Secano / Regadío",
            opciones = SECANO_REGADIO_OPCIONES,
            etiquetaPara = { it },
            expanded = secOpen,
            onExpandedChange = { secOpen = it },
            onSeleccionar = { secanoRegadio = it; secOpen = false }
        )

        ExposedDropdownMenuBox(
            expanded = cultivoOpen,
            onExpandedChange = { cultivoOpen = it }
        ) {
            OutlinedTextField(
                value = cultivoSeleccionado
                    ?.let { listOfNotNull(it.especie, it.variedad).joinToString(" - ").ifBlank { "Cultivo ${it.id}" } }
                    ?: "Selecciona cultivo",
                onValueChange = {},
                readOnly = true,
                label = { Text("Cultivo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cultivoOpen) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = cultivoOpen,
                onDismissRequest = { cultivoOpen = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sin asignar") },
                    onClick = { cultivoId = null; cultivoOpen = false }
                )
                when (val s = cultivosState) {
                    is Result.Success -> s.data.forEach { c ->
                        DropdownMenuItem(
                            text = {
                                Text(listOfNotNull(c.especie, c.variedad).joinToString(" - ").ifBlank { "Cultivo ${c.id}" })
                            },
                            onClick = { cultivoId = c.id; cultivoOpen = false }
                        )
                    }
                    is Result.Error -> DropdownMenuItem(text = { Text("Error al cargar cultivos") }, onClick = {})
                    is Result.Loading -> DropdownMenuItem(text = { Text("Cargando...") }, onClick = {})
                }
            }
        }

        OutlinedTextField(
            value = fechaInicio,
            onValueChange = { fechaInicio = it },
            label = { Text("Fecha inicio (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fechaFin,
            onValueChange = { fechaFin = it },
            label = { Text("Fecha fin (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        DropdownTextField(
            value = aireLibreProtegido,
            label = "Aire libre / Protegido",
            opciones = AIRE_LIBRE_PROTEGIDO_OPCIONES.keys.toList(),
            etiquetaPara = { codigo -> "$codigo — ${AIRE_LIBRE_PROTEGIDO_OPCIONES[codigo].orEmpty()}" },
            expanded = aireOpen,
            onExpandedChange = { aireOpen = it },
            onSeleccionar = { aireLibreProtegido = it; aireOpen = false }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onGuardar(
                    DatosAgronomicos(
                        id                 = agronomicoExistente?.id ?: 0,
                        parcelaId          = parcelaId,
                        especieVariedad    = especieVariedad.takeIf { it.isNotBlank() },
                        ecoregimenPractica = ecoregimenPractica.takeIf { it.isNotBlank() },
                        secanoRegadio      = secanoRegadio.takeIf { it.isNotBlank() },
                        cultivoId          = cultivoId,
                        fechaInicio        = fechaInicio.takeIf { it.isNotBlank() },
                        fechaFin           = fechaFin.takeIf { it.isNotBlank() },
                        aireLibreProtegido = aireLibreProtegido.takeIf { it.isNotBlank() }
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Guardar datos agronómicos")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownTextField(
    value: String,
    label: String,
    opciones: List<String>,
    etiquetaPara: (String) -> String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSeleccionar: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = if (value.isBlank()) "" else etiquetaPara(value),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(etiquetaPara(opcion)) },
                    onClick = { onSeleccionar(opcion) }
                )
            }
        }
    }
}
