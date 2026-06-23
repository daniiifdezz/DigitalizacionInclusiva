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
import org.dferna14.project.domain.model.SemillaTratada
import org.dferna14.project.ui.components.*
import org.dferna14.project.ui.theme.BordeSuave
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.viewmodel.ActividadListaVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.dferna14.project.ui.viewmodel.ProductoVm
import org.dferna14.project.ui.viewmodel.SemillaVm
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun NuevaSemillaSc(
    onVolver: () -> Unit,
    actividadVm: ActividadListaVm = koinViewModel(),
    semillaVm: SemillaVm          = koinViewModel(),
    parcelaVm: ParcelaVm          = koinViewModel(),
    productoVm: ProductoVm        = koinViewModel()
) {
    val scope             = rememberCoroutineScope()
    val parcelasState     by parcelaVm.parcelas.collectAsState()
    val productosState    by productoVm.productos.collectAsState()
    val mensajeErrorAct   by actividadVm.mensajeError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val fechaHoy = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }

    var parcelaSeleccionada  by remember { mutableStateOf<Parcela?>(null) }
    var superficieHa         by remember { mutableStateOf("") }
    var superficieDeSigpac   by remember { mutableStateOf(false) }
    var cantidadSemillaKg    by remember { mutableStateOf("") }
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var variedadSemilla      by remember { mutableStateOf("") }
    var guardando            by remember { mutableStateOf(false) }

    LaunchedEffect(parcelaSeleccionada) {
        val parcela = parcelaSeleccionada
        if (parcela != null && superficieHa.isBlank()) {
            val sup = actividadVm.getSuperficieParcela(parcela.id)
            if (sup != null && sup > 0.0) {
                superficieHa       = sup.toString()
                superficieDeSigpac = true
            }
        }
    }

    LaunchedEffect(mensajeErrorAct) {
        mensajeErrorAct?.let {
            guardando = false
            snackbarHostState.showSnackbar(it)
            actividadVm.limpiarMensajeError()
        }
    }

    val parcelas  = (parcelasState as? Result.Success)?.data.orEmpty()
    val productos = (productosState as? Result.Success)?.data.orEmpty()

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
            val resultado = semillaVm.crearSemillaTratada(
                SemillaTratada(
                    actividadId       = actividadId,
                    parcelaId         = parcela.id,
                    aplica            = true,
                    fechaSiembra      = fechaHoy,
                    superficieHa      = superficieHa.replace(",", ".").toDoubleOrNull(),
                    cantidadSemillaKg = cantidadSemillaKg.replace(",", ".").toDoubleOrNull(),
                    productoId        = productoSeleccionado?.id,
                    variedadSemilla   = variedadSemilla.ifBlank { null },
                    cultivoId         = null
                )
            )
            guardando = false
            when (resultado) {
                is Result.Success -> onVolver()
                is Result.Error   -> snackbarHostState.showSnackbar(resultado.message)
                else              -> {}
            }
        }
    }

    Scaffold(
        containerColor = CremaPrincipal,
        snackbarHost   = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavBarFormulario(titulo = "Siembra de semilla tratada", onVolver = onVolver)
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
                    label = "Fecha de siembra",
                    value = formatearFecha(fechaHoy)
                )

                CampoDropdown(
                    label        = "Parcela *",
                    selectedItem = parcelaSeleccionada,
                    items        = parcelas,
                    itemLabel    = { it.alias ?: "Parcela ${it.orden ?: it.id}" },
                    onSelect     = {
                        parcelaSeleccionada = it
                        superficieHa        = ""
                        superficieDeSigpac  = false
                    },
                    placeholder  = "Selecciona una parcela"
                )

                SectionHeader("Datos de siembra")

                CampoCard {
                    CampoTextField(
                        label         = "Superficie (ha)",
                        value         = superficieHa,
                        onValueChange = {
                            superficieHa       = it
                            superficieDeSigpac = false
                        },
                        keyboardType  = KeyboardType.Decimal
                    )

                    if (superficieDeSigpac && superficieHa.isNotBlank()) {
                        CampoAvisoInfo(
                            mensaje = "Pre-rellenado con la superficie SIGPAC de la parcela. Modifícalo si no aplicas en toda la parcela."
                        )
                    }

                    CampoTextField(
                        label         = "Cantidad de semilla (kg)",
                        value         = cantidadSemillaKg,
                        onValueChange = { cantidadSemillaKg = it },
                        keyboardType  = KeyboardType.Decimal
                    )

                    CampoDropdown(
                        label        = "Producto utilizado",
                        selectedItem = productoSeleccionado,
                        items        = productos,
                        itemLabel    = { it.nombreComercial.ifBlank { "Producto ${it.id}" } },
                        onSelect     = { productoSeleccionado = it },
                        placeholder  = "Selecciona producto (opcional)"
                    )

                    CampoTextField(
                        label         = "Variedad de semilla",
                        value         = variedadSemilla,
                        onValueChange = { variedadSemilla = it },
                        placeholder   = "p. ej. Trigo R01"
                    )
                }

                Spacer(Modifier.height(4.dp))

                if (parcelaSeleccionada == null) {
                    CampoAvisoInfo(mensaje = "Selecciona una parcela para poder guardar.")
                }

                CampoPrimaryButton(
                    text    = if (guardando) "Guardando…" else "Guardar siembra",
                    onClick = { guardar() },
                    enabled = parcelaSeleccionada != null && !guardando
                )
            }
        }
    }
}
