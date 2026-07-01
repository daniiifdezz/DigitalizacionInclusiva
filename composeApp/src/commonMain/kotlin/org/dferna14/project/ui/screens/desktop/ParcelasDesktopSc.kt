package org.dferna14.project.ui.screens.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.domain.model.DatosAgronomicos
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.ReferenciaSigpac
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoAvisoInfo
import org.dferna14.project.ui.components.desktop.DesktopFormField
import org.dferna14.project.ui.components.desktop.DesktopSelectField
import org.dferna14.project.ui.components.desktop.DesktopTopBar
import org.dferna14.project.ui.components.desktop.DesktopTopBarAction
import org.dferna14.project.ui.components.desktop.DesktopWrapper
import org.dferna14.project.ui.theme.BordeClaro
import org.dferna14.project.ui.theme.BordeNormal
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.OlivaClaro
import org.dferna14.project.ui.theme.OlivaOscuro
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.OlivaTint
import org.dferna14.project.ui.theme.SuperficieSepia
import org.dferna14.project.ui.theme.TerracotaAccent
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.extraTypography
import org.dferna14.project.ui.viewmodel.AjustesVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.koin.compose.viewmodel.koinViewModel

//desplegable
private val ECOREGIMEN_OPCIONES = listOf("P1", "P2A", "P3", "P4", "P5", "P6", "P7")
private val AIRE_LIBRE_OPCIONES = mapOf(
    "AL"  to "Aire libre",
    "M"   to "Malla",
    "BP"  to "Bajo plástico",
    "INV" to "Invernadero",
)
private val ESPECIE_VARIEDAD_OPCIONES = listOf(
    "Trigo", "Cebada", "Centeno", "Avena", "Triticale",
    "Maíz", "Girasol", "Colza", "Veza", "Yero",
    "Garbanzo", "Lenteja", "Alfalfa", "Remolacha", "Patata",
    "Vid", "Olivo",
)

private val SISTEMA_ASESORAMIENTO_OPCIONES = listOf(
    "Asesoramiento individual",
    "Asesoramiento de cooperativa",
    "Servicio técnico de la administración",
    "Sistema integrado de gestión (ATRIA)",
    "No aplica",
)

private val USO_SIGPAC_OPCIONES = mapOf(
    "TA" to "Tierra arable",
    "TH" to "Huerta",
    "PR" to "Pasto arbustivo",
    "PS" to "Pastizal",
    "PA" to "Pasto con arbolado",
    "FY" to "Frutos secos",
    "OV" to "Olivar",
    "OF" to "Otros frutales",
    "VI" to "Viñedo",
    "CI" to "Cítricos",
    "IM" to "Improductivo agrícola",
    "FO" to "Forestal",
    "ED" to "Edificaciones",
    "AG" to "Agua",
    "CA" to "Caminos",
    "OC" to "Otros usos no agrarios",
)


private data class ParcelaFs(
    val alias                : String  = "",
    val sistemaAsesoramiento : String  = "",
    val zonaNitratos         : Boolean = false,
)

private data class SigpacFs(
    val id: Int = 0,
    val provincia: String = "",
    val terminoMunicipal: String = "",
    val codigoAgregado: String = "",
    val zona: String = "",
    val numeroPoligono: String = "",
    val numeroParcela: String = "",
    val numeroRecinto: String = "",
    val usoSigpac: String = "",
    val superficieHa: String = "",
)

private data class AgronomicaFs(
    val id: Int = 0,
    val especieVariedad: String = "",
    val ecoregimenPractica: String = "",
    val secanoRegadio: String = "",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val aireLibreProtegido: String = "",
)


