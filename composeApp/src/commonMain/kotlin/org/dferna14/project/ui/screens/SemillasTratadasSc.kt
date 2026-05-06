package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlinx.datetime.todayIn
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.SemillaTratada
import org.dferna14.project.ui.viewmodel.ActividadDetalleVm
import org.dferna14.project.ui.viewmodel.ProductoVm
import org.dferna14.project.ui.viewmodel.SemillaVm
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemillasTratadasSc(
    actividadId: Int,
    onVolver: () -> Unit,
    viewModel: SemillaVm = koinViewModel(),
    actividadDetalleVm: ActividadDetalleVm = koinViewModel(),
    productoVm: ProductoVm = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val semillaState by viewModel.semilla.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }
    val productosState by productoVm.productos.collectAsState()
    val actividadState by actividadDetalleVm.actividadActual.collectAsState()

    // Feedback visual
    var mostrarSnackbar by remember { mutableStateOf(false) }
    var snackbarMensaje by remember { mutableStateOf("") }

    // 1. Cargar la actividad para obtener el parcelaId asociado y la semilla
    LaunchedEffect(actividadId) {
        actividadDetalleVm.cargarActividad(actividadId)
        viewModel.cargarSemilla(actividadId)
    }

    // Si la semilla ya existe, mostrar formulario con datos
    LaunchedEffect(semillaState) {
        val state = semillaState
        if (state is Result.Success && state.data != null) {
            mostrarFormulario = true
        }
    }

    // Extraer parcelaId de la actividad cargada
    val parcelaId = (actividadState as? Result.Success)?.data?.parcelaId ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Semilla Tratada") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("< Volver")
                    }
                },
                actions = {
                    val state = semillaState
                    if (state is Result.Success && state.data == null) {
                        TextButton(onClick = { mostrarFormulario = true }) {
                            Text("Añadir")
                        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = semillaState) {
                is Result.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Result.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            viewModel.cargarSemilla(actividadId)
                        }) {
                            Text("Reintentar")
                        }
                    }
                }
                is Result.Success -> {
                    val semilla = state.data
                    // No habilitar el form hasta que la actividad esté cargada — sin esto
                    // se podía guardar con parcelaId=0 si el usuario era muy rápido y el
                    // INSERT petaba con FK violation devolviendo 500 silencioso.
                    val actividadCargada = actividadState is Result.Success && parcelaId > 0

                    if (!actividadCargada) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (semilla != null || mostrarFormulario) {
                        SemillaTratadaForm(
                            semilla = semilla, // Si es null, el formulario saldrá vacío
                            productosState = productosState,
                            onGuardar = { semillaNueva ->
                                scope.launch {
                                    val semillaFinal = semillaNueva.copy(
                                        actividadId = actividadId,
                                        parcelaId = parcelaId
                                    )
                                    val resultado = viewModel.crearSemillaTratada(semillaFinal)
                                    if (resultado is Result.Success) {
                                        snackbarMensaje = "Registro guardado correctamente"
                                        mostrarSnackbar = true
                                        mostrarFormulario = false
                                    } else if (resultado is Result.Error) {
                                        snackbarMensaje = resultado.message ?: "Error al guardar"
                                        mostrarSnackbar = true
                                    }
                                }
                            }
                        )
                    } else {
                        // Mostrar pantalla vacía con botón para crear
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No hay semilla tratada registrada")
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { mostrarFormulario = true }) {
                                    Text("Registrar semilla tratada")
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
private fun SemillaTratadaForm(
    semilla: SemillaTratada?,
    productosState: Result<List<org.dferna14.project.domain.model.Producto>>,
    onGuardar: (SemillaTratada) -> Unit
) {
    val fechaHoy = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }
    var aplica by remember { mutableStateOf(semilla?.aplica ?: false) }
    var fechaSiembra by remember { mutableStateOf(semilla?.fechaSiembra ?: fechaHoy) }
    var superficieHa by remember { mutableStateOf(semilla?.superficieHa?.toString() ?: "") }
    var cantidadSemillaKg by remember { mutableStateOf(semilla?.cantidadSemillaKg?.toString() ?: "") }
    var productoId by remember { mutableStateOf(semilla?.productoId) }
    var variedadSemilla by remember { mutableStateOf(semilla?.variedadSemilla ?: "") }
    var cultivoId by remember { mutableStateOf(semilla?.cultivoId) }
    var mostrarSelectorProducto by remember { mutableStateOf(false) }

    // Obtener nombre del producto seleccionado
    val productoSeleccionado = when (productosState) {
        is Result.Success -> productosState.data.find { it.id == productoId }?.nombreComercial
        else -> null
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Aplica semilla tratada?", modifier = Modifier.weight(1f))
            Switch(
                checked = aplica,
                onCheckedChange = { aplica = it }
            )
        }

        if (aplica) {
            OutlinedTextField(
                value = fechaSiembra,
                onValueChange = { fechaSiembra = it },
                label = { Text("Fecha de siembra") },
                placeholder = { Text("YYYY-MM-DD") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = superficieHa,
                onValueChange = { superficieHa = it },
                label = { Text("Superficie (ha)") },
                placeholder = { Text("Ej: 10.5") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cantidadSemillaKg,
                onValueChange = { cantidadSemillaKg = it },
                label = { Text("Cantidad de semilla (kg)") },
                placeholder = { Text("Ej: 500") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = productoSeleccionado ?: "Seleccionar producto",
                onValueChange = { },
                label = { Text("Producto utilizado") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { mostrarSelectorProducto = true }) {
                        Text("Seleccionar")
                    }
                }
            )

            OutlinedTextField(
                value = variedadSemilla,
                onValueChange = { variedadSemilla = it },
                label = { Text("Variedad de Semilla") },
                placeholder = { Text("Ej: Trigo R01") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onGuardar(
                    SemillaTratada(
                        id = semilla?.id ?: 0,
                        actividadId = semilla?.actividadId ?: 0,
                        parcelaId = semilla?.parcelaId ?: 0,
                        aplica = aplica,
                        fechaSiembra = if (aplica) fechaSiembra else null,
                        superficieHa = superficieHa.toDoubleOrNull(),
                        cantidadSemillaKg = cantidadSemillaKg.toDoubleOrNull(),
                        productoId = if (aplica) productoId else null,
                        variedadSemilla = if (aplica) variedadSemilla else null,
                        cultivoId = if (aplica) cultivoId else null
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }

    if (mostrarSelectorProducto) {
        AlertDialog(
            onDismissRequest = { mostrarSelectorProducto = false },
            title = { Text("Seleccionar Producto") },
            text = {
                when (val state = productosState) {
                    is Result.Loading -> {
                        CircularProgressIndicator()
                    }
                    is Result.Success -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        ) {
                            items(state.data, key = { it.id }) { producto ->
                                TextButton(
                                    onClick = {
                                        productoId = producto.id
                                        mostrarSelectorProducto = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = producto.nombreComercial ?: "Producto ${producto.id}",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }
                    is Result.Error -> {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarSelectorProducto = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
