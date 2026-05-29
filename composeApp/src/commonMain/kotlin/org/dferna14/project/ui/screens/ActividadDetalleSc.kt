package org.dferna14.project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.EstadoActividad
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.*
import org.dferna14.project.ui.theme.*
import org.dferna14.project.ui.viewmodel.ActividadDetalleVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ActividadDetalleSc(
    actividadId: Int,
    onVolver: () -> Unit,
    onEditar: (Int) -> Unit,
    onVerSemillas: (Int) -> Unit = {},
    onVerFertilizacion: (Int) -> Unit = {},
    viewModel: ActividadDetalleVm = koinViewModel(),
    parcelaVm: ParcelaVm = koinViewModel()
) {
    val actividadState by viewModel.actividadActual.collectAsState()
    val parcelasState by parcelaVm.parcelas.collectAsState()
    val operacionExitosa by viewModel.operacionExitosa.collectAsState()
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actividadId) {
        viewModel.cargarActividad(actividadId)
    }
    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            snackbarHostState.showSnackbar("Operación realizada con éxito")
            viewModel.resetOperacionExitosa()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavBarFormulario(
                titulo = "Actividad",
                onVolver = onVolver,
                accionDerecha = {
                    val act = (actividadState as? Result.Success)?.data
                    if (act != null && act.estado == EstadoActividad.BORRADOR) {
                        TextButton(onClick = { onEditar(actividadId) }) {
                            Text("Editar", color = NaranjaPrimario, fontSize = 13.sp)
                        }
                    } else {
                        Spacer(Modifier.width(60.dp))
                    }
                }
            )
            HorizontalDivider(color = BordeSuave, thickness = 0.5.dp)

            when (val estado = actividadState) {
                is Result.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NaranjaPrimario)
                    }
                }
                is Result.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No se pudo cargar la actividad",
                            style = MaterialTheme.typography.bodyLarge,
                            color = RojoEliminar
                        )
                        Spacer(Modifier.height(12.dp))
                        CampoPrimaryButton(
                            text = "Reintentar",
                            onClick = { viewModel.cargarActividad(actividadId) },
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
                is Result.Success -> {
                    val act = estado.data
                    val nombreParcela = act.parcelaAlias
                        ?: (parcelasState as? Result.Success)?.data
                            ?.find { it.id == act.parcelaId }
                            ?.let { it.alias ?: "Parcela ${it.orden ?: it.id}" }
                        ?: "Parcela ${act.parcelaId}"

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            EstadoBadge(act.estado)
                        }

                        CampoField(label = "Parcela", value = nombreParcela)
                        CampoField(label = "Fecha de inicio", value = formatearFecha(act.fechaInicio))
                        act.superficieTratada?.let {
                            CampoField(label = "Superficie tratada", value = "$it ha")
                        }
                        act.problemaFitosanitario?.takeIf { it.isNotBlank() }?.let {
                            CampoField(label = "Problema fitosanitario", value = it)
                        }
                        act.observaciones?.takeIf { it.isNotBlank() }?.let {
                            CampoField(label = "Observaciones", value = it)
                        }
                        act.eficacia?.takeIf { it.isNotBlank() }?.let {
                            CampoField(label = "Eficacia", value = it)
                        }

                        if (act.estado == EstadoActividad.PENDIENTE_VALIDAR) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AzulFondoPendiente),
                                border = BorderStroke(0.5.dp, AzulPendiente.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = AzulPendiente,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Pendiente de validación",
                                            color = AzulPendiente,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "El técnico de escritorio debe revisar y validar esta actividad",
                                            color = AzulPendiente.copy(alpha = 0.8f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = BordeSuave,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        CampoSecondaryButton(
                            text = "Semillas tratadas",
                            icon = Icons.Outlined.Grass,
                            onClick = { onVerSemillas(actividadId) }
                        )
                        Spacer(Modifier.height(8.dp))
                        CampoSecondaryButton(
                            text = "Fertilización básica",
                            icon = Icons.Outlined.WaterDrop,
                            onClick = { onVerFertilizacion(act.parcelaId) }
                        )

                        if (act.estado == EstadoActividad.BORRADOR) {
                            Spacer(Modifier.height(12.dp))
                            CampoPrimaryButton(
                                text = "Enviar para validación",
                                onClick = { viewModel.enviarActividad(actividadId) }
                            )
                        }

                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = { mostrarDialogoEliminar = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = RojoEliminar,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Eliminar actividad", color = RojoEliminar, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("¿Eliminar actividad?") },
            text = { Text("Esta acción no se puede deshacer. ¿Seguro que quieres eliminar esta actividad?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarActividad(actividadId)
                    mostrarDialogoEliminar = false
                    onVolver()
                }) {
                    Text("Eliminar", color = RojoEliminar, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
