package org.dferna14.project.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import org.dferna14.project.data.remote.DependenciasProductoDto
import org.dferna14.project.domain.model.Producto
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoAvisoInfo
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoDropdown
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.components.CampoTextoConOcr
import org.dferna14.project.ui.components.desktop.*
import org.dferna14.project.ui.theme.*
import org.dferna14.project.ui.viewmodel.AjustesVm
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

private val COLS_PRODUCTOS = listOf(
    DesktopTableColumn("Nombre",               weight = 2.5f),
    DesktopTableColumn("Tipo",                 weight = 1.0f),
    DesktopTableColumn("Materia activa / NPK", weight = 2.0f),
    DesktopTableColumn("Acciones",             weight = 1.0f),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosSc(
    viewModel: ProductoVm = koinViewModel(),
    onVolver: () -> Unit,
    // Desktop (técnico): borrado en cascada con diálogo detallado.
    // Móvil (agricultor, valor por defecto): borrado simple bloqueado con 409.
    isDesktop: Boolean = false,
    onVerInicio: (() -> Unit)? = null,
    onVerActividades: (() -> Unit)? = null,
    onVerParcelas: (() -> Unit)? = null,
    onVerAjustes: (() -> Unit)? = null,
    onVerConfiguracion: (() -> Unit)? = null,
    ajustesVm: AjustesVm = koinViewModel()
) {
    val productosState by viewModel.productos.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    var filtroActivo by remember { mutableStateOf(TIPO_TODOS) }
    // Solo en Desktop: dependencias del producto a eliminar para el diálogo de cascada.
    var dependenciasProducto by remember { mutableStateOf<DependenciasProductoDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }

    LaunchedEffect(productoAEliminar) {
        dependenciasProducto = null
        if (isDesktop) {
            productoAEliminar?.let { dependenciasProducto = viewModel.obtenerDependencias(it.id) }
        }
    }

    LaunchedEffect(mensajeError) {
        mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajeError()
        }
    }

    if (isDesktop) {
        ProductosDesktop(
            productosState     = productosState,
            nombreUsuario      = ajustesVm.nombreMostrado,
            rolUsuario         = ajustesVm.rolUsuario,
            snackbarHostState  = snackbarHostState,
            onCrear            = { mostrarDialogoCrear = true },
            onEliminar         = { productoAEliminar = it },
            onVerInicio        = onVerInicio ?: {},
            onVerActividades   = onVerActividades ?: {},
            onVerParcelas      = onVerParcelas ?: {},
            onVerAjustes       = onVerAjustes ?: {},
            onVerConfiguracion = onVerConfiguracion ?: {},
            onReintentar       = { viewModel.cargarProductos() }
        )
    } else {
        ProductosMovil(
            productosState    = productosState,
            filtroActivo      = filtroActivo,
            onFiltroChange    = { filtroActivo = it },
            snackbarHostState = snackbarHostState,
            onVolver          = onVolver,
            onCrear           = { mostrarDialogoCrear = true },
            onEliminar        = { productoAEliminar = it },
            onReintentar      = { viewModel.cargarProductos() }
        )
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

    // Diálogo confirmar eliminación.
    // Desktop: borrado en cascada con detalle de dependencias.
    // Móvil: borrado simple (el backend bloquea con 409 si hay datos asociados).
    productoAEliminar?.let { producto ->
        if (isDesktop) {
            ConfirmarBorradoCascadaProductoDialog(
                producto = producto,
                dependencias = dependenciasProducto,
                onConfirmar = {
                    viewModel.eliminarProductoEnCascada(producto.id)
                    productoAEliminar = null
                },
                onCancelar = { productoAEliminar = null }
            )
        } else {
            AlertDialog(
                onDismissRequest = { productoAEliminar = null },
                containerColor = SuperficieSepia,
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
}

//mobil
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductosMovil(
    productosState: Result<List<Producto>>,
    filtroActivo: String,
    onFiltroChange: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    onVolver: () -> Unit,
    onCrear: () -> Unit,
    onEliminar: (Producto) -> Unit,
    onReintentar: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CremaPrincipal,
        topBar = {
            TopAppBar(
                title = { Text("Productos", style = MaterialTheme.typography.titleLarge, color = TextoPrimario) },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver al menú principal",
                            tint = OlivaPrimario,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Menú principal", color = OlivaPrimario)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SuperficieSepia)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrear,
                containerColor = OlivaPrimario,
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
                        color = OlivaPrimario,
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
                        CampoPrimaryButton(
                            text = "Reintentar",
                            onClick = onReintentar,
                            modifier = Modifier.width(180.dp)
                        )
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
                            onCambiar = onFiltroChange
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
                                        onEliminar = { onEliminar(producto) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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
            val activo = filtroActivo == valor
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (activo) OlivaPrimario else SuperficieSepia)
                    .border(1.dp, if (activo) OlivaOscuro else BordeNormal, RoundedCornerShape(999.dp))
                    .clickable { onCambiar(valor) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (activo) CremaPrincipal else TextoSecundario
                )
            }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
            .background(SuperficieSepia)
            .border(0.5.dp, BordeNormal, RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(badgeColor)
        )
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CremaPrincipal)
                    .border(1.dp, BordeNormal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Science,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = badgeColor
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = producto.nombreComercial,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
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
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
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
                    style = MaterialTheme.typography.labelSmall,
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

//desktop
@Composable
private fun ProductosDesktop(
    productosState: Result<List<Producto>>,
    nombreUsuario: String,
    rolUsuario: String,
    snackbarHostState: SnackbarHostState,
    onCrear: () -> Unit,
    onEliminar: (Producto) -> Unit,
    onVerInicio: () -> Unit,
    onVerActividades: () -> Unit,
    onVerParcelas: () -> Unit,
    onVerAjustes: () -> Unit,
    onVerConfiguracion: () -> Unit,
    onReintentar: () -> Unit
) {
    DesktopWrapper(
        activeIndex   = 3,
        onNavigate    = { idx ->
            when (idx) {
                0 -> onVerInicio()
                1 -> onVerActividades()
                2 -> onVerParcelas()
                4 -> onVerAjustes()
                5 -> onVerConfiguracion()
            }
        },
        nombreUsuario = nombreUsuario,
        rolUsuario    = rolUsuario
    ) {
        DesktopTopBar(
            title   = "Productos",
            actions = listOf(
                DesktopTopBarAction(
                    label   = "Añadir producto",
                    icon    = Icons.Outlined.Add,
                    primary = true,
                    onClick = onCrear
                )
            )
        )
        when (val estado = productosState) {
            is Result.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OlivaPrimario)
                }
            }
            is Result.Error -> {
                Column(
                    modifier            = Modifier.fillMaxSize().padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text  = "No se pudo cargar el catálogo de productos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextoSecundario
                    )
                    Spacer(Modifier.height(16.dp))
                    CampoPrimaryButton(
                        text     = "Reintentar",
                        onClick  = onReintentar,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }
            is Result.Success -> {
                val productos = estado.data
                if (productos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector        = Icons.Outlined.Science,
                                contentDescription = null,
                                modifier           = Modifier.size(48.dp),
                                tint               = OlivaPrimario
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text  = "No hay productos en el catálogo",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextoPrimario
                            )
                        }
                    }
                } else {
                    DesktopTableHeader(columns = COLS_PRODUCTOS)
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        itemsIndexed(productos, key = { _, p -> p.id }) { index, producto ->
                            DesktopTableRow(
                                columns = COLS_PRODUCTOS,
                                last    = index == productos.lastIndex,
                                cells   = listOf(
                                    {
                                        Text(
                                            text  = producto.nombreComercial,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextoPrimario
                                        )
                                    },
                                    {
                                        val (color, label) = if (producto.tipo == TIPO_FERTILIZANTE)
                                            Pair(AzulPendiente, "Fertilizante")
                                        else
                                            Pair(AmbarProducto, "Fitosanitario")
                                        Text(
                                            text       = label,
                                            style      = MaterialTheme.typography.bodySmall,
                                            color      = color,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    {
                                        val materia = if (producto.tipo == TIPO_FERTILIZANTE)
                                            "NPK: ${producto.riquezaNpk ?: "—"}"
                                        else
                                            producto.materiaActiva ?: "—"
                                        Text(
                                            text  = materia,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextoSecundario
                                        )
                                    },
                                    {
                                        IconButton(onClick = { onEliminar(producto) }) {
                                            Icon(
                                                imageVector        = Icons.Outlined.Delete,
                                                contentDescription = "Eliminar producto",
                                                tint               = RojoEliminar,
                                                modifier           = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbarHostState)
    }
}

// ─── Dialogs ─────────────────────────────────────────────────────────────────

@Composable
private fun ConfirmarBorradoCascadaProductoDialog(
    producto: Producto,
    dependencias: DependenciasProductoDto?,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = SuperficieSepia,
        title = { Text("¿Eliminar producto y sus referencias?") },
        text = {
            Column {
                Text("Se eliminará el producto \"${producto.nombreComercial}\" del catálogo.")
                Spacer(Modifier.height(8.dp))

                if (dependencias == null) {
                    Text("Consultando datos asociados…", color = TextoSecundario)
                } else {
                    val items = buildList {
                        if (dependencias.actividadProductos > 0)
                            add("• Se quitará de ${dependencias.actividadProductos} aplicación(es) en actividades")
                        if (dependencias.semillas > 0)
                            add("• ${dependencias.semillas} semilla(s) tratada(s) quedarán sin producto")
                        if (dependencias.fertilizaciones > 0)
                            add("• ${dependencias.fertilizaciones} fertilización(es) quedarán sin producto")
                    }
                    if (items.isEmpty()) {
                        Text("No está siendo usado en ningún registro.", color = TextoSecundario)
                    } else {
                        items.forEach { Text(it, color = TextoSecundario) }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Esta acción no se puede deshacer.",
                    color = RojoEliminar,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Eliminar todo", color = RojoEliminar, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = TextoSecundario)
            }
        }
    )
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
        containerColor = SuperficieSepia,
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
                        CampoTextoConOcr(
                            value = numeroRegistro,
                            onValueChange = { numeroRegistro = it },
                            label = "Nº Registro (ej: ES-00221)",
                            placeholder = "ES-XXXXX",
                            modifier = Modifier.fillMaxWidth()
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
                Text("Añadir", color = OlivaPrimario, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextoSecundario)
            }
        }
    )
}
