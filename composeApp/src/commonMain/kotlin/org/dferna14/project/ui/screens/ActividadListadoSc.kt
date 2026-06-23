package org.dferna14.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.EstadoActividad
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.BadgeTipo
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.EstadoBadge
import org.dferna14.project.ui.components.formatearFecha
import org.dferna14.project.ui.components.desktop.*
import org.dferna14.project.ui.theme.*
import org.dferna14.project.ui.viewmodel.ActividadListaVm
import org.dferna14.project.ui.viewmodel.AjustesVm
import org.koin.compose.viewmodel.koinViewModel

private enum class FiltroActividad(val etiqueta: String) {
    TODAS("Todas"),
    HOY("Hoy"),
    PENDIENTES("Pendientes"),
    VALIDADAS("Validadas")
}

private fun colorEstado(estado: EstadoActividad): androidx.compose.ui.graphics.Color = when (estado) {
    EstadoActividad.BORRADOR          -> GrisBorrador
    EstadoActividad.PENDIENTE_VALIDAR -> AzulPendiente
    EstadoActividad.VALIDADA          -> VerdeValidada
}

private val COLS_ACTIVIDADES = listOf(
    DesktopTableColumn("Parcela",  weight = 1.5f),
    DesktopTableColumn("Problema", weight = 2.5f),
    DesktopTableColumn("Fecha",    weight = 1.0f),
    DesktopTableColumn("Tipo",     weight = 1.2f),
    DesktopTableColumn("Estado",   weight = 1.5f),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadListadoSc(
    onNuevaActividad: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    isDesktop: Boolean = false,
    onVolver: (() -> Unit)? = null,
    onVerInicio: (() -> Unit)? = null,
    onVerParcelas: (() -> Unit)? = null,
    onVerProductos: (() -> Unit)? = null,
    onVerAjustes: (() -> Unit)? = null,
    onVerConfiguracion: (() -> Unit)? = null,
    viewModel: ActividadListaVm = koinViewModel(),
    ajustesVm: AjustesVm = koinViewModel()
) {
    val actividadesState by viewModel.actividades.collectAsState()
    var filtroActivo by remember { mutableStateOf(FiltroActividad.TODAS) }
    val fechaHoy = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }

    LaunchedEffect(Unit) {
        viewModel.cargarActividades()
    }

    if (isDesktop) {
        ActividadListadoDesktop(
            actividadesState   = actividadesState,
            nombreUsuario      = ajustesVm.nombreMostrado,
            rolUsuario         = ajustesVm.rolUsuario,
            onNuevaActividad   = onNuevaActividad,
            onVerDetalle       = onVerDetalle,
            onVerInicio        = onVerInicio ?: {},
            onVerParcelas      = onVerParcelas ?: {},
            onVerProductos     = onVerProductos ?: {},
            onVerAjustes       = onVerAjustes ?: {},
            onVerConfiguracion = onVerConfiguracion ?: {},
            onReintentar       = { viewModel.cargarActividades() }
        )
    } else {
        ActividadListadoMovil(
            actividadesState = actividadesState,
            filtroActivo     = filtroActivo,
            onFiltroChange   = { filtroActivo = it },
            fechaHoy         = fechaHoy,
            onNuevaActividad = onNuevaActividad,
            onVerDetalle     = onVerDetalle,
            onReintentar     = { viewModel.cargarActividades() }
        )
    }
}

