package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.EquipoAplicacion
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.Usuario
import org.dferna14.project.ui.viewmodel.ActividadDetalleVm
import org.dferna14.project.ui.viewmodel.EquipoVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.dferna14.project.ui.viewmodel.UsuarioVm
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarActividadSc(
    actividadId: Int,
    onVolver: () -> Unit,
    viewModel: ActividadDetalleVm = koinViewModel(),
    parcelaVm: ParcelaVm = koinViewModel(),
    equipoVm: EquipoVm = koinViewModel(),
    usuarioVm: UsuarioVm = koinViewModel()
) {
    val actividadState by viewModel.actividadActual.collectAsState()
    val parcelasState by parcelaVm.parcelas.collectAsState()
    val equiposState by equipoVm.equipos.collectAsState()
    val usuariosState by usuarioVm.usuarios.collectAsState()
    val operacionExitosa by viewModel.operacionExitosa.collectAsState()

    var parcelaSeleccionada by remember { mutableStateOf<Parcela?>(null) }
    var equipoSeleccionado by remember { mutableStateOf<EquipoAplicacion?>(null) }
    var aplicadorSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var fechaInicio by remember { mutableStateOf("") }
    var superficieTratada by remember { mutableStateOf("") }
    var problemaFitosanitario by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var desplegableParcela by remember { mutableStateOf(false) }
    var desplegableEquipo by remember { mutableStateOf(false) }
    var desplegableAplicador by remember { mutableStateOf(false) }
    var datosCargados by remember { mutableStateOf(false) }

    LaunchedEffect(actividadId) {
        viewModel.cargarActividad(actividadId)
    }

    LaunchedEffect(actividadState) {
        if (!datosCargados && actividadState is Result.Success) {
            val act = (actividadState as Result.Success).data
            fechaInicio = act.fechaInicio
            superficieTratada = act.superficieTratada?.toString() ?: ""
            problemaFitosanitario = act.problemaFitosanitario ?: ""
            observaciones = act.observaciones ?: ""
            datosCargados = true
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
                title = { Text("Editar Actividad") },
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
                    Text("Error al cargar la actividad")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.cargarActividad(actividadId) }) {
                        Text("Reintentar")
                    }
                }
            }
            is Result.Success -> {
                val parcelaInicial = (parcelasState as? Result.Success)?.data?.find { it.id == estado.data.parcelaId }
                val equipoInicial = (equiposState as? Result.Success)?.data?.find { it.id == estado.data.equipoId }
                val aplicadorInicial = (usuariosState as? Result.Success)?.data?.find { it.id == estado.data.aplicadorId }

                LaunchedEffect(parcelaInicial) {
                    if (parcelaSeleccionada == null && parcelaInicial != null) parcelaSeleccionada = parcelaInicial
                }
                LaunchedEffect(equipoInicial) {
                    if (equipoSeleccionado == null && equipoInicial != null) equipoSeleccionado = equipoInicial
                }
                LaunchedEffect(aplicadorInicial) {
                    if (aplicadorSeleccionado == null && aplicadorInicial != null) aplicadorSeleccionado = aplicadorInicial
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = { fechaInicio = it },
                        label = { Text("Fecha de inicio (AAAA-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = desplegableParcela,
                        onExpandedChange = { desplegableParcela = it }
                    ) {
                        OutlinedTextField(
                            value = parcelaSeleccionada?.let { it.alias ?: "Parcela ${it.orden ?: it.id}" } ?: "Selecciona una parcela",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Parcela *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegableParcela) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = desplegableParcela,
                            onDismissRequest = { desplegableParcela = false }
                        ) {
                            when (val s = parcelasState) {
                                is Result.Success -> s.data.forEach { parcela ->
                                    DropdownMenuItem(
                                        text = { Text(parcela.alias ?: "Parcela ${parcela.orden ?: parcela.id}") },
                                        onClick = {
                                            parcelaSeleccionada = parcela
                                            desplegableParcela = false
                                        }
                                    )
                                }
                                else -> DropdownMenuItem(text = { Text("Cargando...") }, onClick = {})
                            }
                        }
                    }

                    EquipoDropdown(
                        equipoSeleccionado = equipoSeleccionado,
                        equiposState = equiposState,
                        expandido = desplegableEquipo,
                        onExpandidoChange = { desplegableEquipo = it },
                        onSeleccionar = {
                            equipoSeleccionado = it
                            desplegableEquipo = false
                        }
                    )

                    AplicadorDropdown(
                        aplicadorSeleccionado = aplicadorSeleccionado,
                        usuariosState = usuariosState,
                        expandido = desplegableAplicador,
                        onExpandidoChange = { desplegableAplicador = it },
                        onSeleccionar = {
                            aplicadorSeleccionado = it
                            desplegableAplicador = false
                        }
                    )

                    OutlinedTextField(
                        value = superficieTratada,
                        onValueChange = { superficieTratada = it },
                        label = { Text("Superficie tratada (ha)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = problemaFitosanitario,
                        onValueChange = { problemaFitosanitario = it },
                        label = { Text("Problema fitosanitario") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val parcela = parcelaSeleccionada ?: return@Button
                            viewModel.actualizarActividad(
                                Actividad(
                                    id = actividadId,
                                    parcelaId = parcela.id,
                                    equipoId = equipoSeleccionado?.id,
                                    aplicadorId = aplicadorSeleccionado?.id,
                                    fechaInicio = fechaInicio,
                                    superficieTratada = superficieTratada.toDoubleOrNull(),
                                    problemaFitosanitario = problemaFitosanitario.ifBlank { null },
                                    observaciones = observaciones.ifBlank { null }
                                )
                            )
                        },
                        enabled = parcelaSeleccionada != null && fechaInicio.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Guardar cambios")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EquipoDropdown(
    equipoSeleccionado: EquipoAplicacion?,
    equiposState: Result<List<EquipoAplicacion>>,
    expandido: Boolean,
    onExpandidoChange: (Boolean) -> Unit,
    onSeleccionar: (EquipoAplicacion?) -> Unit,
    label: String = "Equipo de aplicación"
) {
    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = onExpandidoChange
    ) {
        OutlinedTextField(
            value = equipoSeleccionado?.let { eq ->
                listOfNotNull(eq.tipo, eq.marca, eq.modelo).joinToString(" ").ifBlank { "Equipo ${eq.id}" }
            } ?: "Sin asignar",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { onExpandidoChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text("Sin asignar") },
                onClick = { onSeleccionar(null) }
            )
            when (val s = equiposState) {
                is Result.Success -> s.data.forEach { eq ->
                    DropdownMenuItem(
                        text = {
                            Text(listOfNotNull(eq.tipo, eq.marca, eq.modelo).joinToString(" ").ifBlank { "Equipo ${eq.id}" })
                        },
                        onClick = { onSeleccionar(eq) }
                    )
                }
                is Result.Error -> DropdownMenuItem(
                    text = { Text("Error al cargar equipos") },
                    onClick = {}
                )
                is Result.Loading -> DropdownMenuItem(
                    text = { Text("Cargando equipos...") },
                    onClick = {}
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AplicadorDropdown(
    aplicadorSeleccionado: Usuario?,
    usuariosState: Result<List<Usuario>>,
    expandido: Boolean,
    onExpandidoChange: (Boolean) -> Unit,
    onSeleccionar: (Usuario?) -> Unit,
    label: String = "Aplicador"
) {
    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = onExpandidoChange
    ) {
        OutlinedTextField(
            value = aplicadorSeleccionado?.let { u ->
                listOfNotNull(u.nombre, u.apellidos).joinToString(" ")
            } ?: "Sin asignar",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { onExpandidoChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text("Sin asignar") },
                onClick = { onSeleccionar(null) }
            )
            when (val s = usuariosState) {
                is Result.Success -> s.data.forEach { u ->
                    DropdownMenuItem(
                        text = { Text(listOfNotNull(u.nombre, u.apellidos).joinToString(" ")) },
                        onClick = { onSeleccionar(u) }
                    )
                }
                is Result.Error -> DropdownMenuItem(
                    text = { Text("Error al cargar usuarios") },
                    onClick = {}
                )
                is Result.Loading -> DropdownMenuItem(
                    text = { Text("Cargando usuarios...") },
                    onClick = {}
                )
            }
        }
    }
}
