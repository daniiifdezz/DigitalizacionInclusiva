package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoDropdown
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.components.CampoTextoMultilinea
import org.dferna14.project.ui.components.SectionHeader
import org.dferna14.project.ui.theme.BordeSuave
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.viewmodel.ActividadDetalleVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditarActividadSc(
    actividadId: Int,
    onVolver: () -> Unit,
    viewModel: ActividadDetalleVm = koinViewModel(),
    parcelaVm: ParcelaVm = koinViewModel()
) {
    val actividadState by viewModel.actividadActual.collectAsState()
    val parcelasState by parcelaVm.parcelas.collectAsState()
    val operacionExitosa by viewModel.operacionExitosa.collectAsState()

    var parcelaSeleccionada by remember { mutableStateOf<Parcela?>(null) }
    var fechaInicio by remember { mutableStateOf("") }
    var superficieTratada by remember { mutableStateOf("") }
    var problemaFitosanitario by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var datosCargados by remember { mutableStateOf(false) }

    LaunchedEffect(actividadId) {
        viewModel.cargarActividad(actividadId)
    }

    LaunchedEffect(actividadState) {
        if (!datosCargados && actividadState is Result.Success) {
            val act = (actividadState as Result.Success).data
            fechaInicio           = act.fechaInicio
            superficieTratada     = act.superficieTratada?.toString() ?: ""
            problemaFitosanitario = act.problemaFitosanitario ?: ""
            observaciones         = act.observaciones ?: ""
            datosCargados         = true
        }
    }

    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            viewModel.resetOperacionExitosa()
            onVolver()
        }
    }

    Scaffold(containerColor = CremaPrincipal) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavBarFormulario(titulo = "Editar actividad", onVolver = onVolver)
            HorizontalDivider(color = BordeSuave, thickness = 0.5.dp)

            when (val estado = actividadState) {
                is Result.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OlivaPrimario)
                    }
                }
                is Result.Error -> {
                    Column(
                        modifier            = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text  = "Error al cargar la actividad",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoSecundario
                        )
                        Spacer(Modifier.height(12.dp))
                        CampoPrimaryButton(
                            text     = "Reintentar",
                            onClick  = { viewModel.cargarActividad(actividadId) },
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
                is Result.Success -> {
                    val parcelaInicial = (parcelasState as? Result.Success)?.data
                        ?.find { it.id == estado.data.parcelaId }

                    LaunchedEffect(parcelaInicial) {
                        if (parcelaSeleccionada == null && parcelaInicial != null) {
                            parcelaSeleccionada = parcelaInicial
                        }
                    }

                    val parcelas = (parcelasState as? Result.Success)?.data.orEmpty()

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionHeader("Datos generales")

                        CampoTextField(
                            label         = "Fecha de inicio (AAAA-MM-DD)",
                            value         = fechaInicio,
                            onValueChange = { fechaInicio = it }
                        )

                        CampoDropdown(
                            label        = "Parcela *",
                            selectedItem = parcelaSeleccionada,
                            items        = parcelas,
                            itemLabel    = { it.alias ?: "Parcela ${it.orden ?: it.id}" },
                            onSelect     = { parcelaSeleccionada = it },
                            placeholder  = "Selecciona una parcela"
                        )

                        CampoTextField(
                            label         = "Superficie tratada (ha)",
                            value         = superficieTratada,
                            onValueChange = { superficieTratada = it },
                            keyboardType  = KeyboardType.Decimal
                        )

                        SectionHeader("Problema fitosanitario")

                        CampoTextoMultilinea(
                            label         = "Problema fitosanitario",
                            value         = problemaFitosanitario,
                            onValueChange = { problemaFitosanitario = it },
                            minLines      = 2
                        )

                        SectionHeader("Observaciones")

                        CampoTextoMultilinea(
                            label         = "Observaciones",
                            value         = observaciones,
                            onValueChange = { observaciones = it },
                            minLines      = 3
                        )

                        Spacer(Modifier.height(4.dp))

                        CampoPrimaryButton(
                            text    = "Guardar cambios",
                            enabled = parcelaSeleccionada != null && fechaInicio.isNotBlank(),
                            onClick = {
                                val parcela = parcelaSeleccionada
                                if (parcela != null) {
                                    viewModel.actualizarActividad(
                                        Actividad(
                                            id                    = actividadId,
                                            parcelaId             = parcela.id,
                                            equipoId              = null,
                                            aplicadorId           = null,
                                            fechaInicio           = fechaInicio,
                                            superficieTratada     = superficieTratada.toDoubleOrNull(),
                                            problemaFitosanitario = problemaFitosanitario.ifBlank { null },
                                            observaciones         = observaciones.ifBlank { null }
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
