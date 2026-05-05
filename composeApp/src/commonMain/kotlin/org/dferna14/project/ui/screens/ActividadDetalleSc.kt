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
import org.dferna14.project.ui.viewmodel.ActividadDetalleVm
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadDetalleSc(
    actividadId: Int,
    onVolver: () -> Unit,
    onEditar: (Int) -> Unit,
    onVerSemillas: (Int) -> Unit = {},
    onVerFertilizacion: (Int) -> Unit = {},
    viewModel: ActividadDetalleVm = koinViewModel()
) {
    val actividadState by viewModel.actividadActual.collectAsState()
    val operacionExitosa by viewModel.operacionExitosa.collectAsState()
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarSnackbar by remember { mutableStateOf(false) }
    var snackbarMensaje by remember { mutableStateOf("") }

    LaunchedEffect(actividadId) {
        viewModel.cargarActividad(actividadId)
    }

    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            snackbarMensaje = "Operación realizada con éxito"
            mostrarSnackbar = true
            viewModel.resetOperacionExitosa()
        }
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
                    when (val estado = actividadState) {
                        is Result.Success -> {
                            val act = estado.data
                            // Solo editable en BORRADOR
                            if (act.estado.esEditable()) {
                                TextButton(onClick = { onEditar(actividadId) }) {
                                    Text("Editar")
                                }
                                // Botón enviar a validar (solo en BORRADOR)
                                Button(
                                    onClick = { viewModel.enviarActividad(actividadId) },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Enviar")
                                }
                            }
                            // Eliminar en BORRADOR y VALIDADA
                            if (act.estado.esEditable() || act.estado == EstadoActividad.VALIDADA) {
                                TextButton(onClick = { mostrarDialogoEliminar = true }) {
                                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            )
        },
        snackbarHost = {
            if (mostrarSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { mostrarSnackbar = false }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(snackbarMensaje)
                }
            }
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
                    onVerSemillas = onVerSemillas,
                    onVerFertilizacion = onVerFertilizacion,
                    modifier = Modifier.padding(padding),


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
    modifier: Modifier = Modifier,
    onVerSemillas: (Int) -> Unit = {},
    onVerFertilizacion: (Int) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TarjetaCampo(label = "Estado", valor = actividad.estado.name.replace("_", " "))
        TarjetaCampo(label = "Parcela", valor = "Parcela ${actividad.parcelaId}")
        TarjetaCampo(label = "Fecha de inicio", valor = actividad.fechaInicio)
        actividad.superficieTratada?.let {
            TarjetaCampo(label = "Superficie tratada", valor = "$it ha")
        }
        actividad.problemaFitosanitario?.let {
            TarjetaCampo(label = "Problema fitosanitario", valor = it)
        }
        actividad.observaciones?.let {
            TarjetaCampo(label = "Observaciones", valor = it)
        }

        when (actividad.estado) {
            EstadoActividad.BORRADOR -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Esta actividad está en modo borrador",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pulsa 'Enviar a validar' cuando esté completa para que el técnico pueda revisarla.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            EstadoActividad.PENDIENTE_VALIDAR -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pendiente de validación",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "El técnico de escritorio debe revisar y validar esta actividad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            EstadoActividad.VALIDADA -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Actividad validada",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Esta actividad está completa y cumple con los requisitos legales del cuaderno de campo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Botones para Semillas y Fertilización
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onVerSemillas(actividad.id) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Semillas Tratadas")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Lógica Fertilización: Validar puente Actividad -> Parcela -> Cultivo
        // Nota: Actualmente usamos parcelaId como referencia temporal.
        // Si no hay parcelaId válido, deshabilitamos el botón.
        val tieneParcelaAsociada = actividad.parcelaId > 0
        
        Button(
            onClick = { 
                // Pasamos el parcelaId como referencia (el backend tiene cultivoId nullable)
                onVerFertilizacion(actividad.parcelaId) 
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = tieneParcelaAsociada
        ) {
            Text("Fertilización Básica")
        }
        
        if (!tieneParcelaAsociada) {
            Text(
                text = "⚠️ Falta asociar una parcela a esta actividad",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
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
