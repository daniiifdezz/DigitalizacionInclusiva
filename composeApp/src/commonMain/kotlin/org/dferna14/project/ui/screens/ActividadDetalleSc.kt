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
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ActividadViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadDetalleSc(
    actividadId: Int,
    onVolver: () -> Unit,
    onEditar: (Int) -> Unit,
    viewModel: ActividadViewModel = koinViewModel()
) {
    val actividadState by viewModel.actividadActual.collectAsState()
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(actividadId) {
        viewModel.cargarActividad(actividadId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Actividad") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("< Volver")
                    }
                },
                actions = {
                    when (actividadState) {
                        is Result.Success -> {
                            TextButton(onClick = { onEditar(actividadId) }) {
                                Text("Editar")
                            }
                            TextButton(onClick = { mostrarDialogoEliminar = true }) {
                                Text("Eliminar", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        else -> {}
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
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error al cargar la actividad",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.cargarActividad(actividadId) }) {
                        Text("Reintentar")
                    }
                }
            }
            is Result.Success -> {
                ActividadDetalleContenido(
                    actividad = estado.data,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar Actividad") },
            text = { Text("¿Estás seguro de que quieres eliminar esta actividad? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarActividad(actividadId)
                        mostrarDialogoEliminar = false
                        onVolver()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ActividadDetalleContenido(
    actividad: Actividad,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TarjetaCampo(label = "Parcela", valor = "Parcela ${actividad.parcelaId}")
        TarjetaCampo(label = "Fecha de inicio", valor = actividad.fechaInicio)
        actividad.fechaFin?.let {
            TarjetaCampo(label = "Fecha fin", valor = it)
        }
        actividad.superficieTratada?.let {
            TarjetaCampo(label = "Superficie tratada", valor = "$it ha")
        }
        actividad.problemaFitosanitario?.let {
            TarjetaCampo(label = "Problema fitosanitario", valor = it)
        }
        actividad.eficacia?.let {
            TarjetaCampo(label = "Eficacia", valor = it)
        }
        actividad.observaciones?.let {
            TarjetaCampo(label = "Observaciones", valor = it)
        }
    }
}

@Composable
private fun TarjetaCampo(
    label: String,
    valor: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
