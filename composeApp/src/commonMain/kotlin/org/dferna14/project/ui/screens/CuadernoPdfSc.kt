package org.dferna14.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.dferna14.project.ui.components.CampoAvisoInfo
import org.dferna14.project.ui.components.CampoCard
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.theme.RojoFondoEliminar
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.viewmodel.CuadernoVm
import org.dferna14.project.ui.viewmodel.EstadoDescargaPdf
import org.dferna14.project.util.guardarPdfEnDisco
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/**
 * Pantalla Desktop (técnico): genera y descarga el PDF del Cuaderno de Campo oficial
 * (RD 1311/2012) para el periodo elegido. Por defecto el año natural actual.
 *
 * El selector de fechas es de momento un CampoTextField con formato YYYY-MM-DD;
 * más adelante puede sustituirse por un date picker visual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuadernoPdfSc(
    onVolver: () -> Unit,
    cuadernoVm: CuadernoVm = koinViewModel()
) {
    val anioActual = Clock.System.todayIn(TimeZone.currentSystemDefault()).year

    var fechaDesde by remember { mutableStateOf("$anioActual-01-01") }
    var fechaHasta by remember { mutableStateOf("$anioActual-12-31") }
    var mensajeFinal by remember { mutableStateOf<String?>(null) }

    val estado by cuadernoVm.estadoDescarga.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuaderno PDF", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver al menú principal",
                            tint = NaranjaPrimario,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Menú principal", color = NaranjaPrimario)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Cuaderno de Campo Digital",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextoPrimario
            )

            Text(
                "Genera el cuaderno oficial conforme al RD 1311/2012 con todas las " +
                    "actividades validadas del periodo seleccionado.",
                fontSize = 14.sp,
                color = TextoSecundario
            )

            CampoCard {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Periodo del cuaderno",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextoPrimario
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CampoTextField(
                            label = "Desde (YYYY-MM-DD)",
                            value = fechaDesde,
                            onValueChange = { fechaDesde = it },
                            modifier = Modifier.weight(1f)
                        )
                        CampoTextField(
                            label = "Hasta (YYYY-MM-DD)",
                            value = fechaHasta,
                            onValueChange = { fechaHasta = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    CampoAvisoInfo(
                        mensaje = "Solo se incluyen actividades en estado VALIDADA dentro del periodo seleccionado."
                    )
                }
            }

            // Botón principal
            CampoPrimaryButton(
                text = when (estado) {
                    is EstadoDescargaPdf.Descargando -> "Generando PDF..."
                    else -> "Generar Cuaderno PDF"
                },
                onClick = {
                    mensajeFinal = null
                    cuadernoVm.descargarPdf(fechaDesde, fechaHasta) { bytes ->
                        val nombreSugerido = "cuaderno_campo_${fechaDesde}_a_${fechaHasta}.pdf"
                        val rutaGuardada = guardarPdfEnDisco(bytes, nombreSugerido)
                        mensajeFinal = if (rutaGuardada != null) {
                            "PDF guardado en: $rutaGuardada"
                        } else {
                            "Guardado cancelado por el usuario"
                        }
                    }
                },
                enabled = estado !is EstadoDescargaPdf.Descargando,
                modifier = Modifier.fillMaxWidth()
            )

            // Estado dinámico
            when (val e = estado) {
                is EstadoDescargaPdf.Descargando -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = NaranjaPrimario
                        )
                        Text(
                            "Generando el PDF... esto puede tardar unos segundos.",
                            color = TextoSecundario,
                            fontSize = 13.sp
                        )
                    }
                }
                is EstadoDescargaPdf.Error -> AvisoError(mensaje = e.mensaje)
                is EstadoDescargaPdf.Exito -> {
                    mensajeFinal?.let { CampoAvisoInfo(mensaje = it) }
                }
                EstadoDescargaPdf.Idle -> Unit
            }
        }
    }
}

/**
 * Aviso de error en rojo. Equivalente a CampoAvisoInfo pero con la paleta de error,
 * ya que CampoAvisoInfo no expone parámetro de color.
 */
@Composable
private fun AvisoError(mensaje: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RojoFondoEliminar)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = RojoEliminar,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Error: $mensaje",
            color = RojoEliminar,
            fontSize = 12.sp
        )
    }
}
