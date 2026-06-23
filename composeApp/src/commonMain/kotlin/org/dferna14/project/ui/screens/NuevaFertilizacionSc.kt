package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Producto
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.*
import org.dferna14.project.ui.theme.BordeSuave
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.viewmodel.ActividadListaVm
import org.dferna14.project.ui.viewmodel.FertilizacionVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun NuevaFertilizacionSc(
    onVolver: () -> Unit,
    actividadVm: ActividadListaVm = koinViewModel(),
    fertVm: FertilizacionVm       = koinViewModel(),
    parcelaVm: ParcelaVm          = koinViewModel()
) {
    val scope             = rememberCoroutineScope()
    val parcelasState     by parcelaVm.parcelas.collectAsState()
    val fertilizantesState by fertVm.fertilizantes.collectAsState()
    val mensajeErrorFert  by fertVm.mensajeError.collectAsState()
    val mensajeErrorAct   by actividadVm.mensajeError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val fechaHoy = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }

    var parcelaSeleccionada      by remember { mutableStateOf<Parcela?>(null) }
    var fertilizanteSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var numeroAlbaran            by remember { mutableStateOf("") }
    var dosis                    by remember { mutableStateOf("") }
    var observaciones            by remember { mutableStateOf("") }
    var guardando                by remember { mutableStateOf(false) }
    var actividadIdLocal         by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        fertVm.guardadoExitoso.collect {
            if (actividadIdLocal != null) {
                guardando = false
                onVolver()
            }
        }
    }

    LaunchedEffect(mensajeErrorFert) {
        mensajeErrorFert?.let {
            guardando = false
            actividadIdLocal = null
            snackbarHostState.showSnackbar(it)
            fertVm.limpiarMensajeError()
        }
    }

    LaunchedEffect(mensajeErrorAct) {
        mensajeErrorAct?.let {
            guardando = false
            snackbarHostState.showSnackbar(it)
            actividadVm.limpiarMensajeError()
        }
    }

    val parcelas = (parcelasState as? Result.Success)?.data.orEmpty()

    fun guardar() {
        val parcela = parcelaSeleccionada ?: return
        guardando = true
        scope.launch {
            val actividadId = actividadVm.crearActividadYObtenerId(
                Actividad(parcelaId = parcela.id, fechaInicio = fechaHoy)
            )
            if (actividadId == null) {
                guardando = false
                return@launch
            }
            actividadIdLocal = actividadId
            fertVm.guardarFertilizacion(
                actividadId   = actividadId,
                aplica        = true,
                productoId    = fertilizanteSeleccionado?.id,
                riquezaNpk    = fertilizanteSeleccionado?.riquezaNpk,
                numeroAlbaran = numeroAlbaran.ifBlank { null },
                dosis         = dosis.replace(",", ".").toDoubleOrNull(),
                observaciones = observaciones.ifBlank { null },
                fechaInicio   = fechaHoy
            )
        }
    }

    Scaffold(
        containerColor = CremaPrincipal,
        snackbarHost   = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavBarFormulario(titulo = "Fertilización", onVolver = onVolver)
            HorizontalDivider(color = BordeSuave, thickness = 0.5.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionHeader("Datos generales")

                CampoField(
                    label = "Fecha de inicio",
                    value = formatearFecha(fechaHoy)
                )

                CampoDropdown(
                    label        = "Parcela *",
                    selectedItem = parcelaSeleccionada,
                    items        = parcelas,
                    itemLabel    = { it.alias ?: "Parcela ${it.orden ?: it.id}" },
                    onSelect     = { parcelaSeleccionada = it },
                    placeholder  = "Selecciona una parcela"
                )

                SectionHeader("Datos de fertilización")

                CampoAvisoInfo(
                    mensaje = "Selecciona el fertilizante del catálogo. Si no está, pide al técnico que lo añada desde el escritorio."
                )

                CampoCard {
                    when (val estado = fertilizantesState) {
                        is Result.Loading -> CampoField(
                            label = "Fertilizante utilizado",
                            value = "Cargando catálogo…"
                        )
                        is Result.Error   -> CampoField(
                            label = "Fertilizante utilizado",
                            value = "No se pudo cargar el catálogo"
                        )
                        is Result.Success -> CampoDropdown(
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

                Spacer(Modifier.height(4.dp))

                if (parcelaSeleccionada == null) {
                    CampoAvisoInfo(mensaje = "Selecciona una parcela para poder guardar.")
                }

                CampoPrimaryButton(
                    text    = if (guardando) "Guardando…" else "Guardar fertilización",
                    onClick = { guardar() },
                    enabled = parcelaSeleccionada != null && !guardando
                )
            }
        }
    }
}
