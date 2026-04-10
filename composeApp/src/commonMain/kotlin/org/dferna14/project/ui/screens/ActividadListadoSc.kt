package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ActividadViewModel
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
    viewModel: ActividadViewModel = koinViewModel()
) {
    val actividadesState by viewModel.actividades.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Actividades", style = MaterialTheme.typography.headlineSmall) }
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
                            items(estado.data) { actividad ->
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
            Text(
                text = "Parcela ${actividad.parcelaId}",
                style = MaterialTheme.typography.titleMedium
            )
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
                TextButton(onClick = onEliminar) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}