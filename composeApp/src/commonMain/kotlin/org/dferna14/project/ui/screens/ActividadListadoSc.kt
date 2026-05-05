package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.EstadoActividad
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ActividadListaVm
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla principal — listado de actividades agrícolas.
 * Diseñada para ser accesible: texto grande y botones amplios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadListadoSc(
    onNuevaActividad: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    isDesktop: Boolean = false,
    onVolver: (() -> Unit)? = null,
    viewModel: ActividadListaVm = koinViewModel()
) {
    val actividadesState by viewModel.actividades.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Actividades", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    if (isDesktop && onVolver != null) {
                        TextButton(onClick = onVolver) {
                            Text("< Menu")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevaActividad,
                text = { Text("Nueva actividad") },
                icon = { Text("+") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val estado = actividadesState) {
                is Result.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Result.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error al cargar actividades",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.cargarActividades() }) {
                            Text("Reintentar")
                        }
                    }
                }
                is Result.Success -> {
                    if (estado.data.isEmpty()) {
                        Text(
                            text = "No hay actividades registradas",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(estado.data, key = { it.id }) { actividad ->
                                ActividadCard(
                                    actividad = actividad,
                                    onClick = { onVerDetalle(actividad.id) },
                                    onEliminar = { viewModel.eliminarActividad(actividad.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de una actividad individual.
 * Botones grandes para facilitar el uso en campo.
 */
@Composable
fun ActividadCard(
    actividad: Actividad,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
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
                Text(
                    text = "Parcela ${actividad.parcelaId}",
                    style = MaterialTheme.typography.titleMedium
                )
                EstadoChip(estado = actividad.estado)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fecha: ${actividad.fechaInicio}",
                style = MaterialTheme.typography.bodyMedium
            )
            actividad.problemaFitosanitario?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Problema: $it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            actividad.observaciones?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Observaciones: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (actividad.estado.esEditable()) {
                    TextButton(onClick = onEliminar) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun EstadoChip(estado: EstadoActividad) {
    val (color, texto) = when (estado) {
        EstadoActividad.BORRADOR -> Pair(Color(0xFFFFA000), "Borrador")
        EstadoActividad.PENDIENTE_VALIDAR -> Pair(Color(0xFF1976D2), "Pendiente")
        EstadoActividad.VALIDADA -> Pair(Color(0xFF388E3C), "Validada")
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = texto,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}