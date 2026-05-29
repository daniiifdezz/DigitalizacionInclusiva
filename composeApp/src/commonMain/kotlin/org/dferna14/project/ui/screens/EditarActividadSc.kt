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
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ActividadDetalleVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarActividadSc(
    actividadId: Int,
    onVolver: () -> Unit,
    viewModel: ActividadDetalleVm = koinViewModel(),
    parcelaVm: ParcelaVm = koinViewModel()
) {
    val actividadState by viewModel.actividadActual.collectAsState()
    val parcelasState by parcelaVm.parcelas.collectAsState()
    val operacionExitosa by viewModel.operacionExitosa.collectAsState()

    var parcelaSeleccionada by remember { mutableStateOf<Parcela?>(null) }
    var fechaInicio by remember { mutableStateOf("") }
    var superficieTratada by remember { mutableStateOf("") }
    var problemaFitosanitario by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var desplegableParcela by remember { mutableStateOf(false) }
    var datosCargados by remember { mutableStateOf(false) }

    LaunchedEffect(actividadId) {
        viewModel.cargarActividad(actividadId)
    }

    LaunchedEffect(actividadState) {
        if (!datosCargados && actividadState is Result.Success) {
            val act = (actividadState as Result.Success).data
            fechaInicio = act.fechaInicio
            superficieTratada = act.superficieTratada?.toString() ?: ""
            problemaFitosanitario = act.problemaFitosanitario ?: ""
            observaciones = act.observaciones ?: ""
            datosCargados = true
        }
    }

    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            viewModel.resetOperacionExitosa()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Actividad") },
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
                    Text("Error al cargar la actividad")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.cargarActividad(actividadId) }) {
                        Text("Reintentar")
                    }
                }
            }
            is Result.Success -> {
                val parcelaInicial = (parcelasState as? Result.Success)?.data?.find { it.id == estado.data.parcelaId }

                LaunchedEffect(parcelaInicial) {
                    if (parcelaSeleccionada == null && parcelaInicial != null) parcelaSeleccionada = parcelaInicial
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = { fechaInicio = it },
                        label = { Text("Fecha de inicio (AAAA-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = desplegableParcela,
                        onExpandedChange = { desplegableParcela = it }
                    ) {
                        OutlinedTextField(
                            value = parcelaSeleccionada?.let { it.alias ?: "Parcela ${it.orden ?: it.id}" } ?: "Selecciona una parcela",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Parcela *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegableParcela) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = desplegableParcela,
                            onDismissRequest = { desplegableParcela = false }
                        ) {
                            when (val s = parcelasState) {
                                is Result.Success -> s.data.forEach { parcela ->
                                    DropdownMenuItem(
                                        text = { Text(parcela.alias ?: "Parcela ${parcela.orden ?: parcela.id}") },
                                        onClick = {
                                            parcelaSeleccionada = parcela
                                            desplegableParcela = false
                                        }
                                    )
                                }
                                else -> DropdownMenuItem(text = { Text("Cargando...") }, onClick = {})
                            }
                        }
                    }

                    OutlinedTextField(
                        value = superficieTratada,
                        onValueChange = { superficieTratada = it },
                        label = { Text("Superficie tratada (ha)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = problemaFitosanitario,
                        onValueChange = { problemaFitosanitario = it },
                        label = { Text("Problema fitosanitario") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val parcela = parcelaSeleccionada ?: return@Button
                            viewModel.actualizarActividad(
                                Actividad(
                                    id = actividadId,
                                    parcelaId = parcela.id,
                                    equipoId = null,
                                    aplicadorId = null,
                                    fechaInicio = fechaInicio,
                                    superficieTratada = superficieTratada.toDoubleOrNull(),
                                    problemaFitosanitario = problemaFitosanitario.ifBlank { null },
                                    observaciones = observaciones.ifBlank { null }
                                )
                            )
                        },
                        enabled = parcelaSeleccionada != null && fechaInicio.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Guardar cambios")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
