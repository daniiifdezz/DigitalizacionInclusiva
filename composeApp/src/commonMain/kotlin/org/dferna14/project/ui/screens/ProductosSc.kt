package org.dferna14.project.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import org.dferna14.project.ui.components.CampoDropdown
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.theme.AmbarFondoProducto
import org.dferna14.project.ui.theme.AmbarProducto
import org.dferna14.project.ui.theme.AzulFondoPendiente
import org.dferna14.project.ui.theme.AzulPendiente
import org.dferna14.project.ui.theme.BlancoPuro
import org.dferna14.project.ui.theme.NaranjaClaro
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.viewmodel.ProductoVm
import org.koin.compose.viewmodel.koinViewModel

// Tipos de producto del catálogo unificado.
private const val TIPO_TODOS         = "TODOS"
private const val TIPO_FITOSANITARIO = "FITOSANITARIO"
private const val TIPO_FERTILIZANTE  = "FERTILIZANTE"

// Códigos válidos de tipo_fertilizante. Mantener sincronizados con backend.
private data class TipoFert(val codigo: String, val nombre: String) {
    val etiqueta: String get() = "$codigo — $nombre"
}

private val TIPOS_FERTILIZANTE = listOf(
    TipoFert("EB", "Estiércol bovino"),
    TipoFert("EO", "Estiércol ovino"),
    TipoFert("EP", "Estiércol porcino"),
    TipoFert("PP", "Purín porcino"),
    TipoFert("G",  "Gallinaza"),
    TipoFert("L",  "Líquido"),
    TipoFert("C",  "Cristalino"),
    TipoFert("O",  "Otros")
)

