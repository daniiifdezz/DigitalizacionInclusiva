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
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Información de la parcela
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Parcela: ${act.parcelaId}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Fecha inicio: ${act.fechaInicio}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Text(
                        text = "Completar datos de validación:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Fecha fin - OBLIGATORIA
                    OutlinedTextField(
                        value = fechaFin,
                        onValueChange = { newValue ->
                            fechaFin = newValue
                            errorFecha = when {
                                newValue.isBlank() -> "Obligatorio"
                                !validarFormatoFecha(newValue) -> "Formato: AAAA-MM-DD"
                                !esFechaPosterior(newValue, act.fechaInicio) -> "Debe ser >= fecha inicio"
                                else -> null
                            }
                        },
                        label = { Text("Fecha fin *") },
                        isError = errorFecha != null,
                        supportingText = errorFecha?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Eficacia - OBLIGATORIA
                    OutlinedTextField(
                        value = eficacia,
                        onValueChange = { newValue ->
                            eficacia = newValue.uppercase()
                            errorEficacia = when {
                                newValue.isBlank() -> "Obligatorio"
                                !validarEficaciaValue(newValue) -> "Valores: ALTA, MEDIA, BAJA, NULA"
                                else -> null
                            }
                        },
                        label = { Text("Eficacia *") },
                        isError = errorEficacia != null,
                        supportingText = errorEficacia?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = aplicador,
                        onValueChange = { aplicador = it },
                        label = { Text("Aplicador (nombre)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = equipo,
                        onValueChange = { equipo = it },
                        label = { Text("Equipo usado") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones técnicas") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.devolverActividad(actividadId)
                            },
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
                            onClick = {
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
        }
    }
}
