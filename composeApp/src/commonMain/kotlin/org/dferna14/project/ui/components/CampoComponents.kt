package org.dferna14.project.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.domain.model.EquipoAplicacion
import org.dferna14.project.domain.model.EstadoActividad
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.Usuario
import org.dferna14.project.ui.theme.AzulFondoPendiente
import org.dferna14.project.ui.theme.AzulPendiente
import org.dferna14.project.ui.theme.BlancoPuro
import org.dferna14.project.ui.theme.BordeMedio
import org.dferna14.project.ui.theme.BordeNaranjaSuave
import org.dferna14.project.ui.theme.BordeSuave
import org.dferna14.project.ui.theme.CremaSecundario
import org.dferna14.project.ui.theme.GrisBorrador
import org.dferna14.project.ui.theme.GrisFondoBorrador
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.TextoPlaceholder
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.theme.VerdeFondoInfo
import org.dferna14.project.ui.theme.VerdeFondoValidada
import org.dferna14.project.ui.theme.VerdeInfo
import org.dferna14.project.ui.theme.VerdeValidada

/**
 * Componentes reutilizables del sistema de diseño "Campo". Cada uno encapsula
 * los detalles visuales (colores, padding, border-radius) para que las pantallas
 * los usen sin repetir constantes. Pensados para Android y Desktop sin
 * dependencias específicas de plataforma.
 */


@Composable
fun CampoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(BlancoPuro)
        .border(0.5.dp, BordeSuave, RoundedCornerShape(14.dp))

    val finalModifier = if (onClick != null) {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }

    Column(
        modifier = finalModifier.padding(horizontal = 12.dp, vertical = 12.dp),
        content = content
    )
}


@Composable
fun CampoField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CremaSecundario)
            .border(0.5.dp, BordeSuave, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            color = TextoTerciario,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = TextoPrimario,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CampoTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, color = TextoTerciario, fontSize = 12.sp) },
        placeholder = if (placeholder.isNotBlank()) {
            { Text(placeholder, color = TextoPlaceholder, fontSize = 14.sp) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        minLines = minLines,
        singleLine = minLines == 1,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextoPrimario),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = NaranjaPrimario,
            unfocusedBorderColor = BordeMedio,
            disabledBorderColor  = BordeMedio,
            focusedContainerColor   = BlancoPuro,
            unfocusedContainerColor = BlancoPuro,
            disabledContainerColor  = BlancoPuro,
            focusedLabelColor   = NaranjaPrimario,
            unfocusedLabelColor = TextoTerciario,
            cursorColor = NaranjaPrimario
        )
    )
}

@Composable
fun EstadoBadge(estado: EstadoActividad) {
    val (fondo, texto, etiqueta) = when (estado) {
        EstadoActividad.BORRADOR          -> Triple(GrisFondoBorrador, GrisBorrador, "Borrador")
        EstadoActividad.PENDIENTE_VALIDAR -> Triple(AzulFondoPendiente, AzulPendiente, "Pendiente")
        EstadoActividad.VALIDADA          -> Triple(VerdeFondoValidada, VerdeValidada, "Validada")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(fondo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = etiqueta,
            color = texto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun CampoToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BlancoPuro)
            .border(0.5.dp, BordeSuave, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextoPrimario,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = BlancoPuro,
                checkedTrackColor   = NaranjaPrimario,
                uncheckedThumbColor = BlancoPuro,
                uncheckedTrackColor = TextoTerciario
            )
        )
    }
}


@Composable
fun CampoAvisoInfo(
    mensaje: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(VerdeFondoInfo)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = VerdeInfo,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = mensaje,
            color = VerdeInfo,
            fontSize = 11.sp
        )
    }
}


