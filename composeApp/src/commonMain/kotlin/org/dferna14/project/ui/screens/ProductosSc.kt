package org.dferna14.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.domain.model.Producto
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoAvisoInfo
import org.dferna14.project.ui.components.CampoCard
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.theme.AmbarFondoProducto
import org.dferna14.project.ui.theme.AmbarProducto
import org.dferna14.project.ui.theme.BlancoPuro
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.viewmodel.ProductoVm
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosSc(
    viewModel: ProductoVm = koinViewModel(),
    onVolver: () -> Unit
) {
    val productosState by viewModel.productos.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    LaunchedEffect(mensajeError) {
        mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajeError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Productos", style = MaterialTheme.typography.titleLarge)},
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver al menú principal",
                            tint = NaranjaPrimario,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Menú principal", color = NaranjaPrimario)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoCrear = true },
                containerColor = NaranjaPrimario,
                contentColor = BlancoPuro
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Nuevo producto")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = productosState) {
                is Result.Loading -> {
                    CircularProgressIndicator(
                        color = NaranjaPrimario,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Result.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No se pudo cargar el catálogo de productos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoSecundario
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.cargarProductos() },
                            colors = ButtonDefaults.buttonColors(containerColor = NaranjaPrimario)
                        ) { Text("Reintentar") }
                    }
                }
                is Result.Success -> {
                    val productos = state.data
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "${productos.size} producto${if (productos.size != 1) "s" else ""} en catálogo",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoTerciario,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        CampoAvisoInfo(
                            mensaje = "Si no encuentras un producto en la lista, pulsa + para añadirlo",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Spacer(Modifier.height(8.dp))

                        if (productos.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Science,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = TextoTerciario
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "No hay productos en el catálogo",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextoTerciario
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Pulsa + para añadir el primer producto",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoTerciario
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(productos, key = { it.id }) { producto ->
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
        }
    }

    // Diálogo crear producto
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

    // Diálogo confirmar eliminación
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("¿Eliminar producto?") },
            text = { Text("Esta acción no se puede deshacer. ¿Seguro que quieres eliminar \"${producto.nombreComercial}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarProducto(producto.id)
                    productoAEliminar = null
                }) {
                    Text("Eliminar", color = RojoEliminar, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar", color = TextoSecundario)
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
    CampoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AmbarFondoProducto),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Science,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AmbarProducto
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombreComercial,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextoPrimario
                )
                val metadatos = listOfNotNull(
                    producto.materiaActiva,
                    producto.numeroRegistro
                ).joinToString(" · ")
                if (metadatos.isNotBlank()) {
                    Text(
                        text = metadatos,
                        fontSize = 12.sp,
                        color = TextoTerciario
                    )
                }
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Eliminar producto",
                    tint = TextoTerciario
                )
            }
        }
    }
}

@Composable
private fun CrearProductoDialog(
    onDismiss: () -> Unit,
    onCrear: (nombre: String, materiaActiva: String, numeroRegistro: String?) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var materiaActiva by remember { mutableStateOf("") }
    var numeroRegistro by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoTextField(
                    label = "Nombre comercial *",
                    value = nombre,
                    onValueChange = { nombre = it }
                )
                CampoTextField(
                    label = "Materia activa *",
                    value = materiaActiva,
                    onValueChange = { materiaActiva = it }
                )
                CampoTextField(
                    label = "Número de registro",
                    value = numeroRegistro,
                    onValueChange = { numeroRegistro = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCrear(nombre.trim(), materiaActiva.trim(), numeroRegistro.takeIf { it.isNotBlank() })
                },
                enabled = nombre.isNotBlank() && materiaActiva.isNotBlank()
            ) {
                Text("Añadir", color = NaranjaPrimario, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextoSecundario)
            }
        }
    )
}
