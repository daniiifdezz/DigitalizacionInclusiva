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
import org.dferna14.project.ui.viewmodel.ActividadViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemillasTratadasSc(
    actividadId: Int,
    onVolver: () -> Unit,
    viewModel: ActividadViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val semillaState = remember { mutableStateOf<Result<SemillaTratada?>>(Result.Loading) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    val productosState by viewModel.productos.collectAsState()

    // Cargar semilla tratada al iniciar
    LaunchedEffect(actividadId) {
        viewModel.getSemillaTratada(actividadId).collect { resultado ->
            semillaState.value = resultado
            if (resultado is Result.Success && resultado.data != null) {
                mostrarFormulario = true
            }
        }
    }

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
                    val state = semillaState.value
                    if (state is Result.Success && state.data == null) {
                        TextButton(onClick = { mostrarFormulario = true }) {
                            Text("Añadir")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = semillaState.value) {
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
                            viewModel.getSemillaTratada(actividadId)
                        }) {
                            Text("Reintentar")
                        }
                    }
                }
                is Result.Success -> {
                    val semilla = state.data
                    if (semilla == null && !mostrarFormulario) {
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
                    } else {
                        SemillaTratadaForm(
                            semilla = semilla,
                            productosState = productosState,
                            onGuardar = { semillaNueva ->
                                scope.launch {
                                    viewModel.crearSemillaTratada(semillaNueva)
                                    mostrarFormulario = false
                                }
                            }
                        )
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
    var aplica by remember { mutableStateOf(semilla?.aplica ?: false) }
    var fechaSiembra by remember { mutableStateOf(semilla?.fechaSiembra ?: Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()) }
    var superficieHa by remember { mutableStateOf(semilla?.superficieHa?.toString() ?: "") }
    var cantidadSemillaKg by remember { mutableStateOf(semilla?.cantidadSemillaKg?.toString() ?: "") }
    var productoId by remember { mutableStateOf(semilla?.productoId) }
    var mostrarSelectorProducto by remember { mutableStateOf(false) }

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
                value = productoId?.let { "Producto ID: $it" } ?: "Seleccionar producto",
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
                        productoId = if (aplica) productoId else null
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
                        LazyColumn {
                            items(state.data) { producto ->
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
