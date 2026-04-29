package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ActividadViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisParcelasSc(
    viewModel: ActividadViewModel = koinViewModel()
) {
    val parcelasState by viewModel.parcelas.collectAsState()
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var parcelaAEliminar by remember { mutableStateOf<Parcela?>(null) }

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
                        items(state.data) { parcela ->
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

    // Dialogo crear parcela
    if (mostrarDialogoCrear) {
        CrearParcelaDialog(
            onDismiss = { mostrarDialogoCrear = false },
            onCreate = { alias, orden ->
                // El alias del usuario tiene prioridad. Si está vacío, enviar null
                viewModel.crearParcela(
                    Parcela(
                        id = 0,
                        orden = orden,
                        alias = alias?.takeIf { it.isNotBlank() }
                    )
                )
                mostrarDialogoCrear = false
            }
        )
    }

    // Dialogo confirmar eliminación
    parcelaAEliminar?.let { parcela ->
        AlertDialog(
            onDismissRequest = { parcelaAEliminar = null },
            title = { Text("Eliminar parcela") },
            text = { Text("¿Estás seguro de que quieres eliminar la parcela ${parcela.orden ?: parcela.id}?") },
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
                    text = parcela.alias ?: "Parcela ${parcela.orden ?: parcela.id}",
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

@Composable
private fun CrearParcelaDialog(
    onDismiss: () -> Unit,
    onCreate: (String?, Int?) -> Unit
) {
    var alias by remember { mutableStateOf("") }
    var orden by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Parcela") },
        text = {
            Column {
                Text(
                    text = "Introduce un nombre para identificar la parcela",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Nombre/Alias") },
                    placeholder = { Text("Ej: Parcela Norte") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = orden,
                    onValueChange = { orden = it },
                    label = { Text("Orden (opcional)") },
                    placeholder = { Text("Ej: 1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ordenInt = orden.toIntOrNull()
                    onCreate(alias.ifBlank { null }, ordenInt)
                }
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


