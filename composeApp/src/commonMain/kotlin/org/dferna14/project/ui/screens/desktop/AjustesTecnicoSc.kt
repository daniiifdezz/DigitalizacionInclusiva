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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.Usuario
import org.dferna14.project.ui.components.desktop.DesktopFormField
import org.dferna14.project.ui.components.desktop.DesktopTableColumn
import org.dferna14.project.ui.components.desktop.DesktopTableHeader
import org.dferna14.project.ui.components.desktop.DesktopTableRow
import org.dferna14.project.ui.components.desktop.DesktopTopBar
import org.dferna14.project.ui.components.desktop.DesktopWrapper
import org.dferna14.project.ui.components.desktop.InlineCreateCard
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
import org.dferna14.project.ui.viewmodel.AuthVm
import org.dferna14.project.ui.viewmodel.UsuarioVm
import org.koin.compose.viewmodel.koinViewModel

private val COLS_TECNICOS = listOf(
    DesktopTableColumn("Técnico",  weight = 1.4f),
    DesktopTableColumn("Correo",   weight = 1.8f),
    DesktopTableColumn("Estado",   weight = 0.8f),
    DesktopTableColumn("",         fixedWidth = 60.dp),
)

private val COLS_AGRICULTORES = listOf(
    DesktopTableColumn("Agricultor",  weight = 1.4f),
    DesktopTableColumn("Correo",      weight = 1.8f),
    DesktopTableColumn("Estado",      weight = 0.8f),
    DesktopTableColumn("",            fixedWidth = 60.dp),
)

private data class NuevoAgricultorFs(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
)