@Composable
fun CampoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = NaranjaPrimario,
            contentColor           = BlancoPuro,
            disabledContainerColor = TextoTerciario,
            disabledContentColor   = BlancoPuro
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun CampoSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BordeNaranjaSuave),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CremaSecundario,
            contentColor   = NaranjaPrimario
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NaranjaPrimario,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = NaranjaPrimario,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Dropdown mejorado con CampoTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CampoDropdown(
    label: String,
    selectedItem: T?,
    items: List<T>,
    itemLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Selecciona una opción"
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedItem?.let(itemLabel) ?: placeholder,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = TextoTerciario, fontSize = 12.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = if (selectedItem != null) TextoPrimario else TextoPlaceholder
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = NaranjaPrimario,
                unfocusedBorderColor = BordeMedio,
                disabledBorderColor  = BordeMedio,
                focusedContainerColor   = BlancoPuro,
                unfocusedContainerColor = BlancoPuro,
                disabledContainerColor  = BlancoPuro,
                focusedLabelColor   = NaranjaPrimario,
                unfocusedLabelColor = TextoTerciario,
                focusedTrailingIconColor   = TextoTerciario,
                unfocusedTrailingIconColor = TextoTerciario
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (items.isEmpty()) {
                DropdownMenuItem(text = { Text("No hay opciones disponibles") }, onClick = {})
            } else {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemLabel(item)) },
                        onClick = {
                            onSelect(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipoDropdown(
    equipoSeleccionado: EquipoAplicacion?,
    equiposState: Result<List<EquipoAplicacion>>,
    expandido: Boolean,
    onExpandidoChange: (Boolean) -> Unit,
    onSeleccionar: (EquipoAplicacion?) -> Unit,
    label: String = "Equipo de aplicación"
) {
    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = onExpandidoChange
    ) {
        OutlinedTextField(
            value = equipoSeleccionado?.let { eq ->
                listOfNotNull(eq.tipo, eq.marca, eq.modelo).joinToString(" ").ifBlank { "Equipo ${eq.id}" }
            } ?: "Sin asignar",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { onExpandidoChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text("Sin asignar") },
                onClick = { onSeleccionar(null) }
            )
            when (val s = equiposState) {
                is Result.Success -> s.data.forEach { eq ->
                    DropdownMenuItem(
                        text = {
                            Text(listOfNotNull(eq.tipo, eq.marca, eq.modelo).joinToString(" ").ifBlank { "Equipo ${eq.id}" })
                        },
                        onClick = { onSeleccionar(eq) }
                    )
                }
                is Result.Error -> DropdownMenuItem(
                    text = { Text("Error al cargar equipos") },
                    onClick = {}
                )
                is Result.Loading -> DropdownMenuItem(
                    text = { Text("Cargando equipos...") },
                    onClick = {}
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AplicadorDropdown(
    aplicadorSeleccionado: Usuario?,
    usuariosState: Result<List<Usuario>>,
    expandido: Boolean,
    onExpandidoChange: (Boolean) -> Unit,
    onSeleccionar: (Usuario?) -> Unit,
    label: String = "Aplicador"
) {
    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = onExpandidoChange
    ) {
        OutlinedTextField(
            value = aplicadorSeleccionado?.let { u ->
                listOfNotNull(u.nombre, u.apellidos).joinToString(" ")
            } ?: "Sin asignar",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { onExpandidoChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text("Sin asignar") },
                onClick = { onSeleccionar(null) }
            )
            when (val s = usuariosState) {
                is Result.Success -> s.data.forEach { u ->
                    DropdownMenuItem(
                        text = { Text(listOfNotNull(u.nombre, u.apellidos).joinToString(" ")) },
                        onClick = { onSeleccionar(u) }
                    )
                }
                is Result.Error -> DropdownMenuItem(
                    text = { Text("Error al cargar usuarios") },
                    onClick = {}
                )
                is Result.Loading -> DropdownMenuItem(
                    text = { Text("Cargando usuarios...") },
                    onClick = {}
                )
            }
        }
    }
}

// Formateo de fecha, compartido

/** Convierte "2026-04-13" en "13 abr 2026". Si el formato no es el esperado devuelve la cadena tal cual. */
fun formatearFecha(fechaIso: String): String {
    return try {
        val partes = fechaIso.split("-")
        val dia = partes[2].toInt()
        val mes = when (partes[1]) {
            "01" -> "ene"; "02" -> "feb"; "03" -> "mar"
            "04" -> "abr"; "05" -> "may"; "06" -> "jun"
            "07" -> "jul"; "08" -> "ago"; "09" -> "sep"
            "10" -> "oct"; "11" -> "nov"; "12" -> "dic"
            else -> partes[1]
        }
        val anio = partes[0]
        "$dia $mes $anio"
    } catch (e: Exception) {
        fechaIso
    }
}


@Composable
fun CampoPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = TextoSecundario
                )
            }
        },
        supportingText = supportingText?.let { { Text(it) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NaranjaPrimario,
            unfocusedBorderColor = BordeMedio,
            errorBorderColor = RojoEliminar
        )
    )
}