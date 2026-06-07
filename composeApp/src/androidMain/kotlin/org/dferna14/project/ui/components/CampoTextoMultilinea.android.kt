package org.dferna14.project.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.ui.theme.BordeMedio
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.util.VoiceRecognitionState
import org.dferna14.project.util.VoiceRecognizer
import org.koin.compose.koinInject

@Composable
actual fun CampoTextoMultilinea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    minLines: Int,
    maxLines: Int
) {
    val voiceRecognizer: VoiceRecognizer = koinInject()
    val estado by voiceRecognizer.estado.collectAsState(initial = VoiceRecognitionState.Idle)
    var mostrarErrorPermisos by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) voiceRecognizer.iniciarEscucha()
        else mostrarErrorPermisos = true
    }

    LaunchedEffect(estado) {
        when (val e = estado) {
            is VoiceRecognitionState.TextoReconocido -> {
                val nuevoTexto = if (value.isBlank()) e.texto else "$value ${e.texto}"
                onValueChange(nuevoTexto)
            }
            is VoiceRecognitionState.Denegado -> mostrarErrorPermisos = true
            else -> Unit
        }
    }

    DisposableEffect(Unit) {
        onDispose { voiceRecognizer.pararEscucha() }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NaranjaPrimario,
                unfocusedBorderColor = BordeMedio
            ),
            trailingIcon = if (voiceRecognizer.isAvailable) {
                {
                    IconButton(
                        onClick = {
                            if (estado is VoiceRecognitionState.Escuchando) {
                                voiceRecognizer.pararEscucha()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (estado is VoiceRecognitionState.Escuchando)
                                Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Dictar por voz",
                            tint = if (estado is VoiceRecognitionState.Escuchando)
                                RojoEliminar else NaranjaPrimario
                        )
                    }
                }
            } else null
        )

        when (val e = estado) {
            is VoiceRecognitionState.Escuchando -> {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = NaranjaPrimario
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Escuchando... habla ahora", fontSize = 12.sp, color = NaranjaPrimario)
                }
            }
            is VoiceRecognitionState.Error -> {
                Spacer(Modifier.height(4.dp))
                Text(e.mensaje, fontSize = 12.sp, color = RojoEliminar)
            }
            else -> Unit
        }
    }

    if (mostrarErrorPermisos) {
        AlertDialog(
            onDismissRequest = { mostrarErrorPermisos = false },
            title = { Text("Permiso de micrófono necesario") },
            text = {
                Text(
                    "Para usar el dictado por voz, necesitas conceder permiso de micrófono. " +
                    "Si lo has rechazado antes, ve a los Ajustes del sistema para activarlo."
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarErrorPermisos = false }) { Text("Entendido") }
            }
        )
    }
}
