package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
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
import org.dferna14.project.ui.components.CampoAvisoInfo
import org.dferna14.project.ui.components.CampoCard
import org.dferna14.project.ui.components.CampoDropdown
import org.dferna14.project.ui.components.CampoField
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoSecondaryButton
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.components.formatearFecha
import org.dferna14.project.ui.theme.BordeSuave
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.viewmodel.ActividadListaVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.dferna14.project.ui.viewmodel.ProductoVm
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/**
 * Registro de una nueva actividad agrícola. Permite asociar N productos con dosis;
 * todos se persisten al pulsar "Guardar actividad".
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
    var productosSeleccionados by remember {
        mutableStateOf<List<Pair<Producto, Double>>>(emptyList())
    }
    var superficieTratada by remember { mutableStateOf("") }
    // Marca si el valor actual de superficie viene del pre-relleno SIGPAC
    // (para mostrar el aviso). Se reinicia si el usuario edita el campo o
    // si se ha rellenado a mano antes de elegir parcela.
    var superficieDeSigpac by remember { mutableStateOf(false) }
    var problemaFitosanitario by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    // Pre-rellena la superficie con la SIGPAC al elegir parcela, solo si el
    // campo está vacío. El campo sigue siendo editable después.
    LaunchedEffect(parcelaSeleccionada) {
        val parcela = parcelaSeleccionada
        if (parcela != null && superficieTratada.isBlank()) {
            val sup = viewModel.getSuperficieParcela(parcela.id)
            if (sup != null && sup > 0.0) {
                superficieTratada = sup.toString()
                superficieDeSigpac = true
            }
        }
    }

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
                    onValueChange = {
                        superficieTratada = it
                        superficieDeSigpac = false
                    },
                    keyboardType = KeyboardType.Decimal
                )

                if (superficieDeSigpac && superficieTratada.isNotBlank()) {
                    CampoAvisoInfo(
                        mensaje = "Pre-rellenado con la superficie SIGPAC de la parcela. Modifícalo si no aplicas en toda la parcela."
                    )
                }

                CampoTextField(
                    label = "Problema fitosanitario",
                    value = problemaFitosanitario,
                    onValueChange = { problemaFitosanitario = it },
                    minLines = 2
                )

                // Lista de productos ya añadidos. Cada item se puede eliminar
                // antes de guardar la actividad.
                if (productosSeleccionados.isNotEmpty()) {
                    Text(
                        text = "Productos añadidos",
                        fontSize = 13.sp,
                        color = TextoTerciario
                    )
                    productosSeleccionados.forEachIndexed { index, (prod, dosisProd) ->
                        CampoCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.nombreComercial.ifBlank { "Producto ${prod.id}" },
                                        color = TextoPrimario,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "$dosisProd kg/ha",
                                        color = TextoTerciario,
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(onClick = {
                                    productosSeleccionados = productosSeleccionados
                                        .filterIndexed { i, _ -> i != index }
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Quitar producto",
                                        tint = TextoTerciario
                                    )
                                }
                            }
                        }
                    }
                }

                CampoDropdown(
                    label = "Añadir producto",
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

                    CampoSecondaryButton(
                        text = "Añadir a la actividad",
                        onClick = {
                            val prod = productoSeleccionado
                            val dosisNum = dosis.toDoubleOrNull()
                            if (prod != null && dosisNum != null) {
                                productosSeleccionados = productosSeleccionados + (prod to dosisNum)
                                productoSeleccionado = null
                                dosis = ""
                            }
                        }
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
                            viewModel.crearActividadConProductos(
                                actividad = Actividad(
                                    parcelaId             = parcela.id,
                                    fechaInicio           = fechaHoy,
                                    superficieTratada     = superficieTratada.toDoubleOrNull(),
                                    problemaFitosanitario = problemaFitosanitario.ifBlank { null },
                                    observaciones         = observaciones.ifBlank { null }
                                ),
                                productos = productosSeleccionados.map { (p, d) -> p.id to d }
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
