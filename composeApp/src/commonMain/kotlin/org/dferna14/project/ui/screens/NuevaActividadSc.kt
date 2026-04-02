package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ActividadViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/**
 * Pantalla de registro de nueva actividad agrícola.
 * Diseñada para uso en campo — campos mínimos, botones grandes.
 * La fecha de inicio se rellena automáticamente con la fecha actual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaActividadSc(
    onVolver: () -> Unit,
    viewModel: ActividadViewModel = koinViewModel()
) {
    // Estado del forms
    val parcelasState by viewModel.parcelas.collectAsState()
    val operacionExitosa by viewModel.operacionExitosa.collectAsState()

    var parcelaSeleccionada by remember { mutableStateOf<Parcela?>(null) }
    var superficieTratada by remember { mutableStateOf("") }
    var problemaFitosanitario by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var desplegableAbierto by remember { mutableStateOf(false) }

    // Fecha actual
    val fechaHoy = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

    // Cuando la operación es exitosa volvemos al listado
    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            viewModel.resetOperacionExitosa()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Actividad") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("← Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Fecha automatica
            OutlinedTextField(
                value = fechaHoy,
                onValueChange = {},
                label = { Text("Fecha de registro") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Seleccionamos parcela
            ExposedDropdownMenuBox(
                expanded = desplegableAbierto,
                onExpandedChange = { desplegableAbierto = it }
            ) {
                OutlinedTextField(
                    value = parcelaSeleccionada?.let { "Parcela ${it.orden ?: it.id}" } ?: "Selecciona una parcela",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Parcela *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegableAbierto) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = desplegableAbierto,
                    onDismissRequest = { desplegableAbierto = false }
                ) {
                    when (val estado = parcelasState) {
                        is Result.Success -> {
                            estado.data.forEach { parcela ->
                                DropdownMenuItem(
                                    text = { Text("Parcela ${parcela.orden ?: parcela.id}") },
                                    onClick = {
                                        parcelaSeleccionada = parcela
                                        desplegableAbierto = false
                                    }
                                )
                            }
                        }
                        else -> {
                            DropdownMenuItem(
                                text = { Text("Cargando parcelas...") },
                                onClick = {}
                            )
                        }
                    }
                }
            }

            // Superficie tratada
            OutlinedTextField(
                value = superficieTratada,
                onValueChange = { superficieTratada = it },
                label = { Text("Superficie tratada (ha)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Problema fitosanitario
            OutlinedTextField(
                value = problemaFitosanitario,
                onValueChange = { problemaFitosanitario = it },
                label = { Text("Problema fitosanitario") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            // Observaciones
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones (opcional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // Boton guardar
            Button(
                onClick = {
                    val parcela = parcelaSeleccionada ?: return@Button
                    viewModel.crearActividad(
                        Actividad(
                            parcelaId             = parcela.id,
                            fechaInicio           = fechaHoy,
                            superficieTratada     = superficieTratada.toDoubleOrNull(),
                            problemaFitosanitario = problemaFitosanitario.ifBlank { null },
                            observaciones         = observaciones.ifBlank { null }
                        )
                    )
                },
                enabled = parcelaSeleccionada != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Guardar actividad")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}