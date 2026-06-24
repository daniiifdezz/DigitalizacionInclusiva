package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.ActividadProducto
import org.dferna14.project.domain.model.Cultivo
import org.dferna14.project.domain.model.DatosAgronomicos
import org.dferna14.project.domain.model.EquipoAplicacion
import org.dferna14.project.domain.model.EstadoActividad
import org.dferna14.project.domain.model.ParcelaCompleta
import org.dferna14.project.domain.model.Producto
import org.dferna14.project.domain.model.ReferenciaSigpac
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.Usuario
import org.dferna14.project.ui.components.AplicadorDropdown
import org.dferna14.project.ui.components.CampoAvisoInfo
import org.dferna14.project.ui.components.EquipoDropdown
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.viewmodel.ActividadDetalleVm
import org.dferna14.project.ui.viewmodel.EquipoVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.dferna14.project.ui.viewmodel.ProductoVm
import org.dferna14.project.ui.viewmodel.UsuarioVm
import org.koin.compose.viewmodel.koinViewModel

val VALORES_EFICACIA = listOf("ALTA", "MEDIA", "BAJA", "NULA")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidarActividadSc(
    actividadId: Int,
    onVolver: () -> Unit,
    onIrAEditarParcela: (Int) -> Unit,
    viewModel: ActividadDetalleVm = koinViewModel(),
    equipoVm: EquipoVm = koinViewModel(),
    usuarioVm: UsuarioVm = koinViewModel(),
    productoVm: ProductoVm = koinViewModel(),
    parcelaVm: ParcelaVm = koinViewModel()
) {
    val actividadState by viewModel.actividadActual.collectAsState()
    val equiposState by equipoVm.equipos.collectAsState()
    val usuariosState by usuarioVm.usuarios.collectAsState()
    val productosCatalogoState by productoVm.productos.collectAsState()
    val productosActividadState by viewModel.productosActividad.collectAsState()
    val parcelaCompletaState by parcelaVm.parcelaCompleta.collectAsState()
    val cultivosState by parcelaVm.cultivos.collectAsState()
    val operacionExitosa by viewModel.operacionExitosa.collectAsState()
    var datosCargados by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Datos", "Productos", "Parcela", "Validar")

    // Campos para validar
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var eficacia by remember { mutableStateOf("") }
    var aplicadorSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var equipoSeleccionado by remember { mutableStateOf<EquipoAplicacion?>(null) }
    var observaciones by remember { mutableStateOf("") }
    var desplegableEquipo by remember { mutableStateOf(false) }
    var desplegableAplicador by remember { mutableStateOf(false) }

    var errorFechaInicio by remember { mutableStateOf<String?>(null) }
    var errorFecha by remember { mutableStateOf<String?>(null) }
    var errorEficacia by remember { mutableStateOf<String?>(null) }

    fun validarFormatoFecha(fecha: String): Boolean {
        if (fecha.isBlank()) return false
        val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        return regex.matches(fecha)
    }

    fun esFechaPosterior(fechaFin: String, fechaInicio: String): Boolean {
        if (fechaFin.isBlank() || fechaInicio.isBlank()) return true
        return fechaFin >= fechaInicio
    }

    fun validarEficaciaValue(eficacia: String): Boolean {
        if (eficacia.isBlank()) return false
        return VALORES_EFICACIA.contains(eficacia.uppercase())
    }

    LaunchedEffect(actividadId) {
        // Reset defensivo del flag compartido al entrar (ver ActividadDetalleVm).
        viewModel.resetOperacionExitosa()
        viewModel.cargarActividad(actividadId)
        viewModel.cargarProductosActividad(actividadId)
    }

    // Cuando la actividad cargue (Result.Success), pedimos la parcela completa con su id real
    LaunchedEffect(actividadState) {
        val act = (actividadState as? Result.Success)?.data ?: return@LaunchedEffect
        if (act.parcelaId > 0) {
            parcelaVm.cargarParcelaCompleta(act.parcelaId)
        }
    }

    LaunchedEffect(actividadState) {
        if (!datosCargados && actividadState is Result.Success) {
            val act = (actividadState as Result.Success).data
            fechaInicio = act.fechaInicio
            fechaFin = act.fechaFin ?: ""
            eficacia = act.eficacia ?: ""
            observaciones = act.observaciones ?: ""
            datosCargados = true
        }
    }

    LaunchedEffect(actividadState, equiposState) {
        val act = (actividadState as? Result.Success)?.data ?: return@LaunchedEffect
        val equipos = (equiposState as? Result.Success)?.data ?: return@LaunchedEffect
        if (equipoSeleccionado == null && act.equipoId != null) {
            equipoSeleccionado = equipos.find { it.id == act.equipoId }
        }
    }

    LaunchedEffect(actividadState, usuariosState) {
        val act = (actividadState as? Result.Success)?.data ?: return@LaunchedEffect
        val usuarios = (usuariosState as? Result.Success)?.data ?: return@LaunchedEffect
        if (aplicadorSeleccionado == null && act.aplicadorId != null) {
            aplicadorSeleccionado = usuarios.find { it.id == act.aplicadorId }
        }
    }

    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            viewModel.resetOperacionExitosa()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Validar Actividad") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("< Volver")
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
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Error al cargar")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.cargarActividad(actividadId) }) {
                        Text("Reintentar")
                    }
                }
            }
            is Result.Success -> {
                val act = estado.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Tabs
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    // Contenido según pestaña
                    when (selectedTab) {
                        0 -> PestanaDatos(act = act)
                        1 -> PestanaProductos(
                            actividadId = actividadId,
                            productosActividadState = productosActividadState,
                            productosCatalogoState = productosCatalogoState,
                            onAñadir = { productoId, dosis ->
                                viewModel.añadirProducto(actividadId, productoId, dosis)
                            },
                            onEliminar = { actividadProductoId ->
                                viewModel.eliminarProducto(actividadProductoId, actividadId)
                            }
                        )
                        2 -> PestanaParcela(
                            parcelaCompletaState = parcelaCompletaState,
                            cultivosState = cultivosState,
                            onIrAEditarParcela = { onIrAEditarParcela(act.parcelaId) }
                        )
                        3 -> PestanaValidar(
                            act = act,
                            fechaInicio = fechaInicio,
                            fechaFin = fechaFin,
                            eficacia = eficacia,
                            aplicadorSeleccionado = aplicadorSeleccionado,
                            equipoSeleccionado = equipoSeleccionado,
                            equiposState = equiposState,
                            usuariosState = usuariosState,
                            desplegableEquipo = desplegableEquipo,
                            desplegableAplicador = desplegableAplicador,
                            observaciones = observaciones,
                            errorFechaInicio = errorFechaInicio,
                            errorFecha = errorFecha,
                            errorEficacia = errorEficacia,
                            onFechaInicioChange = { nuevo ->
                                fechaInicio = nuevo
                                errorFechaInicio = if (nuevo.isNotBlank() && !validarFormatoFecha(nuevo))
                                    "Formato incorrecto. Usa AAAA-MM-DD (ej: 2026-05-29)"
                                else null
                                if (fechaFin.isNotBlank() && validarFormatoFecha(nuevo)) {
                                    errorFecha = if (!esFechaPosterior(fechaFin, nuevo))
                                        "La fecha fin no puede ser anterior a la fecha de inicio"
                                    else null
                                }
                            },
                            onFechaFinChange = { nuevo ->
                                fechaFin = nuevo
                                errorFecha = when {
                                    nuevo.isBlank() ->
                                        null
                                    !validarFormatoFecha(nuevo) ->
                                        "Formato incorrecto. Usa AAAA-MM-DD (ej: 2026-05-29)"
                                    !esFechaPosterior(nuevo, fechaInicio) ->
                                        "La fecha fin no puede ser anterior a la fecha de inicio"
                                    else -> null
                                }
                            },
                            onEficaciaChange = { eficacia = it },
                            onAplicadorChange = { aplicadorSeleccionado = it },
                            onEquipoChange = { equipoSeleccionado = it },
                            onDesplegableEquipoChange = { desplegableEquipo = it },
                            onDesplegableAplicadorChange = { desplegableAplicador = it },
                            onObservacionesChange = { observaciones = it },
                            onValidar = {
                                val actActualizada = act.copy(
                                    fechaInicio = fechaInicio,
                                    fechaFin = fechaFin,
                                    eficacia = eficacia.uppercase(),
                                    equipoId = equipoSeleccionado?.id,
                                    aplicadorId = aplicadorSeleccionado?.id,
                                    observaciones = observaciones.ifBlank { null },
                                    estado = EstadoActividad.VALIDADA
                                )
                                // La vuelta la dispara LaunchedEffect(operacionExitosa)
                                // cuando el backend confirma. No resetear ni navegar de
                                // forma síncrona aquí (desmontaría la pantalla antes de
                                // que termine la operación y dejaría el flag atascado).
                                viewModel.actualizarActividad(actActualizada)
                            },
                            onDevolver = {
                                viewModel.devolverActividad(actividadId)
                            },
                            validarFormatoFecha = { validarFormatoFecha(it) },
                            esFechaPosterior = { f, i -> esFechaPosterior(f, i) },
                            validarEficaciaValue = { validarEficaciaValue(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PestanaDatos(act: Actividad) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Datos de la actividad",
            style = MaterialTheme.typography.titleMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FieldView("Parcela", act.parcelaAlias ?: "Parcela ${act.parcelaId}")
                FieldView("Fecha inicio", act.fechaInicio)
                act.fechaFin?.let { FieldView("Fecha fin", it) }
                act.superficieTratada?.let { FieldView("Superficie tratadas", "$it ha") }
            }
        }

        Text(
            text = "Problema fitosanitario",
            style = MaterialTheme.typography.titleSmall
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = act.problemaFitosanitario ?: "No especificadis",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(
            text = "Observaciones del agricultor",
            style = MaterialTheme.typography.titleSmall
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = act.observaciones ?: "Sin observaciones",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PestanaProductos(
    actividadId: Int,
    productosActividadState: Result<List<ActividadProducto>>,
    productosCatalogoState: Result<List<Producto>>,
    onAñadir: (productoId: Int, dosis: Double) -> Unit,
    onEliminar: (actividadProductoId: Int) -> Unit
) {
    val catalogo = (productosCatalogoState as? Result.Success)?.data.orEmpty()
    var productoAEliminar by remember { mutableStateOf<ActividadProducto?>(null) }

    // Estado del form de añadir
    var productoSeleccionado by remember(catalogo) { mutableStateOf<Producto?>(null) }
    var dosisTexto by remember { mutableStateOf("") }
    var dropdownAbierto by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Productos aplicados",
            style = MaterialTheme.typography.titleMedium
        )

        // Lista de productos ya asociados
        when (val state = productosActividadState) {
            is Result.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Result.Error -> {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            is Result.Success -> {
                if (state.data.isEmpty()) {
                    Text(
                        text = "No hay productos registrados para esta actividad",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data, key = { it.id }) { ap ->
                            val producto = catalogo.find { it.id == ap.productoId }
                            ProductoAplicadoCard(
                                actividadProducto = ap,
                                producto = producto,
                                onEliminar = { productoAEliminar = ap }
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        // Form de añadir producto
        Text(
            text = "Añadir producto",
            style = MaterialTheme.typography.titleSmall
        )

        ExposedDropdownMenuBox(
            expanded = dropdownAbierto,
            onExpandedChange = { dropdownAbierto = it }
        ) {
            OutlinedTextField(
                value = productoSeleccionado?.nombreComercial ?: "Selecciona producto",
                onValueChange = {},
                readOnly = true,
                label = { Text("Producto") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownAbierto) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = dropdownAbierto,
                onDismissRequest = { dropdownAbierto = false }
            ) {
                if (catalogo.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay productos en el catálogo") },
                        onClick = {}
                    )
                } else {
                    catalogo.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.nombreComercial) },
                            onClick = {
                                productoSeleccionado = p
                                dropdownAbierto = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = dosisTexto,
            onValueChange = { dosisTexto = it },
            label = { Text("Dosis (kg/ha o l/ha)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val producto = productoSeleccionado ?: return@Button
                val dosis = dosisTexto.replace(",", ".").toDoubleOrNull() ?: return@Button
                onAñadir(producto.id, dosis)
                // Reset del form para permitir añadir varios sin recargar la pantalla
                productoSeleccionado = null
                dosisTexto = ""
            },
            enabled = productoSeleccionado != null && dosisTexto.replace(",", ".").toDoubleOrNull() != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Añadir producto")
        }
    }

    // Dialogo confirmar eliminación
    productoAEliminar?.let { ap ->
        val producto = catalogo.find { it.id == ap.productoId }
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar producto aplicado") },
            text = {
                Text("¿Eliminar ${producto?.nombreComercial ?: "este producto"} (dosis ${ap.dosis}) de la actividad?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminar(ap.id)
                        productoAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProductoAplicadoCard(
    actividadProducto: ActividadProducto,
    producto: Producto?,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto?.nombreComercial ?: "Producto #${actividadProducto.productoId}",
                    style = MaterialTheme.typography.titleMedium
                )
                producto?.materiaActiva?.let {
                    Text(
                        text = "Materia activa: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Dosis: ${actividadProducto.dosis} kg/ha",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onEliminar) {
                Text("X", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PestanaParcela(
    parcelaCompletaState: Result<ParcelaCompleta?>,
    cultivosState: Result<List<Cultivo>>,
    onIrAEditarParcela: () -> Unit
) {
    when (val state = parcelaCompletaState) {
        is Result.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is Result.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
        is Result.Success -> {
            val completa = state.data
            if (completa == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("La parcela asociada no existe")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SeccionSigpac(
                        sigpac = completa.referenciaSigpac,
                        onIrAEditarParcela = onIrAEditarParcela
                    )
                    SeccionAgronomicos(
                        agronomicos = completa.datosAgronomicos,
                        cultivosState = cultivosState,
                        onIrAEditarParcela = onIrAEditarParcela
                    )
                }
            }
        }
    }
}

@Composable
private fun SeccionSigpac(
    sigpac: ReferenciaSigpac?,
    onIrAEditarParcela: () -> Unit
) {
    Text(
        text = "Referencia SIGPAC",
        style = MaterialTheme.typography.titleMedium
    )
    if (sigpac == null) {
        TarjetaAviso(
            texto = "⚠️ Esta parcela no tiene datos SIGPAC registrados.",
            subtexto = "Es necesario completarlos para el cuaderno oficial.",
            onIrAEditarParcela = onIrAEditarParcela
        )
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FieldView("Provincia", sigpac.provincia ?: "—")
                FieldView("Término municipal", sigpac.terminoMunicipal ?: "—")
                FieldView("Polígono", sigpac.numeroPoligono ?: "—")
                FieldView("Parcela", sigpac.numeroParcela ?: "—")
                FieldView("Recinto", sigpac.numeroRecinto ?: "—")
                FieldView("Superficie (ha)", sigpac.superficieHa?.toString() ?: "—")
                FieldView("Uso SIGPAC", sigpac.usoSigpac ?: "—")
                FieldView("Zona", sigpac.zona ?: "—")
                FieldView("Código agregado", sigpac.codigoAgregado ?: "—")
            }
        }
    }
}

@Composable
private fun SeccionAgronomicos(
    agronomicos: DatosAgronomicos?,
    cultivosState: Result<List<Cultivo>>,
    onIrAEditarParcela: () -> Unit
) {
    Text(
        text = "Datos agronómicos",
        style = MaterialTheme.typography.titleMedium
    )
    if (agronomicos == null) {
        TarjetaAviso(
            texto = "⚠️ Esta parcela no tiene datos agronómicos registrados.",
            subtexto = "Es necesario completarlos para el cuaderno oficial.",
            onIrAEditarParcela = onIrAEditarParcela
        )
    } else {
        val cultivoNombre = (cultivosState as? Result.Success)?.data
            ?.find { it.id == agronomicos.cultivoId }
            ?.let { listOfNotNull(it.especie, it.variedad).joinToString(" - ").ifBlank { "Cultivo ${it.id}" } }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FieldView("Especie / Variedad", agronomicos.especieVariedad ?: "—")
                FieldView("Cultivo", cultivoNombre ?: agronomicos.cultivoId?.let { "Cultivo #$it" } ?: "—")
                FieldView("Secano / Regadío", agronomicos.secanoRegadio ?: "—")
                FieldView("Eco-régimen / Práctica", agronomicos.ecoregimenPractica ?: "—")
                FieldView("Aire libre / Protegido", agronomicos.aireLibreProtegido ?: "—")
                FieldView("Fecha inicio", agronomicos.fechaInicio ?: "—")
                FieldView("Fecha fin", agronomicos.fechaFin ?: "—")
            }
        }
    }
}

@Composable
private fun TarjetaAviso(
    texto: String,
    subtexto: String,
    onIrAEditarParcela: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF4E5),  // amarillo/naranja claro de aviso
            contentColor   = Color(0xFF7A4F01)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtexto,
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = onIrAEditarParcela,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir a editar parcela")
            }
        }
    }
}


@Composable
private fun PestanaValidar(
    act: Actividad,
    fechaInicio: String,
    fechaFin: String,
    eficacia: String,
    aplicadorSeleccionado: Usuario?,
    equipoSeleccionado: EquipoAplicacion?,
    equiposState: Result<List<EquipoAplicacion>>,
    usuariosState: Result<List<Usuario>>,
    desplegableEquipo: Boolean,
    desplegableAplicador: Boolean,
    observaciones: String,
    errorFechaInicio: String?,
    errorFecha: String?,
    errorEficacia: String?,
    onFechaInicioChange: (String) -> Unit,
    onFechaFinChange: (String) -> Unit,
    onEficaciaChange: (String) -> Unit,
    onAplicadorChange: (Usuario?) -> Unit,
    onEquipoChange: (EquipoAplicacion?) -> Unit,
    onDesplegableEquipoChange: (Boolean) -> Unit,
    onDesplegableAplicadorChange: (Boolean) -> Unit,
    onObservacionesChange: (String) -> Unit,
    onValidar: () -> Unit,
    onDevolver: () -> Unit,
    validarFormatoFecha: (String) -> Boolean,
    esFechaPosterior: (String, String) -> Boolean,
    validarEficaciaValue: (String) -> Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Completar datos de validación:",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = fechaInicio,
            onValueChange = { newValue -> onFechaInicioChange(newValue) },
            label = { Text("Fecha inicio *") },
            placeholder = { Text("AAAA-MM-DD") },
            isError = errorFechaInicio != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (errorFechaInicio != null) {
            Text(
                text = errorFechaInicio,
                color = RojoEliminar,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        OutlinedTextField(
            value = fechaFin,
            onValueChange = { newValue -> onFechaFinChange(newValue) },
            label = { Text("Fecha fin *") },
            placeholder = { Text("AAAA-MM-DD") },
            isError = errorFecha != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (errorFecha != null) {
            Text(
                text = errorFecha,
                color = RojoEliminar,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        EficaciaDropdown(
            eficaciaActual = eficacia,
            onSeleccionar = { onEficaciaChange(it) }
        )

        AplicadorDropdown(
            aplicadorSeleccionado = aplicadorSeleccionado,
            usuariosState = usuariosState,
            expandido = desplegableAplicador,
            onExpandidoChange = onDesplegableAplicadorChange,
            onSeleccionar = {
                onAplicadorChange(it)
                onDesplegableAplicadorChange(false)
            }
        )

        EquipoDropdown(
            equipoSeleccionado = equipoSeleccionado,
            equiposState = equiposState,
            expandido = desplegableEquipo,
            onExpandidoChange = onDesplegableEquipoChange,
            onSeleccionar = {
                onEquipoChange(it)
                onDesplegableEquipoChange(false)
            },
            label = "Equipo usado"
        )

        OutlinedTextField(
            value = observaciones,
            onValueChange = onObservacionesChange,
            label = { Text("Observaciones técnicas") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        CampoAvisoInfo(
            mensaje = "Añade cualquier observación técnica relevante para el cuaderno oficial: condiciones meteorológicas, incidencias, justificación del tratamiento..."
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDevolver,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Devolver")
            }

            // El botón exige fecha inicio y fin válidas (formato + orden cronológico).
            // La eficacia es opcional desde Desktop (el técnico puede dejarla en
            // blanco si todavía no tiene criterio).
            val esValido = fechaInicio.isNotBlank() &&
                    errorFechaInicio == null &&
                    validarFormatoFecha(fechaInicio) &&
                    fechaFin.isNotBlank() &&
                    errorFecha == null &&
                    validarFormatoFecha(fechaFin) &&
                    esFechaPosterior(fechaFin, fechaInicio)

            Button(
                onClick = onValidar,
                enabled = esValido,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Validar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FieldView(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private val OPCIONES_EFICACIA = listOf(
    "ALTA"  to "Alta — tratamiento muy efectivo",
    "MEDIA" to "Media — tratamiento parcialmente efectivo",
    "BAJA"  to "Baja — tratamiento poco efectivo",
    "NULA"  to "Nula — tratamiento sin efecto"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EficaciaDropdown(
    eficaciaActual: String,
    onSeleccionar: (String) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val etiqueta = OPCIONES_EFICACIA.find { it.first == eficaciaActual.uppercase() }?.second

    ExposedDropdownMenuBox(
        expanded = abierto,
        onExpandedChange = { abierto = it }
    ) {
        OutlinedTextField(
            value = etiqueta ?: "Selecciona eficacia del tratamiento",
            onValueChange = {},
            readOnly = true,
            label = { Text("Eficacia del tratamiento") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NaranjaPrimario
            )
        )
        ExposedDropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false }
        ) {
            OPCIONES_EFICACIA.forEach { (valor, etiqueta) ->
                DropdownMenuItem(
                    text = { Text(etiqueta, fontSize = 13.sp) },
                    onClick = {
                        onSeleccionar(valor)
                        abierto = false
                    }
                )
            }
        }
    }
}