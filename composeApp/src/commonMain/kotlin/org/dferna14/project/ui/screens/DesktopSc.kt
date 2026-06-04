package org.dferna14.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.EstadoActividad
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ActividadListaVm
import org.dferna14.project.ui.viewmodel.ProductoVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopMainSc(
    onVerListado: () -> Unit,
    onVerPendientes: () -> Unit,
    onVerParcelas: () -> Unit,
    onVerProductos: () -> Unit,
    onVerValidar: (Int) -> Unit,
    onVerCuadernoPdf: () -> Unit,
    onVerConfiguracion: () -> Unit,
    actividadListaVm: ActividadListaVm = koinViewModel(),
    parcelaVm: ParcelaVm = koinViewModel(),
    productoVm: ProductoVm = koinViewModel()
) {
    var selectedItem by remember { mutableStateOf(0) }

    val actividadesState by actividadListaVm.actividades.collectAsState()

    val parcelasState by parcelaVm.parcelas.collectAsState()
    val productosState by productoVm.productos.collectAsState()

    LaunchedEffect(Unit) {
        actividadListaVm.cargarActividades()
        parcelaVm.cargarParcelas()
        productoVm.cargarProductos()
    }

    // Cálculo de las 3 métricas que muestra el dashboard.
    // "—" mientras cargan, "?" si la carga falla.
    val hoy = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }
    val mesActual = remember(hoy) { hoy.substring(0, 7) } // "YYYY-MM"

    val pendientesTexto = estadisticaActividades(actividadesState) { lista ->
        lista.count { it.estado == EstadoActividad.PENDIENTE_VALIDAR }
    }
    val validadasHoyTexto = estadisticaActividades(actividadesState) { lista ->
        lista.count { it.estado == EstadoActividad.VALIDADA && it.esDeHoy(hoy) }
    }
    val totalMesTexto = estadisticaActividades(actividadesState) { lista ->
        lista.count { it.fechaInicio.startsWith(mesActual) }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Menu lateral
        Column(
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Text(
                text = "MENU",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            MenuItem(
                text = "Todas las actividades",
                selected = selectedItem == 0,
                onClick = {
                    selectedItem = 0
                    onVerListado()
                }
            )
            MenuItem(
                text = "Pendientes de validar",
                selected = selectedItem == 1,
                onClick = {
                    selectedItem = 1
                    onVerPendientes()
                }
            )
            MenuItem(
                text = "Parcelas",
                selected = selectedItem == 2,
                onClick = {
                    selectedItem = 2
                    onVerParcelas()
                }
            )
            MenuItem(
                text = "Productos",
                selected = selectedItem == 3,
                onClick = {
                    selectedItem = 3
                    onVerProductos()
                }
            )
            MenuItem(
                text = "Cuaderno PDF",
                selected = selectedItem == 4,
                onClick = {
                    selectedItem = 4
                    onVerCuadernoPdf()
                }
            )
            MenuItem(
                text = "Configuración",
                selected = selectedItem == 5,
                onClick = {
                    selectedItem = 5
                    onVerConfiguracion()
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "TFG - Daniel Fernandez",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        // Contenido del main
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Cuaderno de Campo - Desktop",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Pendientes",
                    value = pendientesTexto,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Validadas hoy",
                    value = validadasHoyTexto,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total mes",
                    value = totalMesTexto,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Acciones rapidas",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onVerPendientes,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ver pendientes")
                }

                // Revisar para un futuro
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Informes")
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Devuelve la representación textual de una estadística derivada de la lista
 * de actividades: "—" mientras carga, "?" si hubo error, el número en caso
 * de éxito.
 */
private fun estadisticaActividades(
    estado: Result<List<Actividad>>,
    contador: (List<Actividad>) -> Int
): String = when (estado) {
    is Result.Loading -> "—"
    is Result.Error   -> "?"
    is Result.Success -> contador(estado.data).toString()
}

/**
 * Una actividad es "de hoy" si su fechaFin o, en su defecto, su fechaInicio
 * coincide con la fecha actual (formato YYYY-MM-DD).
 */
private fun Actividad.esDeHoy(hoy: String): Boolean {
    val fechaReferencia = fechaFin?.takeIf { it.isNotBlank() } ?: fechaInicio
    return fechaReferencia == hoy
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}