private fun nombreTipoFert(codigo: String?): String =
    TIPOS_FERTILIZANTE.find { it.codigo == codigo }?.nombre ?: "Sin tipo"

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
    var filtroActivo by remember { mutableStateOf(TIPO_TODOS) }

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
                title = { Text("Productos", style = MaterialTheme.typography.titleLarge) },
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
                    val productosMostrados = when (filtroActivo) {
                        TIPO_FITOSANITARIO -> productos.filter { it.tipo == TIPO_FITOSANITARIO }
                        TIPO_FERTILIZANTE  -> productos.filter { it.tipo == TIPO_FERTILIZANTE }
                        else               -> productos
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "${productosMostrados.size} producto${if (productosMostrados.size != 1) "s" else ""} " +
                                if (filtroActivo == TIPO_TODOS) "en catálogo" else "filtrados",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoTerciario,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        CampoAvisoInfo(
                            mensaje = "Si no encuentras un producto en la lista, pulsa + para añadirlo",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        FiltrosTipo(
                            filtroActivo = filtroActivo,
                            onCambiar = { filtroActivo = it }
                        )

                        Spacer(Modifier.height(4.dp))

                        if (productosMostrados.isEmpty()) {
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
                                    text = if (productos.isEmpty()) "No hay productos en el catálogo"
                                           else "No hay productos para este filtro",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextoTerciario
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Pulsa + para añadir un producto",
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
                                items(productosMostrados, key = { it.id }) { producto ->
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
            onCrear = { nuevo ->
                viewModel.crearProducto(nuevo)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosTipo(
    filtroActivo: String,
    onCambiar: (String) -> Unit
) {
    val opciones = listOf(
        TIPO_TODOS         to "Todos",
        TIPO_FITOSANITARIO to "Fitosanitarios",
        TIPO_FERTILIZANTE  to "Fertilizantes"
    )
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEach { (valor, label) ->
            FilterChip(
                selected = filtroActivo == valor,
                onClick = { onCambiar(valor) },
                label = { Text(label, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NaranjaClaro,
                    selectedLabelColor     = NaranjaPrimario
                )
            )
        }
    }
}

@Composable
private fun ProductoCard(
    producto: Producto,
    onEliminar: () -> Unit
) {
    val esFertilizante = producto.tipo == TIPO_FERTILIZANTE
    val (badgeFondo, badgeColor, badgeLabel) = if (esFertilizante) {
        Triple(AzulFondoPendiente, AzulPendiente, "Fertilizante")
    } else {
        Triple(AmbarFondoProducto, AmbarProducto, "Fitosanitario")
    }

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
                    .background(badgeFondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Science,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = badgeColor
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = producto.nombreComercial,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextoPrimario,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .background(badgeFondo, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            fontSize = 10.sp,
                            color = badgeColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                val subtitulo = if (esFertilizante) {
                    "NPK: ${producto.riquezaNpk ?: "—"} · ${nombreTipoFert(producto.tipoFertilizante)}"
                } else {
                    "${producto.materiaActiva ?: "Sin materia activa"} · ${producto.numeroRegistro ?: "Sin registro"}"
                }
                Text(
                    text = subtitulo,
                    fontSize = 11.sp,
                    color = TextoTerciario
                )
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
    onCrear: (Producto) -> Unit
) {
    var tipoNuevo by remember { mutableStateOf(TIPO_FITOSANITARIO) }
    var nombre by remember { mutableStateOf("") }
    var materiaActiva by remember { mutableStateOf("") }
    var numeroRegistro by remember { mutableStateOf("") }
    var riquezaNpk by remember { mutableStateOf("") }
    var tipoFertilizanteSel by remember { mutableStateOf<TipoFert?>(null) }

    val esFitosanitario = tipoNuevo == TIPO_FITOSANITARIO
    val esFertilizante  = tipoNuevo == TIPO_FERTILIZANTE

    // Reglas mínimas: para fitosanitario exigimos nombre y materia activa
    // (criterio existente); para fertilizante basta el nombre.
    val confirmHabilitado = nombre.isNotBlank() && when {
        esFitosanitario -> materiaActiva.isNotBlank()
        esFertilizante  -> true
        else            -> true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoDropdown(
                    label = "Tipo *",
                    selectedItem = tipoNuevo,
                    items = listOf(TIPO_FITOSANITARIO, TIPO_FERTILIZANTE),
                    itemLabel = { if (it == TIPO_FERTILIZANTE) "Fertilizante" else "Fitosanitario" },
                    onSelect = { tipoNuevo = it }
                )

                CampoTextField(
                    label = "Nombre comercial *",
                    value = nombre,
                    onValueChange = { nombre = it }
                )

                AnimatedVisibility(visible = esFitosanitario) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                }

                AnimatedVisibility(visible = esFertilizante) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoTextField(
                            label = "Riqueza NPK",
                            value = riquezaNpk,
                            onValueChange = { riquezaNpk = it },
                            placeholder = "Ej: 15-15-15"
                        )
                        CampoAvisoInfo(
                            mensaje = "Formato N-P-K en porcentaje. Ejemplo: 15-15-15 significa 15% Nitrógeno, 15% Fósforo, 15% Potasio"
                        )
                        CampoDropdown(
                            label = "Tipo de fertilizante",
                            selectedItem = tipoFertilizanteSel,
                            items = TIPOS_FERTILIZANTE,
                            itemLabel = { it.etiqueta },
                            onSelect = { tipoFertilizanteSel = it },
                            placeholder = "Selecciona tipo"
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = confirmHabilitado,
                onClick = {
                    onCrear(
                        Producto(
                            id               = 0,
                            nombreComercial  = nombre.trim(),
                            materiaActiva    = if (esFitosanitario) materiaActiva.trim().ifBlank { null } else null,
                            numeroRegistro   = if (esFitosanitario) numeroRegistro.trim().ifBlank { null } else null,
                            tipo             = tipoNuevo,
                            riquezaNpk       = if (esFertilizante) riquezaNpk.trim().ifBlank { null } else null,
                            tipoFertilizante = if (esFertilizante) tipoFertilizanteSel?.codigo else null
                        )
                    )
                }
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