//mobil
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActividadListadoMovil(
    actividadesState: Result<List<Actividad>>,
    filtroActivo: FiltroActividad,
    onFiltroChange: (FiltroActividad) -> Unit,
    fechaHoy: String,
    onNuevaActividad: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onReintentar: () -> Unit
) {
    Scaffold(
        containerColor = CremaPrincipal,
        topBar = {
            TopAppBar(
                title  = { Text("Mis actividades", style = MaterialTheme.typography.titleLarge, color = TextoPrimario) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SuperficieSepia)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick        = onNuevaActividad,
                text           = { Text("Nueva actividad") },
                icon           = { Icon(Icons.Outlined.Add, contentDescription = null) },
                containerColor = TerracotaAccent,
                contentColor   = BlancoPuro
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FiltroActividad.entries) { filtro ->
                    FiltroChip(
                        filtro  = filtro,
                        activo  = filtro == filtroActivo,
                        onClick = { onFiltroChange(filtro) }
                    )
                }
            }

            when (val estado = actividadesState) {
                is Result.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OlivaPrimario)
                    }
                }
                is Result.Error -> {
                    Column(
                        modifier              = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment   = Alignment.CenterHorizontally,
                        verticalArrangement   = Arrangement.Center
                    ) {
                        Text(
                            text  = "No se pudieron cargar las actividades",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoSecundario
                        )
                        Spacer(Modifier.height(12.dp))
                        CampoPrimaryButton(
                            text     = "Reintentar",
                            onClick  = onReintentar,
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
                is Result.Success -> {
                    val todas = estado.data
                    val filtradas = when (filtroActivo) {
                        FiltroActividad.TODAS      -> todas
                        FiltroActividad.HOY        -> todas.filter { it.fechaInicio == fechaHoy }
                        FiltroActividad.PENDIENTES -> todas.filter { it.estado == EstadoActividad.PENDIENTE_VALIDAR }
                        FiltroActividad.VALIDADAS  -> todas.filter { it.estado == EstadoActividad.VALIDADA }
                    }

                    if (todas.isEmpty()) {
                        Column(
                            modifier            = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Assignment,
                                contentDescription = null,
                                modifier           = Modifier.size(64.dp),
                                tint               = OlivaPrimario
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text  = "No tienes actividades registradas",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextoPrimario
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text  = "Pulsa + para crear tu primera actividad",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoTerciario
                            )
                        }
                    } else if (filtradas.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text  = "Sin actividades con este filtro",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoTerciario
                            )
                        }
                    } else {
                        val agrupadasPorFecha = filtradas.groupBy { it.fechaInicio }
                        LazyColumn(
                            modifier       = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 88.dp)
                        ) {
                            agrupadasPorFecha.forEach { (fecha, grupo) ->
                                item(key = "header_$fecha") {
                                    Text(
                                        text     = formatearFecha(fecha).uppercase(),
                                        style    = MaterialTheme.extraTypography.eyebrow,
                                        color    = TextoTerciario,
                                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                                    )
                                }
                                items(grupo, key = { it.id }) { actividad ->
                                    ActividadCard(
                                        actividad = actividad,
                                        onClick   = { onVerDetalle(actividad.id) }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroChip(
    filtro: FiltroActividad,
    activo: Boolean,
    onClick: () -> Unit
) {
    val bg     = if (activo) OlivaPrimario  else SuperficieSepia
    val text   = if (activo) CremaPrincipal else TextoSecundario
    val border = if (activo) OlivaOscuro    else BordeNormal

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = filtro.etiqueta,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color      = text
        )
    }
}

@Composable
private fun ActividadCard(
    actividad: Actividad,
    onClick: () -> Unit
) {
    val stateColor    = colorEstado(actividad.estado)
    val nombreParcela = actividad.parcelaAlias ?: "Parcela ${actividad.parcelaId}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
            .background(SuperficieSepia)
            .border(0.5.dp, BordeNormal, RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(stateColor)
        )
        Row(
            modifier              = Modifier.padding(horizontal = 13.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.Top
        ) {
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CremaPrincipal)
                    .border(1.dp, BordeNormal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Assignment,
                    contentDescription = null,
                    tint               = stateColor,
                    modifier           = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = nombreParcela,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color      = TextoPrimario
                )
                Row(
                    modifier              = Modifier.padding(top = 3.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier           = Modifier.size(11.dp),
                        tint               = TextoTerciario
                    )
                    Text(
                        text  = formatearFecha(actividad.fechaInicio),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoTerciario
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EstadoBadge(actividad.estado)
                    BadgeTipo(actividad.tipoActividad)
                }
                val problema = actividad.problemaFitosanitario
                if (!problema.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = problema,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = TextoSecundario,
                        maxLines = 2
                    )
                }
            }
            Icon(
                imageVector        = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint               = TextoTerciario,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

//desktop
@Composable
private fun ActividadListadoDesktop(
    actividadesState: Result<List<Actividad>>,
    nombreUsuario: String,
    rolUsuario: String,
    onNuevaActividad: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onVerInicio: () -> Unit,
    onVerParcelas: () -> Unit,
    onVerProductos: () -> Unit,
    onVerAjustes: () -> Unit,
    onVerConfiguracion: () -> Unit,
    onReintentar: () -> Unit
) {
    DesktopWrapper(
        activeIndex   = 1,
        onNavigate    = { idx ->
            when (idx) {
                0 -> onVerInicio()
                2 -> onVerParcelas()
                3 -> onVerProductos()
                4 -> onVerAjustes()
                5 -> onVerConfiguracion()
            }
        },
        nombreUsuario = nombreUsuario,
        rolUsuario    = rolUsuario
    ) {
        DesktopTopBar(
            title   = "Actividades",
            actions = listOf(
                DesktopTopBarAction(
                    label   = "Nueva actividad",
                    icon    = Icons.Outlined.Add,
                    primary = true,
                    onClick = onNuevaActividad
                )
            )
        )
        when (val estado = actividadesState) {
            is Result.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OlivaPrimario)
                }
            }
            is Result.Error -> {
                Column(
                    modifier            = Modifier.fillMaxSize().padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text  = "No se pudieron cargar las actividades",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextoSecundario
                    )
                    Spacer(Modifier.height(16.dp))
                    CampoPrimaryButton(
                        text     = "Reintentar",
                        onClick  = onReintentar,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }
            is Result.Success -> {
                val actividades = estado.data
                if (actividades.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector        = Icons.Outlined.Assignment,
                                contentDescription = null,
                                modifier           = Modifier.size(48.dp),
                                tint               = OlivaPrimario
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text  = "No hay actividades registradas",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextoPrimario
                            )
                        }
                    }
                } else {
                    DesktopTableHeader(columns = COLS_ACTIVIDADES)
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        itemsIndexed(actividades, key = { _, a -> a.id }) { index, actividad ->
                            DesktopTableRow(
                                columns = COLS_ACTIVIDADES,
                                last    = index == actividades.lastIndex,
                                onClick = { onVerDetalle(actividad.id) },
                                cells   = listOf(
                                    {
                                        Text(
                                            text  = actividad.parcelaAlias ?: "Parcela ${actividad.parcelaId}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextoPrimario
                                        )
                                    },
                                    {
                                        Text(
                                            text     = actividad.problemaFitosanitario ?: "—",
                                            style    = MaterialTheme.typography.bodySmall,
                                            color    = TextoSecundario,
                                            maxLines = 2
                                        )
                                    },
                                    {
                                        Text(
                                            text  = formatearFecha(actividad.fechaInicio),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextoTerciario
                                        )
                                    },
                                    {
                                        BadgeTipo(actividad.tipoActividad)
                                    },
                                    {
                                        EstadoBadge(actividad.estado)
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
