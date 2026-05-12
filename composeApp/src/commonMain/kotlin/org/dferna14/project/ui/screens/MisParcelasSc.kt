package org.dferna14.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.domain.model.Explotacion
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoCard
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.theme.*
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

    LaunchedEffect(mostrarDialogoCrear) {
        if (mostrarDialogoCrear) viewModel.cargarExplotaciones()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis parcelas", style = MaterialTheme.typography.titleLarge) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoCrear = true },
                containerColor = NaranjaPrimario,
                contentColor = BlancoPuro
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Nueva parcela")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = parcelasState) {
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
                            text = "No se pudieron cargar las parcelas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoSecundario
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.cargarParcelas() },
                            colors = ButtonDefaults.buttonColors(containerColor = NaranjaPrimario)
                        ) { Text("Reintentar") }
                    }
                }
                is Result.Success -> {
                    val parcelas = state.data
                    if (parcelas.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Map,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = TextoTerciario
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "No tienes parcelas registradas",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextoTerciario
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Pulsa + para añadir tu primera parcela",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoTerciario
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "${parcelas.size} parcela${if (parcelas.size != 1) "s" else ""} registrada${if (parcelas.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoTerciario,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(parcelas, key = { it.id }) { parcela ->
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
        }
    }

    // Diálogo crear parcela, depende de explotaciones
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

    // Diálogo confirmar eliminación
    parcelaAEliminar?.let { parcela ->
        val nombre = parcela.alias ?: "Parcela ${parcela.id}"
        AlertDialog(
            onDismissRequest = { parcelaAEliminar = null },
            title = { Text("¿Eliminar parcela?") },
            text = { Text("Esta acción no se puede deshacer. ¿Seguro que quieres eliminar \"$nombre\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarParcela(parcela.id)
                    parcelaAEliminar = null
                }) {
                    Text("Eliminar", color = RojoEliminar, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { parcelaAEliminar = null }) {
                    Text("Cancelar", color = TextoSecundario)
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
                    .background(VerdeFondoInfo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = VerdeValidada
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parcela.alias ?: "Parcela ${parcela.id}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextoPrimario
                )
                val subtitulo = buildList {
                    parcela.sistemaAsesoramiento?.let { add(it) }
                    if (parcela.zonaNitratos == true) add("Zona nitratos")
                }.joinToString(" · ")
                if (subtitulo.isNotBlank()) {
                    Text(
                        text = subtitulo,
                        fontSize = 12.sp,
                        color = TextoTerciario
                    )
                }
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Eliminar parcela",
                    tint = TextoTerciario
                )
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
    var explotacionSeleccionada by remember(explotaciones) {
        mutableStateOf(explotaciones.firstOrNull())
    }
    var dropdownAbierto by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva parcela") },
        text = {
            Column {
                Text(
                    text = "Introduce un nombre para identificar la parcela",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoTerciario
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
                            modifier = Modifier.fillMaxWidth().menuAnchor()
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
                CampoTextField(
                    label = "Nombre / Alias",
                    value = alias,
                    onValueChange = { alias = it },
                    placeholder = "Ej: Parcela Norte"
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val expId = explotacionSeleccionada?.id ?: return@TextButton
                    onCreate(alias.ifBlank { null }, expId)
                },
                enabled = explotacionSeleccionada != null
            ) {
                Text("Crear", color = NaranjaPrimario, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextoSecundario)
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
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NaranjaPrimario)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextoSecundario) }
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
            TextButton(onClick = onDismiss) { Text("Entendido", color = NaranjaPrimario, fontWeight = FontWeight.Medium) }
        }
    )
}
