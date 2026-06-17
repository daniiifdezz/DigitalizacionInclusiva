package org.dferna14.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import org.dferna14.project.domain.model.Explotacion
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoDropdown
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
    val mensajeError by viewModel.mensajeError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var parcelaAEliminar by remember { mutableStateOf<Parcela?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarParcelas()
    }
    LaunchedEffect(mostrarDialogoCrear) {
        if (mostrarDialogoCrear) viewModel.cargarExplotaciones()
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
                title = { Text("Mis parcelas", style = MaterialTheme.typography.titleLarge, color = TextoPrimario) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SuperficieSepia)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoCrear = true },
                containerColor = OlivaPrimario,
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
                            text = "No se pudieron cargar las parcelas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoSecundario
                        )
                        Spacer(Modifier.height(12.dp))
                        CampoPrimaryButton(
                            text = "Reintentar",
                            onClick = { viewModel.cargarParcelas() },
                            modifier = Modifier.width(180.dp)
                        )
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
            containerColor = SuperficieSepia,
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
                .background(OlivaPrimario)
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
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = OlivaPrimario
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parcela.alias ?: "Parcela ${parcela.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario
                )
                val subtitulo = buildList {
                    parcela.sistemaAsesoramiento?.let { add(it) }
                    if (parcela.zonaNitratos == true) add("Zona nitratos")
                }.joinToString(" · ")
                if (subtitulo.isNotBlank()) {
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.labelSmall,
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SuperficieSepia,
        title = { Text("Nueva parcela") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Introduce un nombre para identificar la parcela",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoTerciario
                )

                if (explotaciones.size > 1) {
                    CampoDropdown(
                        label = "Explotación *",
                        selectedItem = explotacionSeleccionada,
                        items = explotaciones,
                        itemLabel = { it.nombre },
                        onSelect = { explotacionSeleccionada = it },
                        placeholder = "Selecciona explotación"
                    )
                }

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
                Text("Crear", color = OlivaPrimario, fontWeight = FontWeight.Medium)
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
        containerColor = SuperficieSepia,
        title = { Text("Cargando…") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OlivaPrimario)
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
        containerColor = SuperficieSepia,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", color = OlivaPrimario, fontWeight = FontWeight.Medium)
            }
        }
    )
}
