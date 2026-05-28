package org.dferna14.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoCard
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.EstadoBadge
import org.dferna14.project.ui.components.formatearFecha
import org.dferna14.project.ui.theme.*
import org.dferna14.project.ui.viewmodel.ActividadListaVm
import org.koin.compose.viewmodel.koinViewModel

/**
 * Listado de actividades agrícolas. Diseño accesible para agricultores mayores:
 * texto grande, tarjetas amplias, zonas táctiles generosas.
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

    // Refrescar al entrar
    LaunchedEffect(Unit) {
        viewModel.cargarActividades()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis actividades", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    if (isDesktop && onVolver != null) {
                        TextButton(onClick = onVolver) { Text("< Menú") }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevaActividad,
                text = { Text("Nueva actividad") },
                icon = { Text("+", style = MaterialTheme.typography.titleMedium) },
                containerColor = NaranjaPrimario,
                contentColor = BlancoPuro
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
                        color = NaranjaPrimario,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Result.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No se pudieron cargar las actividades",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoSecundario
                        )
                        Spacer(Modifier.height(12.dp))
                        CampoPrimaryButton(
                            text = "Reintentar",
                            onClick = { viewModel.cargarActividades() },
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
                is Result.Success -> {
                    val actividades = estado.data
                    if (actividades.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = TextoTerciario
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "No tienes actividades registradas",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextoTerciario
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Pulsa + para crear tu primera actividad",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoTerciario
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "${actividades.size} registro${if (actividades.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoTerciario,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(actividades, key = { it.id }) { actividad ->
                                    ActividadCard(
                                        actividad = actividad,
                                        onClick = { onVerDetalle(actividad.id) }
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
private fun ActividadCard(
    actividad: Actividad,
    onClick: () -> Unit
) {
    CampoCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Parcela ${actividad.parcelaId}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextoPrimario,
                modifier = Modifier.weight(1f)
            )
            EstadoBadge(actividad.estado)
        }

        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = TextoTerciario
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = formatearFecha(actividad.fechaInicio),
                fontSize = 12.sp,
                color = TextoTerciario
            )
        }

        val problema = actividad.problemaFitosanitario
        if (!problema.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CremaSecundario)
                    .border(0.5.dp, BordeSuave, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = problema,
                    fontSize = 12.sp,
                    color = TextoSecundario
                )
            }
        }
    }
}

