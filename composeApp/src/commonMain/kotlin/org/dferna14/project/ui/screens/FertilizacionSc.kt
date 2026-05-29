package org.dferna14.project.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.dferna14.project.domain.model.Fertilizacion
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoAvisoInfo
import org.dferna14.project.ui.components.CampoDropdown
import org.dferna14.project.ui.components.CampoField
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.components.CampoToggle
import org.dferna14.project.ui.components.formatearFecha
import org.dferna14.project.ui.theme.BordeSuave
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.viewmodel.FertilizacionVm
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/** Opción de un desplegable de código corto + nombre completo */
private data class OpcionCodigo(val codigo: String, val nombre: String) {
    val etiqueta: String get() = "$codigo — $nombre"
}

private val TIPOS_PRODUCTO = listOf(
    OpcionCodigo("EB", "Estiércol de bovino"),
    OpcionCodigo("EO", "Estiércol de ovino/caprino"),
    OpcionCodigo("EP", "Estiércol de porcino"),
    OpcionCodigo("PP", "Purín de porcino"),
    OpcionCodigo("G", "Gallinaza"),
    OpcionCodigo("L", "Lodos de depuradora"),
    OpcionCodigo("C", "Compost"),
    OpcionCodigo("O", "Otros")
)

private val TIPOS_FERTILIZACION = listOf(
    OpcionCodigo("F", "Fondo"),
    OpcionCodigo("AF", "Aporte foliar"),
    OpcionCodigo("AC", "Aporte cobertera")
)

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
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "No hay parcela asociada a esta actividad",
                        color = RojoEliminar,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Asocia una parcela antes de registrar la fertilización.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        return
    }

    val scope = rememberCoroutineScope()
    var guardando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val fechaHoy = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }
    var aplica by remember { mutableStateOf(false) }
    var numeroAlbaran by remember { mutableStateOf("") }
    var tipoProductoSel by remember { mutableStateOf<OpcionCodigo?>(null) }
    var riquezaNPK by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var tipoFertilizacionSel by remember { mutableStateOf<OpcionCodigo?>(null) }
    var observaciones by remember { mutableStateOf("") }
    var camposPrecargados by remember { mutableStateOf(false) }

    val fertilizacionState by viewModel.fertilizacion.collectAsState()

    LaunchedEffect(actividadId) {
        if (actividadId > 0) viewModel.cargarFertilizacion(actividadId)
    }

    // Prellenamos campos cuando el backend devuelve una fertilización guardada
    LaunchedEffect(fertilizacionState) {
        if (camposPrecargados) return@LaunchedEffect
        val fert = (fertilizacionState as? Result.Success)?.data ?: return@LaunchedEffect
        aplica = fert.aplica
        numeroAlbaran = fert.numeroAlbaran.orEmpty()
        tipoProductoSel = TIPOS_PRODUCTO.find { it.codigo == fert.tipoProducto }
        riquezaNPK = fert.riquezaNPK.orEmpty()
        dosis = fert.dosis?.toString().orEmpty()
        tipoFertilizacionSel = TIPOS_FERTILIZACION.find { it.codigo == fert.tipoFertilizacion }
        observaciones = fert.observaciones.orEmpty()
        camposPrecargados = true
    }

    Scaffold { padding ->
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
                    label = "¿Aplica fertilización?",
                    checked = aplica,
                    onCheckedChange = { aplica = it }
                )

                AnimatedVisibility(visible = aplica) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CampoField(label = "Fecha de inicio", value = formatearFecha(fechaHoy))

                        CampoTextField(
                            label = "Número de albarán",
                            value = numeroAlbaran,
                            onValueChange = { numeroAlbaran = it },
                            trailingIcon = {
                                IconButton(
                                    onClick = { /* TODO: OCR */ },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CameraAlt,
                                        contentDescription = "Escanear albarán con cámara",
                                        tint = NaranjaPrimario,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )
                        CampoAvisoInfo(
                            mensaje = "Próximamente: escanea el albarán con la cámara automáticamente"
                        )

                        CampoDropdown(
                            label = "Tipo de producto",
                            selectedItem = tipoProductoSel,
                            items = TIPOS_PRODUCTO,
                            itemLabel = { it.etiqueta },
                            onSelect = { tipoProductoSel = it },
                            placeholder = "Selecciona tipo de producto"
                        )

                        CampoTextField(
                            label = "Riqueza NPK",
                            value = riquezaNPK,
                            onValueChange = { riquezaNPK = it },
                            placeholder = "p.ej. 15-15-15"
                        )

                        CampoTextField(
                            label = "Dosis (kg/ha)",
                            value = dosis,
                            onValueChange = { dosis = it },
                            keyboardType = KeyboardType.Decimal
                        )

                        CampoDropdown(
                            label = "Tipo de fertilización",
                            selectedItem = tipoFertilizacionSel,
                            items = TIPOS_FERTILIZACION,
                            itemLabel = { it.etiqueta },
                            onSelect = { tipoFertilizacionSel = it },
                            placeholder = "Selecciona tipo de fertilización"
                        )

                        CampoTextField(
                            label = "Observaciones (opcional)",
                            value = observaciones,
                            onValueChange = { observaciones = it },
                            minLines = 2
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                CampoPrimaryButton(
                    text = "Guardar fertilización",
                    onClick = {
                        scope.launch {
                            guardando = true
                            mensajeError = null
                            val resultado = viewModel.guardarFertilizacion(
                                actividadId = actividadId,
                                fertilizacion = Fertilizacion(
                                    id                = 0,
                                    actividadId       = actividadId,
                                    cultivoId         = null, // sin vínculo Cultivo todavía
                                    aplica            = aplica,
                                    fechaInicio       = if (aplica) fechaHoy else null,
                                    fechaFin          = null,
                                    tipoProducto      = if (aplica) tipoProductoSel?.codigo else null,
                                    numeroAlbaran     = if (aplica) numeroAlbaran.ifBlank { null } else null,
                                    riquezaNPK        = if (aplica) riquezaNPK.ifBlank { null } else null,
                                    dosis             = if (aplica) dosis.toDoubleOrNull() else null,
                                    tipoFertilizacion = if (aplica) tipoFertilizacionSel?.codigo else null,
                                    observaciones     = if (aplica) observaciones.ifBlank { null } else null
                                )
                            )
                            guardando = false
                            if (resultado is Result.Error) {
                                mensajeError = resultado.message
                            } else {
                                onVolver()
                            }
                        }
                    }
                )

                if (guardando) {
                    CircularProgressIndicator(
                        color = NaranjaPrimario,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                mensajeError?.let { error ->
                    Text(
                        text = error,
                        color = RojoEliminar,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