@Composable
fun AjustesTecnicoSc(
    onVerInicio: () -> Unit,
    onVerActividades: () -> Unit,
    onVerParcelas: () -> Unit,
    onVerProductos: () -> Unit,
    onVerConfiguracion: () -> Unit,
    onCerrarSesion: () -> Unit,
    usuarioVm: UsuarioVm = koinViewModel(),
    ajustesVm: AjustesVm = koinViewModel(),
    authVm: AuthVm = koinViewModel(),
) {
    val usuariosResult  by usuarioVm.usuarios.collectAsState()
    val tecnicosResult  by usuarioVm.tecnicos.collectAsState()
    var mensajeError    by remember { mutableStateOf<String?>(null) }
    val mensajeRol      by usuarioVm.mensajeRol.collectAsState()
    var nuevoFs             by remember { mutableStateOf(NuevoAgricultorFs()) }
    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var confirmar           by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        usuarioVm.cargarUsuarios(rol = "AGRICULTOR")
        usuarioVm.cargarTecnicos()
    }

    LaunchedEffect(Unit) {
        usuarioVm.mensajeError.collect { error ->
            mensajeError = error
            delay(4_000)
            mensajeError = null
        }
    }
    LaunchedEffect(mensajeRol) {
        if (mensajeRol != null) { delay(4_000); usuarioVm.limpiarMensajeRol() }
    }

    val agricultores: List<Usuario> = (usuariosResult as? Result.Success)?.data
        ?.filter { it.rol == "AGRICULTOR" } ?: emptyList()
    val tecnicos: List<Usuario>     = (tecnicosResult as? Result.Success)?.data ?: emptyList()

    usuarioSeleccionado?.let { u ->
        val esAgricultor = u.rol == "AGRICULTOR"
        val nuevoRol     = if (esAgricultor) "TECNICO" else "AGRICULTOR"
        val accionLabel  = if (esAgricultor) "Promover a Técnico" else "Degradar a Agricultor"
        val accionColor  = if (esAgricultor) OlivaPrimario else TerracotaAccent

        AlertDialog(
            onDismissRequest = { usuarioSeleccionado = null; confirmar = false },
            title = {
                Text(
                    text  = if (!confirmar) "Gestionar usuario" else "Confirmar cambio de rol",
                    style = MaterialTheme.extraTypography.display.copy(fontSize = 17.sp),
                    color = TextoPrimario,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!confirmar) {
                        Text(
                            text  = listOfNotNull(u.nombre, u.apellidos).joinToString(" "),
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextoPrimario),
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(u.email, style = MaterialTheme.typography.bodySmall.copy(color = TextoSecundario))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = "Rol actual: ${u.rol}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextoTerciario),
                        )
                    } else {
                        Text(
                            text  = "¿Cambiar el rol de ${u.nombre} a $nuevoRol?",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextoPrimario),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = "Esta acción es reversible.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextoTerciario),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (!confirmar) {
                        confirmar = true
                    } else {
                        usuarioVm.cambiarRolUsuario(u.id, nuevoRol)
                        usuarioSeleccionado = null
                        confirmar = false
                    }
                }) {
                    Text(
                        text       = if (!confirmar) accionLabel else "Confirmar",
                        color      = accionColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioSeleccionado = null; confirmar = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            containerColor = SuperficieSepia,
        )
    }

    DesktopWrapper(
        activeIndex   = 5,
        onNavigate    = { idx ->
            when (idx) {
                0    -> onVerInicio()
                1    -> onVerActividades()
                2    -> onVerParcelas()
                3    -> onVerProductos()
                4    -> onVerConfiguracion()
                else -> {}
            }
        },
        nombreUsuario = ajustesVm.nombreMostrado,
        rolUsuario    = ajustesVm.rolUsuario,
    ) {
        DesktopTopBar(
            title    = "Ajustes",
            subtitle = "Perfil del técnico y gestión de la explotación",
        )

        Row(
            modifier              = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment     = Alignment.Top,
        ) {
            Column(modifier = Modifier.width(300.dp)) {
                Text(
                    text     = "Mi perfil".uppercase(),
                    style    = MaterialTheme.extraTypography.eyebrow,
                    color    = TextoTerciario,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SuperficieSepia, RoundedCornerShape(12.dp))
                        .border(1.dp, BordeNormal, RoundedCornerShape(12.dp))
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(64.dp)
                                .background(OlivaTint, CircleShape)
                                .border(2.dp, OlivaClaro, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text       = initials(ajustesVm.nombreMostrado),
                                style      = MaterialTheme.extraTypography.display.copy(fontSize = 22.sp),
                                color      = OlivaPrimario,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text       = ajustesVm.nombreMostrado,
                            style      = MaterialTheme.extraTypography.display.copy(fontSize = 18.sp),
                            color      = TextoPrimario,
                        )
                        Text(
                            text     = ajustesVm.rolUsuario,
                            fontSize = 12.sp,
                            color    = TextoTerciario,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    DesktopFormField(label = "Correo",      value = ajustesVm.emailUsuario,            readOnly = true)
                    DesktopFormField(label = "Rol",         value = ajustesVm.rolUsuario,              readOnly = true)
                    DesktopFormField(label = "Explotación", value = ajustesVm.explotacionNombre ?: "—", readOnly = true)
                }
                Spacer(Modifier.height(16.dp))
                // Botón cerrar sesión
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(2.dp, TerracotaAccent, RoundedCornerShape(8.dp))
                        .clickable(onClick = { authVm.cerrarSesion(); onCerrarSesion() }),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Close,
                        contentDescription = null,
                        tint               = TerracotaAccent,
                        modifier           = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = "Cerrar sesión",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TerracotaAccent,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                // Cabecera sección
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text  = "Agricultores de la explotación".uppercase(),
                        style = MaterialTheme.extraTypography.eyebrow,
                        color = TextoTerciario,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SuperficieSepia, RoundedCornerShape(12.dp))
                        .border(1.dp, BordeNormal, RoundedCornerShape(12.dp)),
                ) {
                    DesktopTableHeader(COLS_AGRICULTORES)
                    agricultores.forEachIndexed { i, u ->
                        DesktopTableRow(
                            columns = COLS_AGRICULTORES,
                            last    = i == agricultores.lastIndex,
                            cells   = listOf(
                                {
                                    Text(
                                        text       = listOfNotNull(u.nombre, u.apellidos).joinToString(" "),
                                        fontSize   = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = TextoPrimario,
                                    )
                                },
                                {
                                    Text(
                                        text     = u.email,
                                        fontSize = 12.5.sp,
                                        color    = TextoSecundario,
                                    )
                                },
                                {
                                    Box(
                                        modifier = Modifier
                                            .background(OlivaTint, RoundedCornerShape(999.dp))
                                            .border(1.dp, OlivaClaro, RoundedCornerShape(999.dp))
                                            .padding(horizontal = 9.dp, vertical = 3.dp),
                                    ) {
                                        Text(
                                            text       = "Activo",
                                            fontSize   = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = OlivaOscuro,
                                        )
                                    }
                                },
                                {
                                    Text(
                                        text       = "Editar →",
                                        fontSize   = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color      = OlivaPrimario,
                                        modifier   = Modifier.clickable { usuarioSeleccionado = u },
                                    )
                                },
                            ),
                        )
                    }
                    if (agricultores.isEmpty()) {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Sin agricultores registrados",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextoTerciario),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                InlineCreateCard(title = "Nuevo agricultor") {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        DesktopFormField(
                            label         = "Nombre completo",
                            value         = nuevoFs.nombre,
                            onValueChange = { nuevoFs = nuevoFs.copy(nombre = it) },
                            placeholder   = "Nombre y apellidos",
                            modifier      = Modifier.weight(1f),
                        )
                        DesktopFormField(
                            label         = "Correo electrónico",
                            value         = nuevoFs.email,
                            onValueChange = { nuevoFs = nuevoFs.copy(email = it) },
                            placeholder   = "correo@dominio.es",
                            modifier      = Modifier.weight(1f),
                        )
                        DesktopFormField(
                            label         = "Contraseña",
                            value         = nuevoFs.password,
                            onValueChange = { nuevoFs = nuevoFs.copy(password = it) },
                            placeholder   = "Mínimo 6 caracteres",
                            modifier      = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier              = Modifier
                            .background(OlivaPrimario, RoundedCornerShape(8.dp))
                            .border(1.dp, OlivaOscuro, RoundedCornerShape(8.dp))
                            .clickable(onClick = {
                                if (nuevoFs.nombre.isNotBlank() && nuevoFs.email.isNotBlank()) {
                                    usuarioVm.crearAplicador(
                                        usuario    = Usuario(
                                            nombre = nuevoFs.nombre,
                                            email  = nuevoFs.email,
                                            rol    = "AGRICULTOR",
                                        ),
                                        contrasena = nuevoFs.password.ifBlank { null },
                                    )
                                    nuevoFs = NuevoAgricultorFs()
                                }
                            })
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Add,
                            contentDescription = null,
                            tint               = CremaPrincipal,
                            modifier           = Modifier.size(15.dp),
                        )
                        Text(
                            text       = "Crear agricultor",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = CremaPrincipal,
                        )
                    }
                }

                mensajeError?.let { msg ->
                    Spacer(Modifier.height(12.dp))
                    FeedbackBanner(msg, esError = true) { mensajeError = null }
                }
                mensajeRol?.let { msg ->
                    Spacer(Modifier.height(12.dp))
                    FeedbackBanner(msg, esError = msg.startsWith("Error")) { usuarioVm.limpiarMensajeRol() }
                }

                Spacer(Modifier.height(28.dp))

                // ── Técnicos de la explotación ────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text  = "Técnicos de la explotación".uppercase(),
                        style = MaterialTheme.extraTypography.eyebrow,
                        color = TextoTerciario,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SuperficieSepia, RoundedCornerShape(12.dp))
                        .border(1.dp, BordeNormal, RoundedCornerShape(12.dp)),
                ) {
                    DesktopTableHeader(COLS_TECNICOS)
                    tecnicos.forEachIndexed { i, u ->
                        DesktopTableRow(
                            columns = COLS_TECNICOS,
                            last    = i == tecnicos.lastIndex,
                            cells   = listOf(
                                {
                                    Text(
                                        text       = listOfNotNull(u.nombre, u.apellidos).joinToString(" "),
                                        fontSize   = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = TextoPrimario,
                                    )
                                },
                                {
                                    Text(
                                        text     = u.email,
                                        fontSize = 12.5.sp,
                                        color    = TextoSecundario,
                                    )
                                },
                                {
                                    Box(
                                        modifier = Modifier
                                            .background(OlivaTint, RoundedCornerShape(999.dp))
                                            .border(1.dp, OlivaClaro, RoundedCornerShape(999.dp))
                                            .padding(horizontal = 9.dp, vertical = 3.dp),
                                    ) {
                                        Text(
                                            text       = "Activo",
                                            fontSize   = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = OlivaOscuro,
                                        )
                                    }
                                },
                                {
                                    Text(
                                        text       = "Editar →",
                                        fontSize   = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color      = TerracotaAccent,
                                        modifier   = Modifier.clickable { usuarioSeleccionado = u },
                                    )
                                },
                            ),
                        )
                    }
                    if (tecnicos.isEmpty()) {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Sin técnicos registrados",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextoTerciario),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackBanner(mensaje: String, esError: Boolean, onDismiss: () -> Unit) {
    val color = if (esError) TerracotaAccent else OlivaPrimario
    val bg    = if (esError) TerracotaAccent.copy(alpha = 0.08f) else OlivaTint
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text     = mensaje,
            fontSize = 13.sp,
            color    = color,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector        = Icons.Outlined.Close,
            contentDescription = "Cerrar",
            tint               = color,
            modifier           = Modifier.size(16.dp).clickable(onClick = onDismiss),
        )
    }
}

private fun initials(nombre: String): String {
    val partes = nombre.trim().split(" ").filter { it.isNotBlank() }
    return when {
        partes.size >= 2 -> "${partes[0].first()}${partes[1].first()}".uppercase()
        partes.isNotEmpty() -> partes[0].take(2).uppercase()
        else -> "??"
    }
}
