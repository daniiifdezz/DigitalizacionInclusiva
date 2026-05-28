package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Producto
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoDropdown
import org.dferna14.project.ui.components.CampoField
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.components.formatearFecha
import org.dferna14.project.ui.theme.BordeSuave
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.viewmodel.ActividadListaVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.dferna14.project.ui.viewmodel.ProductoVm
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/**
 * Registro de una nueva actividad agrícola. Diseño accesible:
 * pocos campos, etiquetas claras, botón de guardar grande.
 */
@Composable
fun NuevaActividadSc(
    onVolver: () -> Unit,
    viewModel: ActividadListaVm = koinViewModel(),
    parcelaVm: ParcelaVm = koinViewModel(),
    productoVm: ProductoVm = koinViewModel()
) {
    val parcelasState by parcelaVm.parcelas.collectAsState()
    val productosState by productoVm.productos.collectAsState()
    val operacionExitosa by viewModel.operacionExitosa.collectAsState()

    var parcelaSeleccionada by remember { mutableStateOf<Parcela?>(null) }
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var dosis by remember { mutableStateOf("") }
    var superficieTratada by remember { mutableStateOf("") }
    var problemaFitosanitario by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    val fechaHoy = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }

    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            viewModel.resetOperacionExitosa()
            onVolver()
        }
    }

    val parcelas = (parcelasState as? Result.Success)?.data.orEmpty()
    val productos = (productosState as? Result.Success)?.data.orEmpty()

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavBarFormulario(titulo = "Nueva actividad", onVolver = onVolver)
            HorizontalDivider(color = BordeSuave, thickness = 0.5.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CampoField(
                    label = "Fecha de registro",
                    value = formatearFecha(fechaHoy)
                )

                CampoDropdown(
                    label = "Parcela *",
                    selectedItem = parcelaSeleccionada,
                    items = parcelas,
                    itemLabel = { it.alias ?: "Parcela ${it.orden ?: it.id}" },
                    onSelect = { parcelaSeleccionada = it },
                    placeholder = "Selecciona una parcela"
                )

                CampoTextField(
                    label = "Superficie tratada (ha)",
                    value = superficieTratada,
                    onValueChange = { superficieTratada = it },
                    keyboardType = KeyboardType.Decimal
                )

                CampoTextField(
                    label = "Problema fitosanitario",
                    value = problemaFitosanitario,
                    onValueChange = { problemaFitosanitario = it },
                    minLines = 2
                )

                CampoDropdown(
                    label = "Producto usado",
                    selectedItem = productoSeleccionado,
                    items = productos,
                    itemLabel = { it.nombreComercial.ifBlank { "Producto ${it.id}" } },
                    onSelect = { productoSeleccionado = it },
                    placeholder = "Selecciona producto"
                )

                if (productoSeleccionado != null) {
                    CampoTextField(
                        label = "Dosis (kg/ha)",
                        value = dosis,
                        onValueChange = { dosis = it },
                        keyboardType = KeyboardType.Decimal
                    )
                }

                CampoTextField(
                    label = "Observaciones (opcional)",
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    minLines = 3
                )

                Spacer(Modifier.height(4.dp))

                CampoPrimaryButton(
                    text = "Guardar actividad",
                    onClick = {
                        parcelaSeleccionada?.let { parcela ->
                            viewModel.crearActividad(
                                Actividad(
                                    parcelaId             = parcela.id,
                                    fechaInicio           = fechaHoy,
                                    superficieTratada     = superficieTratada.toDoubleOrNull(),
                                    problemaFitosanitario = problemaFitosanitario.ifBlank { null },
                                    observaciones         = observaciones.ifBlank { null }
                                )
                            )
                        }
                    },
                    enabled = parcelaSeleccionada != null
                )
            }
        }
    }
}

/**
 * Barra de navegación reutilizada por todos los formularios móviles:
 * botón "Volver" naranja a la izquierda, título centrado, hueco simétrico a la derecha.
 */
@Composable
internal fun NavBarFormulario(
    titulo: String,
    onVolver: () -> Unit,
    accionDerecha: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onVolver) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Volver",
                tint = NaranjaPrimario,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text("Volver", color = NaranjaPrimario, fontSize = 13.sp)
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = TextoPrimario
        )
        Spacer(Modifier.weight(1f))
        if (accionDerecha != null) {
            accionDerecha()
        } else {
            Spacer(Modifier.width(72.dp))
        }
    }
}
