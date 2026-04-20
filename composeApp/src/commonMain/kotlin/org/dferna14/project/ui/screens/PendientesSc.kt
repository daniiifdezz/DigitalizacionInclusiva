package org.dferna14.project.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendientesSc(
    onVolver: () -> Unit,
    onVerActividad: (Int) -> Unit,
    viewModel: ActividadViewModel = koinViewModel()
) {
    val pendientesState by viewModel.actividadesPendientes.collectAsState()
    var filtroEstado by remember { mutableStateOf("") }
    var filtroFecha by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actividades Pendientes") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("< Menu")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = filtroEstado,
                    onValueChange = { filtroEstado = it },
                    label = { Text("Estado") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = filtroFecha,
                    onValueChange = { filtroFecha = it },
                    label = { Text("Fecha") },
                    modifier = Modifier.weight(1f)
                )
            }

            when (val state = pendientesState) {
                is Result.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Result.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: ${state.message}")
                    }
                }
                is Result.Success -> {
                    val filtered = state.data.filter { act ->
                        (filtroEstado.isEmpty() || act.estado.name.contains(filtroEstado.uppercase())) &&
                        (filtroFecha.isEmpty() || act.fechaInicio.contains(filtroFecha))
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { actividad ->
                            ActividadPendienteCard(
                                actividad = actividad,
                                onClick = { onVerActividad(actividad.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActividadPendienteCard(
    actividad: Actividad,
    onClick: () -> Unit
) {
    val estadoColor = when (actividad.estado) {
        EstadoActividad.BORRADOR -> MaterialTheme.colorScheme.secondary
        EstadoActividad.PENDIENTE_VALIDAR -> MaterialTheme.colorScheme.primary
        EstadoActividad.VALIDADA -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Parcela ${actividad.parcelaId}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = actividad.fechaInicio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                actividad.problemaFitosanitario?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            AssistChip(
                onClick = {},
                label = { Text(actividad.estado.name.replace("_", " ")) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = estadoColor.copy(alpha = 0.2f)
                )
            )
        }
    }
}
