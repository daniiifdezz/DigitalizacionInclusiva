package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Explotacion
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisParcelasSc(
    viewModel: ParcelaVm = koinViewModel()
) {
    val parcelasState by viewModel.parcelas.collectAsState()
    val explotacionesState by viewModel.explotaciones.collectAsState()
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var parcelaAEliminar by remember { mutableStateOf<Parcela?>(null) }

    // Cuando el usuario abre el diálogo refrescamos la lista de explotaciones
    // por si se ha creado una desde Desktop mientras la app móvil estaba abierta.
    LaunchedEffect(mostrarDialogoCrear) {
        if (mostrarDialogoCrear) viewModel.cargarExplotaciones()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Parcelas") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoCrear = true }
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { padding ->
        when (val state = parcelasState) {
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.cargarParcelas() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is Result.Success -> {
                if (state.data.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No hay parcelas")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { mostrarDialogoCrear = true }) {
                                Text("Crear primera parcela")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "${state.data.size} parcela(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(state.data, key = { it.id }) { parcela ->
                            ParcelaCard(
                                parcela = parcela,
                                onEliminar = { parcelaAEliminar = parcela }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogo crear parcela — comportamiento depende del estado de explotaciones
    if (mostrarDialogoCrear) {
        when (val expState = explotacionesState) {
            is Result.Loading -> CargandoExplotacionesDialog(onDismiss = { mostrarDialogoCrear = false })
            is Result.Error -> SinExplotacionesDialog(
                titulo = "Error",
                mensaje = expState.message,
                onDismiss = { mostrarDialogoCrear = false }
            )
            is Result.Success -> {
                if (expState.data.isEmpty()) {
                    SinExplotacionesDialog(
                        titulo = "No hay explotaciones",
                        mensaje = "Primero debes crear una explotación desde el escritorio antes de poder añadir parcelas.",
                        onDismiss = { mostrarDialogoCrear = false }
                    )
                } else {
                    CrearParcelaDialog(
                        explotaciones = expState.data,
                        onDismiss = { mostrarDialogoCrear = false },
                        onCreate = { alias, explotacionId ->
                            viewModel.crearParcela(
                                Parcela(
                                    id            = 0,
                                    explotacionId = explotacionId,
                                    alias         = alias?.takeIf { it.isNotBlank() }
                                )
                            )
                            mostrarDialogoCrear = false
                        }
                    )
                }
            }
        }
    }

    // Dialogo confirmar eliminación
    parcelaAEliminar?.let { parcela ->
        AlertDialog(
            onDismissRequest = { parcelaAEliminar = null },
            title = { Text("Eliminar parcela") },
            text = { Text("¿Estás seguro de que quieres eliminar la parcela ${parcela.id}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarParcela(parcela.id)
                        parcelaAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { parcelaAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ParcelaCard(
    parcela: Parcela,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = parcela.alias ?: "Parcela ${parcela.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (parcela.sistemaAsesoramiento != null || parcela.zonaNitratos != null) {
                    Text(
                        text = listOfNotNull(
                            parcela.sistemaAsesoramiento,
                            parcela.zonaNitratos?.let { if (it) "Zona nitratos" else null }
                        ).joinToString(" - "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = onEliminar) {
                Text("X", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrearParcelaDialog(
    explotaciones: List<Explotacion>,
    onDismiss: () -> Unit,
    onCreate: (alias: String?, explotacionId: Int) -> Unit
) {
    var alias by remember { mutableStateOf("") }
    // Si solo hay una explotación se preselecciona automáticamente sin pedir nada al
    // usuario. En cuanto haya login, esto se reemplazará por la explotación del JWT.
    var explotacionSeleccionada by remember(explotaciones) {
        mutableStateOf(explotaciones.firstOrNull())
    }
    var dropdownAbierto by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Parcela") },
        text = {
            Column {
                Text(
                    text = "Introduce un nombre para identificar la parcela",
                    style = MaterialTheme.typography.bodySmall
                )

                if (explotaciones.size > 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ExposedDropdownMenuBox(
                        expanded = dropdownAbierto,
                        onExpandedChange = { dropdownAbierto = it }
                    ) {
                        OutlinedTextField(
                            value = explotacionSeleccionada?.nombre ?: "Selecciona explotación",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Explotación *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownAbierto) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownAbierto,
                            onDismissRequest = { dropdownAbierto = false }
                        ) {
                            explotaciones.forEach { exp ->
                                DropdownMenuItem(
                                    text = { Text(exp.nombre) },
                                    onClick = {
                                        explotacionSeleccionada = exp
                                        dropdownAbierto = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Nombre/Alias") },
                    placeholder = { Text("Ej: Parcela Norte") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val expId = explotacionSeleccionada?.id ?: return@Button
                    onCreate(alias.ifBlank { null }, expId)
                },
                enabled = explotacionSeleccionada != null
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun CargandoExplotacionesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cargando…") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun SinExplotacionesDialog(
    titulo: String,
    mensaje: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Entendido") }
        }
    )
}
