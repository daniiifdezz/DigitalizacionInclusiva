package org.dferna14.project.ui.screens

import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.dferna14.project.domain.model.Fertilizacion
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ActividadViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizacionSc(
    cultivoId: Int,
    onVolver: () -> Unit,
    viewModel: ActividadViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    var guardando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fertilización Básica") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("< Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FertilizacionForm(
                cultivoId = cultivoId,
                onGuardar = { nuevaFertilizacion ->
                    // 2. Ejecutamos la llamada suspendida dentro del scope
                    scope.launch {
                        guardando = true
                        mensajeError = null
                        val resultado = viewModel.crearFertilizacion(nuevaFertilizacion)
                        guardando = false
                        if (resultado is Result.Error) {
                            mensajeError = resultado.message
                        } else {
                            onVolver()
                        }
                    }
                }
            )

            if (guardando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            mensajeError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun FertilizacionForm(
    cultivoId: Int,
    onGuardar: (Fertilizacion) -> Unit
) {
    var aplica by remember { mutableStateOf(false) }
    var fechaInicio by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()) }
    var fechaFin by remember { mutableStateOf("") }
    var tipoProducto by remember { mutableStateOf("") }
    var numeroAlbaran by remember { mutableStateOf("") }
    var riquezaNPK by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var tipoFertilizacion by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿Aplica fertilización?", modifier = Modifier.weight(1f))
            Switch(
                checked = aplica,
                onCheckedChange = { aplica = it }
            )
        }

        if (aplica) {
            OutlinedTextField(
                value = fechaInicio,
                onValueChange = { fechaInicio = it },
                label = { Text("Fecha inicio") },
                placeholder = { Text("YYYY-MM-DD") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fechaFin,
                onValueChange = { fechaFin = it },
                label = { Text("Fecha fin (opcional)") },
                placeholder = { Text("YYYY-MM-DD") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tipoProducto,
                onValueChange = { tipoProducto = it },
                label = { Text("Tipo de producto") },
                placeholder = { Text("Ej: Orgánico, Químico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = numeroAlbaran,
                onValueChange = { numeroAlbaran = it },
                label = { Text("Número de albarán") },
                placeholder = { Text("Ej: A-2024-001") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = riquezaNPK,
                onValueChange = { riquezaNPK = it },
                label = { Text("Riqueza NPK") },
                placeholder = { Text("Ej: 15-15-15") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dosis,
                onValueChange = { dosis = it },
                label = { Text("Dosis (kg/ha)") },
                placeholder = { Text("Ej: 300") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tipoFertilizacion,
                onValueChange = { tipoFertilizacion = it },
                label = { Text("Tipo de fertilización") },
                placeholder = { Text("Ej: Foliar, Radicular") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones") },
                placeholder = { Text("Notas adicionales...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onGuardar(
                    Fertilizacion(
                        id = 0,
                        cultivoId = cultivoId,
                        aplica = aplica,
                        fechaInicio = if (aplica) fechaInicio else null,
                        fechaFin = if (aplica && fechaFin.isNotBlank()) fechaFin else null,
                        tipoProducto = if (aplica) tipoProducto.ifBlank { null } else null,
                        numeroAlbaran = if (aplica) numeroAlbaran.ifBlank { null } else null,
                        riquezaNPK = if (aplica) riquezaNPK.ifBlank { null } else null,
                        dosis = if (aplica) dosis.toDoubleOrNull() else null,
                        tipoFertilizacion = if (aplica) tipoFertilizacion.ifBlank { null } else null,
                        observaciones = if (aplica) observaciones.ifBlank { null } else null
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}
