package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Producto
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ActividadViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosSc(
    viewModel: ActividadViewModel = koinViewModel()
) {
    val productosState by viewModel.productos.collectAsState()
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") }
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
        when (val state = productosState) {
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
                        Button(onClick = { viewModel.cargarProductos() }) {
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
                            Text("No hay productos")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { mostrarDialogoCrear = true }) {
                                Text("Crear primer producto")
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
                                text = "${state.data.size} producto(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(state.data) { producto ->
                            ProductoCard(
                                producto = producto,
                                onEliminar = { productoAEliminar = producto }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogo crear producto
    if (mostrarDialogoCrear) {
        CrearProductoDialog(
            onDismiss = { mostrarDialogoCrear = false },
            onCrear = { nombre, materiaActiva, numeroRegistro ->
                viewModel.crearProducto(
                    Producto(
                        id = 0,
                        nombreComercial = nombre,
                        materiaActiva = materiaActiva,
                        numeroRegistro = numeroRegistro
                    )
                )
                mostrarDialogoCrear = false
            }
        )
    }

    // Dialogo confirmar eliminación
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Estás seguro de que quieres eliminar ${producto.nombreComercial ?: "este producto"}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarProducto(producto.id)
                        productoAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProductoCard(
    producto: Producto,
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
                    text = producto.nombreComercial ?: "Sin nombre",
                    style = MaterialTheme.typography.titleMedium
                )
                producto.materiaActiva?.let {
                    Text(
                        text = "Materia activa: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                producto.numeroRegistro?.let {
                    Text(
                        text = "Registro: $it",
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
private fun CrearProductoDialog(
    onDismiss: () -> Unit,
    onCrear: (String?, String?, String?) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var materiaActiva by remember { mutableStateOf("") }
    var numeroRegistro by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Producto") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre comercial *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = materiaActiva,
                    onValueChange = { materiaActiva = it },
                    label = { Text("Materia activa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = numeroRegistro,
                    onValueChange = { numeroRegistro = it },
                    label = { Text("Número de registro") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nombreFinal = nombre.takeIf { it.isNotBlank() }
                    val materiaFinal = materiaActiva.takeIf { it.isNotBlank() }
                    val registroFinal = numeroRegistro.takeIf { it.isNotBlank() }
                    onCrear(nombreFinal, materiaFinal, registroFinal)
                },
                enabled = nombre.isNotBlank()
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