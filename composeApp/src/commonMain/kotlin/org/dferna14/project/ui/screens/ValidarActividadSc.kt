package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.EstadoActividad
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ActividadViewModel
import org.koin.compose.viewmodel.koinViewModel

val VALORES_EFICACIA = listOf("ALTA", "MEDIA", "BAJA", "NULA")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidarActividadSc(
    actividadId: Int,
    onVolver: () -> Unit,
    viewModel: ActividadViewModel = koinViewModel()
) {
    val actividadState by viewModel.actividadActual.collectAsState()
    val operacionExitosa by viewModel.operacionExitosa.collectAsState()
    var datosCargados by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Datos", "Productos", "Parcela", "Validar")

    // Campos para validar
    var fechaFin by remember { mutableStateOf("") }
    var eficacia by remember { mutableStateOf("") }
    var aplicador by remember { mutableStateOf("") }
    var equipo by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    var errorFecha by remember { mutableStateOf<String?>(null) }
    var errorEficacia by remember { mutableStateOf<String?>(null) }

    fun validarFormatoFecha(fecha: String): Boolean {
        if (fecha.isBlank()) return false
        val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        return regex.matches(fecha)
    }

    fun esFechaPosterior(fechaFin: String, fechaInicio: String): Boolean {
        if (fechaFin.isBlank() || fechaInicio.isBlank()) return true
        return fechaFin >= fechaInicio
    }

    fun validarEficaciaValue(eficacia: String): Boolean {
        if (eficacia.isBlank()) return false
        return VALORES_EFICACIA.contains(eficacia.uppercase())
    }

    LaunchedEffect(actividadId) {
        viewModel.cargarActividad(actividadId)
    }

    LaunchedEffect(actividadState) {
        if (!datosCargados && actividadState is Result.Success) {
            val act = (actividadState as Result.Success).data
            fechaFin = act.fechaFin ?: ""
            eficacia = act.eficacia ?: ""
            observaciones = act.observaciones ?: ""
            datosCargados = true
        }
    }

    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Validar Actividad") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("< Volver")
                    }
                }
            )
        }
    ) { padding ->
        when (val estado = actividadState) {
            is Result.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Result.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Error al cargar")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.cargarActividad(actividadId) }) {
                        Text("Reintentar")
                    }
                }
            }
            is Result.Success -> {
                val act = estado.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Tabs
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    // Contenido según pestaña
                    when (selectedTab) {
                        0 -> PestanaDatos(act = act)
                        1 -> PestanaProductos(act = act)
                        2 -> PestanaParcela(act = act)
                        3 -> PestanaValidar(
                            act = act,
                            fechaFin = fechaFin,
                            eficacia = eficacia,
                            aplicador = aplicador,
                            equipo = equipo,
                            observaciones = observaciones,
                            errorFecha = errorFecha,
                            errorEficacia = errorEficacia,
                            onFechaFinChange = { fechaFin = it },
                            onEficaciaChange = { eficacia = it },
                            onAplicadorChange = { aplicador = it },
                            onEquipoChange = { equipo = it },
                            onObservacionesChange = { observaciones = it },
                            onValidar = {
                                val actActualizada = act.copy(
                                    fechaFin = fechaFin,
                                    eficacia = eficacia.uppercase(),
                                    observaciones = observaciones.ifBlank { null },
                                    estado = EstadoActividad.VALIDADA
                                )
                                viewModel.actualizarActividad(actActualizada)
                                viewModel.resetOperacionExitosa()
                                onVolver()
                            },
                            onDevolver = {
                                viewModel.devolverActividad(actividadId)
                            },
                            validarFormatoFecha = { validarFormatoFecha(it) },
                            esFechaPosterior = { f, i -> esFechaPosterior(f, i) },
                            validarEficaciaValue = { validarEficaciaValue(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PestanaDatos(act: Actividad) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Datos de la actividad",
            style = MaterialTheme.typography.titleMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FieldView("Parcela", "Parcela ${act.parcelaId}")
                FieldView("Fecha inicio", act.fechaInicio)
                act.fechaFin?.let { FieldView("Fecha fin", it) }
                act.superficieTratada?.let { FieldView("Superficie tratadas", "$it ha") }
            }
        }

        Text(
            text = "Problema fitosanitario",
            style = MaterialTheme.typography.titleSmall
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = act.problemaFitosanitario ?: "No especificadis",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(
            text = "Observaciones del agricultor",
            style = MaterialTheme.typography.titleSmall
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = act.observaciones ?: "Sin observaciones",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PestanaProductos(act: Actividad) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Productos utilizados",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Esta funcionalidad requiere implementación adicional",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun PestanaParcela(act: Actividad) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Datos de la parcela (SIGPAC)",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Parcela ID: ${act.parcelaId}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Esta funcionalidad requiere implementación adicional",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun PestanaValidar(
    act: Actividad,
    fechaFin: String,
    eficacia: String,
    aplicador: String,
    equipo: String,
    observaciones: String,
    errorFecha: String?,
    errorEficacia: String?,
    onFechaFinChange: (String) -> Unit,
    onEficaciaChange: (String) -> Unit,
    onAplicadorChange: (String) -> Unit,
    onEquipoChange: (String) -> Unit,
    onObservacionesChange: (String) -> Unit,
    onValidar: () -> Unit,
    onDevolver: () -> Unit,
    validarFormatoFecha: (String) -> Boolean,
    esFechaPosterior: (String, String) -> Boolean,
    validarEficaciaValue: (String) -> Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Completar datos de validación:",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = fechaFin,
            onValueChange = { newValue ->
                onFechaFinChange(newValue)
            },
            label = { Text("Fecha fin *") },
            isError = errorFecha != null,
            supportingText = errorFecha?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = eficacia,
            onValueChange = { newValue ->
                onEficaciaChange(newValue.uppercase())
            },
            label = { Text("Eficacia *") },
            isError = errorEficacia != null,
            supportingText = errorEficacia?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = aplicador,
            onValueChange = onAplicadorChange,
            label = { Text("Aplicador (nombre)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = equipo,
            onValueChange = onEquipoChange,
            label = { Text("Equipo usado") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = observaciones,
            onValueChange = onObservacionesChange,
            label = { Text("Observaciones técnicas") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDevolver,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Devolver")
            }

            val esValido = fechaFin.isNotBlank() &&
                    validarFormatoFecha(fechaFin) &&
                    esFechaPosterior(fechaFin, act.fechaInicio) &&
                    validarEficaciaValue(eficacia)

            Button(
                onClick = onValidar,
                enabled = esValido,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Validar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FieldView(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}