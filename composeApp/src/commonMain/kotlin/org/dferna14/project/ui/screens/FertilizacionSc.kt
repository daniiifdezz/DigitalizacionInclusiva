package org.dferna14.project.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.dferna14.project.domain.model.Producto
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoAvisoInfo
import org.dferna14.project.ui.components.CampoCard
import org.dferna14.project.ui.components.CampoDropdown
import org.dferna14.project.ui.components.CampoField
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.components.CampoTextoConOcr
import org.dferna14.project.ui.components.CampoToggle
import org.dferna14.project.ui.components.SectionHeader
import org.dferna14.project.ui.components.formatearFecha
import org.dferna14.project.ui.theme.BordeSuave
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.viewmodel.FertilizacionVm
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/**
 * Pantalla móvil de fertilización. El agricultor elige fertilizante
 * del catálogo y rellena albarán, dosis y observaciones. Los campos técnicos
 * (NPK, tipoFertilizacion, tipoProducto) los completa el técnico desde Desktop.
 */
@Composable
fun FertilizacionSc(
    parcelaId: Int,
    actividadId: Int,
    onVolver: () -> Unit,
    viewModel: FertilizacionVm = koinViewModel()
) {
    if (parcelaId <= 0) {
        Column(modifier = Modifier.fillMaxSize()) {
            NavBarFormulario(titulo = "Fertilización básica", onVolver = onVolver)
            HorizontalDivider(color = BordeSuave, thickness = 0.5.dp)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.padding(24.dp)
                ) {
                    Text(
                        text  = "No hay parcela asociada a esta actividad",
                        color = RojoEliminar,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "Asocia una parcela antes de registrar la fertilización.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        return
    }

    val fertilizacionState by viewModel.fertilizacion.collectAsState()
    val fertilizantesState by viewModel.fertilizantes.collectAsState()
    val mensajeError       by viewModel.mensajeError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val fechaHoy = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }
    var aplica                   by remember { mutableStateOf(false) }
    var fertilizanteSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var numeroAlbaran            by remember { mutableStateOf("") }
    var dosis                    by remember { mutableStateOf("") }
    var observaciones            by remember { mutableStateOf("") }
    var camposPrecargados        by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarFertilizantes()
    }

    LaunchedEffect(actividadId) {
        if (actividadId > 0) viewModel.cargarFertilizacion(actividadId)
    }

    // Precarga al volver a entrar. Espera a que el catálogo de fertilizantes
    // esté cargado para poder resolver productoId → Producto en el dropdown.
    LaunchedEffect(fertilizacionState, fertilizantesState) {
        if (camposPrecargados) return@LaunchedEffect
        val fert    = (fertilizacionState as? Result.Success)?.data ?: return@LaunchedEffect
        val catalogo = (fertilizantesState as? Result.Success)?.data ?: return@LaunchedEffect
        aplica                   = fert.aplica
        numeroAlbaran            = fert.numeroAlbaran.orEmpty()
        dosis                    = fert.dosis?.toString().orEmpty()
        observaciones            = fert.observaciones.orEmpty()
        fertilizanteSeleccionado = fert.productoId?.let { pid -> catalogo.find { it.id == pid } }
        camposPrecargados        = true
    }

    LaunchedEffect(Unit) {
        viewModel.guardadoExitoso.collect { _ ->
            onVolver()
        }
    }

    LaunchedEffect(mensajeError) {
        mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajeError()
        }
    }

    Scaffold(
        containerColor = CremaPrincipal,
        snackbarHost   = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavBarFormulario(titulo = "Fertilización básica", onVolver = onVolver)
            HorizontalDivider(color = BordeSuave, thickness = 0.5.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CampoToggle(
                    label           = "¿Aplica fertilización?",
                    checked         = aplica,
                    onCheckedChange = { aplica = it }
                )

                AnimatedVisibility(visible = aplica) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader("Datos de fertilización")

                        CampoAvisoInfo(
                            mensaje = "Selecciona el fertilizante del catálogo. Si no está, pide al técnico que lo añada desde el escritorio."
                        )

                        CampoCard {
                            CampoField(label = "Fecha de inicio", value = formatearFecha(fechaHoy))

                            when (val estado = fertilizantesState) {
                                is Result.Loading -> {
                                    CampoField(
                                        label = "Fertilizante utilizado",
                                        value = "Cargando catálogo…"
                                    )
                                }
                                is Result.Error -> {
                                    CampoField(
                                        label = "Fertilizante utilizado",
                                        value = "No se pudo cargar el catálogo"
                                    )
                                }
                                is Result.Success -> {
                                    CampoDropdown(
                                        label        = "Fertilizante utilizado",
                                        selectedItem = fertilizanteSeleccionado,
                                        items        = estado.data,
                                        itemLabel    = { p ->
                                            val sufijo = p.riquezaNpk?.let { " · NPK $it" }.orEmpty()
                                            "${p.nombreComercial}$sufijo"
                                        },
                                        onSelect     = { fertilizanteSeleccionado = it },
                                        placeholder  = if (estado.data.isEmpty())
                                            "No hay fertilizantes en el catálogo"
                                        else "Selecciona fertilizante"
                                    )
                                }
                            }

                            CampoTextoConOcr(
                                value         = numeroAlbaran,
                                onValueChange = { numeroAlbaran = it },
                                label         = "Nº Albarán",
                                placeholder   = "Número del albarán",
                                modifier      = Modifier.fillMaxWidth()
                            )

                            CampoTextField(
                                label         = "Dosis aplicada (kg/ha)",
                                value         = dosis,
                                onValueChange = { dosis = it },
                                keyboardType  = KeyboardType.Decimal
                            )
                        }

                        SectionHeader("Observaciones")

                        CampoTextField(
                            label         = "Observaciones (opcional)",
                            value         = observaciones,
                            onValueChange = { observaciones = it },
                            minLines      = 2
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                CampoPrimaryButton(
                    text    = "Guardar fertilización",
                    onClick = {
                        viewModel.guardarFertilizacion(
                            actividadId   = actividadId,
                            aplica        = aplica,
                            productoId    = if (aplica) fertilizanteSeleccionado?.id else null,
                            riquezaNpk    = if (aplica) fertilizanteSeleccionado?.riquezaNpk else null,
                            numeroAlbaran = if (aplica) numeroAlbaran.ifBlank { null } else null,
                            dosis         = if (aplica) dosis.toDoubleOrNull() else null,
                            observaciones = if (aplica) observaciones.ifBlank { null } else null,
                            fechaInicio   = if (aplica) fechaHoy else null
                        )
                    }
                )
            }
        }
    }
}