@Composable
fun ParcelasDesktopSc(
    onVerInicio: () -> Unit,
    onVerActividades: () -> Unit,
    onVerProductos: () -> Unit,
    onVerAjustes: () -> Unit,
    onVerConfiguracion: () -> Unit,
    parcelaVm: ParcelaVm = koinViewModel(),
    ajustesVm: AjustesVm = koinViewModel(),
) {
    val parcelasResult       by parcelaVm.parcelas.collectAsState()
    val parcelaCompletaResult by parcelaVm.parcelaCompleta.collectAsState()

    var selectedId      by remember { mutableStateOf<Int?>(null) }
    var parcelaFs       by remember { mutableStateOf(ParcelaFs()) }
    var sigpacFs        by remember { mutableStateOf(SigpacFs()) }
    var agronomicaFs    by remember { mutableStateOf(AgronomicaFs()) }
    var sigpacCargado   by remember { mutableStateOf(false) }
    var parcelaAEliminar by remember { mutableStateOf<Parcela?>(null) }

    val parcelas = (parcelasResult as? Result.Success)?.data ?: emptyList()

    LaunchedEffect(Unit) {
        parcelaVm.cargarParcelas()
    }

    LaunchedEffect(parcelasResult) {
        val lista = (parcelasResult as? Result.Success)?.data ?: return@LaunchedEffect
        if (selectedId == null && lista.isNotEmpty()) {
            selectedId = lista.first().id
            parcelaVm.cargarParcelaCompleta(lista.first().id)
        }
    }

    LaunchedEffect(parcelaCompletaResult) {
        val pc = (parcelaCompletaResult as? Result.Success)?.data ?: return@LaunchedEffect
        sigpacCargado = false
        val s = pc.referenciaSigpac
        val a = pc.datosAgronomicos
        parcelaFs = ParcelaFs(
            alias                = pc.parcela.alias                ?: "",
            sistemaAsesoramiento = pc.parcela.sistemaAsesoramiento ?: "",
            zonaNitratos         = pc.parcela.zonaNitratos         ?: false,
        )
        sigpacFs = SigpacFs(
            id               = s?.id               ?: 0,
            provincia        = s?.provincia        ?: "",
            terminoMunicipal = s?.terminoMunicipal ?: "",
            codigoAgregado   = s?.codigoAgregado   ?: "",
            zona             = s?.zona             ?: "",
            numeroPoligono   = s?.numeroPoligono   ?: "",
            numeroParcela    = s?.numeroParcela    ?: "",
            numeroRecinto    = s?.numeroRecinto    ?: "",
            usoSigpac        = s?.usoSigpac        ?: "",
            superficieHa     = s?.superficieHa?.toString() ?: "",
        )
        agronomicaFs = AgronomicaFs(
            id                 = a?.id                 ?: 0,
            especieVariedad    = a?.especieVariedad    ?: "",
            ecoregimenPractica = a?.ecoregimenPractica ?: "",
            secanoRegadio      = a?.secanoRegadio      ?: "",
            fechaInicio        = a?.fechaInicio        ?: "",
            fechaFin           = a?.fechaFin           ?: "",
            aireLibreProtegido = a?.aireLibreProtegido ?: "",
        )
        sigpacCargado = true
    }

    val parcelaSeleccionada = parcelas.find { it.id == selectedId }

    //confirmacion borrado
    parcelaAEliminar?.let { p ->
        AlertDialog(
            onDismissRequest = { parcelaAEliminar = null },
            title = {
                Text(
                    "Eliminar parcela",
                    style = MaterialTheme.extraTypography.display.copy(fontSize = 17.sp),
                    color = TextoPrimario,
                )
            },
            text = {
                Text(
                    "¿Seguro que quieres eliminar \"${p.alias ?: "Parcela ${p.id}"}\" y todos sus datos SIGPAC y agronómicos?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextoPrimario),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    parcelaVm.eliminarParcelaEnCascada(p.id)
                    if (selectedId == p.id) selectedId = null
                    parcelaAEliminar = null
                }) {
                    Text("Eliminar", color = TerracotaAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { parcelaAEliminar = null }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            containerColor = SuperficieSepia,
        )
    }

    DesktopWrapper(
        activeIndex   = 2,
        onNavigate    = { idx ->
            when (idx) {
                0    -> onVerInicio()
                1    -> onVerActividades()
                3    -> onVerProductos()
                4    -> onVerConfiguracion()
                5    -> onVerAjustes()
                else -> {}
            }
        },
        nombreUsuario = ajustesVm.nombreMostrado,
        rolUsuario    = ajustesVm.rolUsuario,
    ) {
        DesktopTopBar(
            title    = "Gestión de parcelas",
            subtitle = "Edición de datos SIGPAC · ${ajustesVm.explotacionNombre ?: ""}",
            actions  = listOf(
                DesktopTopBarAction(
                    label   = "Nueva parcela",
                    icon    = Icons.Outlined.Add,
                    primary = true,
                    onClick = {
                        parcelaVm.crearParcela(
                            Parcela(id = 0, alias = "Nueva parcela ${parcelas.size + 1}")
                        )
                    },
                ),
            ),
        )

        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // listar parcelas
            Column(
                modifier = Modifier
                    .width(264.dp)
                    .fillMaxHeight()
                    .background(SuperficieSepia)
                    .drawBehind {
                        drawLine(BordeNormal, Offset(size.width, 0f), Offset(size.width, size.height), 1.dp.toPx())
                    }
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 13.dp, bottom = 8.dp),
                ) {
                    Text(
                        text  = "Parcelas · ${parcelas.size} registros".uppercase(),
                        style = MaterialTheme.extraTypography.eyebrow,
                        color = TextoTerciario,
                    )
                }
                parcelas.forEach { parcela ->
                    val isActive = parcela.id == selectedId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                val barWidth = 3.dp.toPx()
                                drawLine(
                                    color       = if (isActive) OlivaPrimario else Color.Transparent,
                                    start       = Offset(0f, 0f),
                                    end         = Offset(0f, size.height),
                                    strokeWidth = barWidth,
                                )
                                drawLine(
                                    color       = BordeClaro,
                                    start       = Offset(0f, size.height),
                                    end         = Offset(size.width, size.height),
                                    strokeWidth = 1.dp.toPx(),
                                )
                            }
                            .background(if (isActive) CremaPrincipal else Color.Transparent)
                            .clickable {
                                selectedId = parcela.id
                                parcelaVm.cargarParcelaCompleta(parcela.id)
                            }
                            .padding(start = 16.dp, end = 8.dp, top = 13.dp, bottom = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = parcela.alias ?: "Parcela ${parcela.id}",
                                fontSize   = 14.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color      = TextoPrimario,
                            )
                            Text(
                                text     = buildString {
                                    parcela.sistemaAsesoramiento?.let { append(it) }
                                    parcela.zonaNitratos?.let { if (it) append(" · Zona nitratos") }
                                }.ifBlank { "Sin datos adicionales" },
                                fontSize = 12.sp,
                                color    = TextoSecundario,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        Icon(
                            imageVector        = Icons.Outlined.Delete,
                            contentDescription = "Eliminar parcela",
                            tint               = TerracotaAccent.copy(alpha = 0.6f),
                            modifier           = Modifier
                                .size(16.dp)
                                .clickable { parcelaAEliminar = parcela },
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                }
                if (parcelas.isEmpty()) {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Sin parcelas", style = MaterialTheme.typography.bodyMedium.copy(color = TextoTerciario))
                    }
                }
            }

            // forms
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (parcelaSeleccionada == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Selecciona una parcela", style = MaterialTheme.typography.bodyMedium.copy(color = TextoTerciario))
                    }
                } else {
                    ParcelaDetail(
                        parcela            = parcelaSeleccionada,
                        parcelaFs          = parcelaFs,
                        onParcelaFsChange  = { parcelaFs = it },
                        onGuardarParcela   = {
                            parcelaVm.actualizarParcela(
                                parcelaSeleccionada.copy(
                                    alias                = parcelaFs.alias.ifBlank { null },
                                    sistemaAsesoramiento = parcelaFs.sistemaAsesoramiento.ifBlank { null },
                                    zonaNitratos         = parcelaFs.zonaNitratos,
                                )
                            )
                        },
                        explotacionNombre  = ajustesVm.explotacionNombre,
                        sigpacFs           = sigpacFs,
                        agronomicaFs       = agronomicaFs,
                        onSigpacChange     = { sigpacFs = it },
                        onAgronomicaChange = { agronomicaFs = it },
                        onGuardarSigpac    = {
                            parcelaVm.guardarSigpac(
                                parcelaSeleccionada.id,
                                ReferenciaSigpac(
                                    id               = sigpacFs.id,
                                    parcelaId        = parcelaSeleccionada.id,
                                    provincia        = sigpacFs.provincia.ifBlank { null },
                                    terminoMunicipal = sigpacFs.terminoMunicipal.ifBlank { null },
                                    codigoAgregado   = sigpacFs.codigoAgregado.ifBlank { null },
                                    zona             = sigpacFs.zona.ifBlank { null },
                                    numeroPoligono   = sigpacFs.numeroPoligono.ifBlank { null },
                                    numeroParcela    = sigpacFs.numeroParcela.ifBlank { null },
                                    numeroRecinto    = sigpacFs.numeroRecinto.ifBlank { null },
                                    usoSigpac        = sigpacFs.usoSigpac.ifBlank { null },
                                    superficieHa     = sigpacFs.superficieHa.toDoubleOrNull(),
                                )
                            )
                        },
                        onGuardarAgronomico = {
                            parcelaVm.guardarAgronomico(
                                parcelaSeleccionada.id,
                                DatosAgronomicos(
                                    id                 = agronomicaFs.id,
                                    parcelaId          = parcelaSeleccionada.id,
                                    especieVariedad    = agronomicaFs.especieVariedad.ifBlank { null },
                                    ecoregimenPractica = agronomicaFs.ecoregimenPractica.ifBlank { null },
                                    secanoRegadio      = agronomicaFs.secanoRegadio.ifBlank { null },
                                    fechaInicio        = agronomicaFs.fechaInicio.ifBlank { null },
                                    fechaFin           = agronomicaFs.fechaFin.ifBlank { null },
                                    aireLibreProtegido = agronomicaFs.aireLibreProtegido.ifBlank { null },
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}

// deetalle de parcelas

@Composable
private fun ParcelaDetail(
    parcela            : Parcela,
    parcelaFs          : ParcelaFs,
    onParcelaFsChange  : (ParcelaFs) -> Unit,
    onGuardarParcela   : () -> Unit,
    explotacionNombre  : String?,
    sigpacFs           : SigpacFs,
    agronomicaFs       : AgronomicaFs,
    onSigpacChange     : (SigpacFs) -> Unit,
    onAgronomicaChange : (AgronomicaFs) -> Unit,
    onGuardarSigpac    : () -> Unit,
    onGuardarAgronomico: () -> Unit,
) {
    var sistemaExpanded by remember { mutableStateOf(false) }
    var ecoExpanded     by remember { mutableStateOf(false) }
    var aireExpanded    by remember { mutableStateOf(false) }
    var usoExpanded     by remember { mutableStateOf(false) }
    var especieExpanded by remember { mutableStateOf(false) }

    // Estado local para el dropdown especie/variedad.
    // Se resetea con remember(parcela.id) al cambiar de parcela.
    // Si el valor cargado no está en la lista oficial → "Otro" preseleccionado
    // con el texto antiguo en el campo libre.
    var especieDropdownSel by remember(parcela.id) {
        val v = agronomicaFs.especieVariedad
        mutableStateOf(if (v.isBlank() || v in ESPECIE_VARIEDAD_OPCIONES) v else "Otro")
    }
    var especieTextoLibre by remember(parcela.id) {
        val v = agronomicaFs.especieVariedad
        mutableStateOf(if (v.isNotBlank() && v !in ESPECIE_VARIEDAD_OPCIONES) v else "")
    }

    val sistemaOpciones: List<String> = run {
        val valor = parcelaFs.sistemaAsesoramiento
        if (valor.isNotBlank() && valor !in SISTEMA_ASESORAMIENTO_OPCIONES)
            listOf(valor) + SISTEMA_ASESORAMIENTO_OPCIONES
        else
            SISTEMA_ASESORAMIENTO_OPCIONES
    }


    val usoOpciones: List<Pair<String, String>> = run {
        val oficiales = USO_SIGPAC_OPCIONES.entries.map { it.key to it.value }
        val valor = sigpacFs.usoSigpac
        if (valor.isNotBlank() && valor !in USO_SIGPAC_OPCIONES)
            listOf(valor to valor) + oficiales
        else
            oficiales
    }

    // basicos
    SectionHeader(title = "Datos básicos", onGuardar = onGuardarParcela)
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(SuperficieSepia, RoundedCornerShape(12.dp))
            .border(1.dp, BordeNormal, RoundedCornerShape(12.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DesktopFormField(
                label         = "Alias / Nombre",
                value         = parcelaFs.alias,
                onValueChange = { onParcelaFsChange(parcelaFs.copy(alias = it)) },
                placeholder   = "Ej. La Vega Norte",
                modifier      = Modifier.weight(1.5f),
            )
            DesktopFormField(
                label    = "Explotación",
                value    = explotacionNombre ?: "—",
                readOnly = true,
                modifier = Modifier.weight(1f),
            )
        }
        Box {
            DesktopSelectField(
                label  = "Sistema de asesoramiento GIP",
                value  = parcelaFs.sistemaAsesoramiento,
                onClick = { sistemaExpanded = true },
            )
            DropdownMenu(
                expanded         = sistemaExpanded,
                onDismissRequest = { sistemaExpanded = false },
            ) {
                sistemaOpciones.forEach { opcion ->
                    DropdownMenuItem(
                        text    = { Text(opcion) },
                        onClick = {
                            onParcelaFsChange(parcelaFs.copy(sistemaAsesoramiento = opcion))
                            sistemaExpanded = false
                        },
                    )
                }
            }
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text  = "Zona vulnerable a nitratos",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextoPrimario),
            )
            Switch(
                checked         = parcelaFs.zonaNitratos,
                onCheckedChange = { onParcelaFsChange(parcelaFs.copy(zonaNitratos = it)) },
                colors          = SwitchDefaults.colors(
                    checkedThumbColor       = CremaPrincipal,
                    checkedTrackColor       = OlivaPrimario,
                    uncheckedThumbColor     = CremaPrincipal,
                    uncheckedTrackColor     = BordeNormal,
                    uncheckedBorderColor    = BordeNormal,
                ),
            )
        }
    }

    //sigpac
    SectionHeader(title = "Datos SIGPAC", onGuardar = onGuardarSigpac)
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(SuperficieSepia, RoundedCornerShape(12.dp))
            .border(1.dp, BordeNormal, RoundedCornerShape(12.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DesktopFormField("Provincia (cód.)",   sigpacFs.provincia,        { onSigpacChange(sigpacFs.copy(provincia = it)) },        modifier = Modifier.weight(1f))
            DesktopFormField("Término municipal",  sigpacFs.terminoMunicipal, { onSigpacChange(sigpacFs.copy(terminoMunicipal = it)) }, modifier = Modifier.weight(1f))
            DesktopFormField("Municipio (cód.)",   sigpacFs.codigoAgregado,   { onSigpacChange(sigpacFs.copy(codigoAgregado = it)) },   modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DesktopFormField("Polígono", sigpacFs.numeroPoligono, { onSigpacChange(sigpacFs.copy(numeroPoligono = it)) }, modifier = Modifier.weight(1f))
            DesktopFormField("Parcela",  sigpacFs.numeroParcela,  { onSigpacChange(sigpacFs.copy(numeroParcela = it)) },  modifier = Modifier.weight(1f))
            DesktopFormField("Recinto",  sigpacFs.numeroRecinto,  { onSigpacChange(sigpacFs.copy(numeroRecinto = it)) },  modifier = Modifier.weight(1f))
        }
        CampoAvisoInfo(
            mensaje = "Selecciona el uso oficial de la parcela según la clasificación SIGPAC del Ministerio."
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1.5f)) {
                DesktopSelectField(
                    label  = "Uso SIGPAC",
                    value  = sigpacFs.usoSigpac.let { code ->
                        if (code.isBlank()) "" else USO_SIGPAC_OPCIONES[code]?.let { "$code — $it" } ?: code
                    },
                    onClick = { usoExpanded = true },
                )
                DropdownMenu(
                    expanded         = usoExpanded,
                    onDismissRequest = { usoExpanded = false },
                ) {
                    usoOpciones.forEach { (code, label) ->
                        DropdownMenuItem(
                            text    = { Text(if (code == label) code else "$code — $label") },
                            onClick = {
                                onSigpacChange(sigpacFs.copy(usoSigpac = code))
                                usoExpanded = false
                            },
                        )
                    }
                }
            }
            DesktopFormField("Superficie (ha)", sigpacFs.superficieHa, { onSigpacChange(sigpacFs.copy(superficieHa = it)) }, modifier = Modifier.weight(1f))
            DesktopFormField("Zona (cód.)",     sigpacFs.zona,         { onSigpacChange(sigpacFs.copy(zona = it)) },         modifier = Modifier.weight(1f))
        }
    }

    //agronomicos
    SectionHeader(title = "Datos agronómicos", onGuardar = onGuardarAgronomico)
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(SuperficieSepia, RoundedCornerShape(12.dp))
            .border(1.dp, BordeNormal, RoundedCornerShape(12.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1.5f)) {
                DesktopSelectField(
                    label  = "Especie / variedad",
                    value  = if (especieDropdownSel == "Otro") "Otro" else especieDropdownSel,
                    onClick = { especieExpanded = true },
                )
                DropdownMenu(
                    expanded         = especieExpanded,
                    onDismissRequest = { especieExpanded = false },
                ) {
                    (ESPECIE_VARIEDAD_OPCIONES + "Otro").forEach { opcion ->
                        DropdownMenuItem(
                            text    = { Text(opcion) },
                            onClick = {
                                especieDropdownSel = opcion
                                if (opcion != "Otro") {
                                    especieTextoLibre = ""
                                    onAgronomicaChange(agronomicaFs.copy(especieVariedad = opcion))
                                } else {
                                    onAgronomicaChange(agronomicaFs.copy(especieVariedad = especieTextoLibre))
                                }
                                especieExpanded = false
                            },
                        )
                    }
                }
            }
            DesktopFormField(
                label         = "Régimen hídrico",
                value         = agronomicaFs.secanoRegadio,
                onValueChange = { onAgronomicaChange(agronomicaFs.copy(secanoRegadio = it)) },
                modifier      = Modifier.weight(1f),
            )
        }
        if (especieDropdownSel == "Otro") {
            DesktopFormField(
                label         = "Especie / variedad (personalizada)",
                value         = especieTextoLibre,
                onValueChange = {
                    especieTextoLibre = it
                    onAgronomicaChange(agronomicaFs.copy(especieVariedad = it))
                },
                placeholder   = "Escribe la especie o variedad",
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Eco-régimen FEADER — desplegable
            Box(modifier = Modifier.weight(1.5f)) {
                DesktopSelectField(
                    label = "Eco-régimen FEADER",
                    value = agronomicaFs.ecoregimenPractica,
                    onClick = { ecoExpanded = true },
                )
                DropdownMenu(
                    expanded          = ecoExpanded,
                    onDismissRequest  = { ecoExpanded = false },
                ) {
                    ECOREGIMEN_OPCIONES.forEach { opcion ->
                        DropdownMenuItem(
                            text    = { Text(opcion) },
                            onClick = {
                                onAgronomicaChange(agronomicaFs.copy(ecoregimenPractica = opcion))
                                ecoExpanded = false
                            },
                        )
                    }
                }
            }
            // Aire libre / Protegido — desplegable
            Box(modifier = Modifier.weight(1f)) {
                DesktopSelectField(
                    label = "Aire libre / Protegido",
                    value = agronomicaFs.aireLibreProtegido.let { code ->
                        if (code.isBlank()) "" else "$code — ${AIRE_LIBRE_OPCIONES[code].orEmpty()}"
                    },
                    onClick = { aireExpanded = true },
                )
                DropdownMenu(
                    expanded         = aireExpanded,
                    onDismissRequest = { aireExpanded = false },
                ) {
                    AIRE_LIBRE_OPCIONES.forEach { (code, label) ->
                        DropdownMenuItem(
                            text    = { Text("$code — $label") },
                            onClick = {
                                onAgronomicaChange(agronomicaFs.copy(aireLibreProtegido = code))
                                aireExpanded = false
                            },
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DesktopFormField("Fecha de siembra (AAAA-MM-DD)",       agronomicaFs.fechaInicio, { onAgronomicaChange(agronomicaFs.copy(fechaInicio = it)) }, modifier = Modifier.weight(1f))
            DesktopFormField("Fecha prevista cosecha (AAAA-MM-DD)", agronomicaFs.fechaFin,    { onAgronomicaChange(agronomicaFs.copy(fechaFin = it)) },    modifier = Modifier.weight(1f))
        }
    }
}

//ayudass

@Composable
private fun SectionHeader(title : String, onGuardar: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = title.uppercase(),
            style = MaterialTheme.extraTypography.eyebrow,
            color = TextoTerciario,
        )
        Row(
            modifier              = Modifier
                .background(OlivaPrimario, RoundedCornerShape(8.dp))
                .border(1.dp, OlivaOscuro, RoundedCornerShape(8.dp))
                .clickable(onClick = onGuardar)
                .padding(horizontal = 14.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Check, contentDescription = null, tint = CremaPrincipal, modifier = Modifier.size(14.dp))
            Text("Guardar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CremaPrincipal)
        }
    }
}
