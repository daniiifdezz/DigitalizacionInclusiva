package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Explotacion
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoAvisoInfo
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.components.CampoToggle
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelasSc(
    onVolver: () -> Unit,
    onEditarParcela: (Int) -> Unit,
    viewModel: ParcelaVm = koinViewModel()
) {
    val parcelasState by viewModel.parcelas.collectAsState()
    val explotacionesState by viewModel.explotaciones.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var parcelaExpandida by remember { mutableStateOf<Int?>(null) }
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var parcelaAEliminar by remember { mutableStateOf<Parcela?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarParcelas()
        viewModel.cargarExplotaciones()
    }

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
                title = { Text("Parcelas") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("< Menu")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.cargarParcelas() }) {
                        Text("Recargar")
                    }
                }
            )
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cargando parcelas...")
                    }
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Parcelas", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "${state.data.size} parcelas registradas",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoTerciario
                            )
                        }
                        CampoPrimaryButton(
                            text = "+ Nueva parcela",
                            onClick = { mostrarDialogoCrear = true },
                            modifier = Modifier.width(180.dp)
                        )
                    }

                    if (state.data.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay parcelas. Pulsa “+ Nueva parcela” para crear la primera.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.data, key = { it.id }) { parcela ->
                                ParcelaCard(
                                    parcela = parcela,
                                    expandida = parcelaExpandida == parcela.id,
                                    onToggleExpand = {
                                        parcelaExpandida = if (parcelaExpandida == parcela.id) null else parcela.id
                                    },
                                    onEditar = { onEditarParcela(parcela.id) },
                                    onEliminar = { parcelaAEliminar = parcela }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        val explotaciones = (explotacionesState as? Result.Success)?.data ?: emptyList()
        when {
            explotacionesState is Result.Loading -> CargandoExplotacionesParcelasDialog(
                onDismiss = { mostrarDialogoCrear = false }
            )
            explotaciones.isEmpty() -> SinExplotacionesParcelasDialog(
                onDismiss = { mostrarDialogoCrear = false }
            )
            else -> NuevaParcelaDesktopDialog(
                explotaciones = explotaciones,
                onDismiss = { mostrarDialogoCrear = false },
                onCrear = { nueva ->
                    viewModel.crearParcela(nueva)
                    mostrarDialogoCrear = false
                }
            )
        }
    }

    parcelaAEliminar?.let { parcela ->
        AlertDialog(
            onDismissRequest = { parcelaAEliminar = null },
            title = { Text("¿Eliminar parcela?") },
            text = {
                Text(
                    "Se eliminará \"${parcela.alias ?: "Parcela ${parcela.id}"}\" y todos sus datos asociados (SIGPAC, agronómicos). Esta acción no se puede deshacer."
                )
            },
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
    expandida: Boolean,
    onToggleExpand: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggleExpand
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = parcela.alias ?: "Parcela ${parcela.id}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Explotación: ${parcela.explotacionId ?: "—"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        tint = TextoTerciario,
                        contentDescription = "Eliminar parcela"
                    )
                }
                if (expandida) Text("▲") else Text("▼")
            }

            if (expandida) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                InfoRow("Orden", parcela.orden?.toString() ?: "No disponible")
                InfoRow("Sistema Asesoramiento", parcela.sistemaAsesoramiento ?: "No disponible")
                InfoRow("Zona Nitratos", parcela.zonaNitratos?.let { if (it) "Sí" else "No" } ?: "No disponible")

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onEditar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar SIGPAC y agronómicos")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuevaParcelaDesktopDialog(
    explotaciones: List<Explotacion>,
    onDismiss: () -> Unit,
    onCrear: (Parcela) -> Unit
) {
    var alias by remember { mutableStateOf("") }
    var explotacionSeleccionada by remember(explotaciones) { mutableStateOf(explotaciones.firstOrNull()) }
    var sistemaAsesoramiento by remember { mutableStateOf("") }
    var zonaNitratos by remember { mutableStateOf(false) }
    var desplegableExplotacion by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva parcela") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoTextField(
                    label = "Nombre / Alias *",
                    value = alias,
                    onValueChange = { alias = it },
                    placeholder = "Ej: La Vega, El Cerro…"
                )

                ExposedDropdownMenuBox(
                    expanded = desplegableExplotacion,
                    onExpandedChange = { desplegableExplotacion = it }
                ) {
                    OutlinedTextField(
                        value = explotacionSeleccionada?.nombre ?: "Selecciona explotación",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Explotación *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegableExplotacion)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = desplegableExplotacion,
                        onDismissRequest = { desplegableExplotacion = false }
                    ) {
                        explotaciones.forEach { exp ->
                            DropdownMenuItem(
                                text = { Text(exp.nombre) },
                                onClick = {
                                    explotacionSeleccionada = exp
                                    desplegableExplotacion = false
                                }
                            )
                        }
                    }
                }

                CampoTextField(
                    label = "Sistema de asesoramiento",
                    value = sistemaAsesoramiento,
                    onValueChange = { sistemaAsesoramiento = it },
                    placeholder = "Ej: Asesoramiento individual"
                )

                CampoAvisoInfo(
                    mensaje = "El sistema de asesoramiento indica cómo se gestiona el uso de fitosanitarios en esta parcela"
                )

                CampoToggle(
                    label = "Zona vulnerable a nitratos",
                    checked = zonaNitratos,
                    onCheckedChange = { zonaNitratos = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = alias.isNotBlank() && explotacionSeleccionada != null,
                onClick = {
                    val exp = explotacionSeleccionada ?: return@TextButton
                    onCrear(
                        Parcela(
                            id                   = 0,
                            explotacionId        = exp.id,
                            alias                = alias.trim(),
                            sistemaAsesoramiento = sistemaAsesoramiento.trim().ifBlank { null },
                            zonaNitratos         = zonaNitratos
                        )
                    )
                }
            ) {
                Text("Crear parcela", color = NaranjaPrimario, fontWeight = FontWeight.Medium)
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
private fun CargandoExplotacionesParcelasDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cargando…") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = NaranjaPrimario) }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextoSecundario) }
        }
    )
}

@Composable
private fun SinExplotacionesParcelasDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("No hay explotaciones") },
        text = { Text("Primero debes crear una explotación desde Configuración Inicial antes de poder añadir parcelas.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", color = NaranjaPrimario, fontWeight = FontWeight.Medium)
            }
        }
    )
}
