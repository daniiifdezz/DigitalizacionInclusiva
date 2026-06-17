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
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.SemillaTratada
import org.dferna14.project.ui.components.CampoAvisoInfo
import org.dferna14.project.ui.components.CampoCard
import org.dferna14.project.ui.components.CampoDropdown
import org.dferna14.project.ui.components.CampoField
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.components.CampoToggle
import org.dferna14.project.ui.components.SectionHeader
import org.dferna14.project.ui.components.formatearFecha
import org.dferna14.project.ui.theme.BordeSuave
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.viewmodel.ActividadDetalleVm
import org.dferna14.project.ui.viewmodel.ProductoVm
import org.dferna14.project.ui.viewmodel.SemillaVm
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun SemillasTratadasSc(
    actividadId: Int,
    onVolver: () -> Unit,
    viewModel: SemillaVm = koinViewModel(),
    actividadDetalleVm: ActividadDetalleVm = koinViewModel(),
    productoVm: ProductoVm = koinViewModel()
) {
    val scope             = rememberCoroutineScope()
    val semillaState      by viewModel.semilla.collectAsState()
    val productosState    by productoVm.productos.collectAsState()
    val actividadState    by actividadDetalleVm.actividadActual.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actividadId) {
        actividadDetalleVm.cargarActividad(actividadId)
        viewModel.cargarSemilla(actividadId)
    }

    val parcelaId = (actividadState as? Result.Success)?.data?.parcelaId ?: 0
    val productos = (productosState as? Result.Success)?.data.orEmpty()
    val fechaHoy  = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }

    // La semilla existente (puede ser null si aún no hay registro).
    val semillaExistente = (semillaState as? Result.Success)?.data

    // Estado del formulario, re-inicializado cuando llega una semilla del backend.
    var aplica by remember(semillaExistente) { mutableStateOf(semillaExistente?.aplica ?: false) }
    var superficieHa by remember(semillaExistente) {
        mutableStateOf(semillaExistente?.superficieHa?.toString() ?: "")
    }
    var superficieDeSigpac by remember(semillaExistente) { mutableStateOf(false) }
    var cantidadSemillaKg by remember(semillaExistente) {
        mutableStateOf(semillaExistente?.cantidadSemillaKg?.toString() ?: "")
    }
    var variedadSemilla by remember(semillaExistente) {
        mutableStateOf(semillaExistente?.variedadSemilla ?: "")
    }
    var productoSeleccionado by remember(semillaExistente, productos) {
        mutableStateOf(productos.find { it.id == semillaExistente?.productoId })
    }

    // Pre-rellena la superficie SIGPAC al conocer la parcela, solo si el campo
    // está vacío (no había semilla guardada previa con su propia superficie).
    LaunchedEffect(parcelaId) {
        if (parcelaId > 0 && superficieHa.isBlank()) {
            val sup = viewModel.getSuperficieParcela(parcelaId)
            if (sup != null && sup > 0.0) {
                superficieHa       = sup.toString()
                superficieDeSigpac = true
            }
        }
    }

    fun guardar() {
        scope.launch {
            val resultado = viewModel.crearSemillaTratada(
                SemillaTratada(
                    id                = semillaExistente?.id ?: 0,
                    actividadId       = actividadId,
                    parcelaId         = parcelaId,
                    aplica            = aplica,
                    fechaSiembra      = if (aplica) fechaHoy else null,
                    superficieHa      = superficieHa.toDoubleOrNull(),
                    cantidadSemillaKg = cantidadSemillaKg.toDoubleOrNull(),
                    productoId        = if (aplica) productoSeleccionado?.id else null,
                    variedadSemilla   = if (aplica) variedadSemilla.ifBlank { null } else null,
                    cultivoId         = null
                )
            )
            when (resultado) {
                is Result.Success -> snackbarHostState.showSnackbar("Semilla tratada guardada correctamente")
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
            NavBarFormulario(
                titulo        = "Semilla tratada",
                onVolver      = onVolver,
                accionDerecha = {
                    val habilitado = actividadState is Result.Success && parcelaId > 0
                    TextButton(onClick = { guardar() }, enabled = habilitado) {
                        Text(
                            text     = "Guardar",
                            color    = if (habilitado) OlivaPrimario else BordeSuave,
                            fontSize = 13.sp
                        )
                    }
                }
            )
            HorizontalDivider(color = BordeSuave, thickness = 0.5.dp)

            when (val state = semillaState) {
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
                            text  = "No se pudo cargar la semilla tratada",
                            style = MaterialTheme.typography.bodyLarge,
                            color = RojoEliminar
                        )
                        Spacer(Modifier.height(12.dp))
                        CampoPrimaryButton(
                            text     = "Reintentar",
                            onClick  = { viewModel.cargarSemilla(actividadId) },
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
                is Result.Success -> {
                    if (actividadState !is Result.Success || parcelaId <= 0) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = OlivaPrimario)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                                .padding(top = 12.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (semillaExistente != null) {
                                CampoAvisoInfo(
                                    mensaje = "Ya tienes datos de semilla guardados. Puedes modificarlos y guardar de nuevo."
                                )
                            }

                            CampoToggle(
                                label           = "¿Aplica semilla tratada?",
                                checked         = aplica,
                                onCheckedChange = { aplica = it }
                            )

                            CampoAvisoInfo(
                                mensaje = "La fecha de siembra se registra automáticamente con el día de hoy"
                            )

                            AnimatedVisibility(visible = aplica) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    SectionHeader("Datos de siembra")

                                    CampoCard {
                                        CampoField(
                                            label = "Fecha de siembra",
                                            value = formatearFecha(fechaHoy)
                                        )

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
                                            placeholder  = "Selecciona producto"
                                        )

                                        CampoTextField(
                                            label         = "Variedad de semilla",
                                            value         = variedadSemilla,
                                            onValueChange = { variedadSemilla = it },
                                            placeholder   = "p.ej. Trigo R01"
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
}
