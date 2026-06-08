package org.dferna14.project.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import org.dferna14.project.ui.screens.OcrCameraScreen
import org.dferna14.project.ui.theme.NaranjaPrimario

@Composable
actual fun CampoTextoConOcr(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    placeholder: String
) {
    val context = LocalContext.current
    var mostrarCamara by remember { mutableStateOf(false) }
    var mostrarDialogoPermiso by remember { mutableStateOf(false) }

    val tienePermisoCamara = ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) mostrarCamara = true
        else mostrarDialogoPermiso = true
    }

    CampoTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        trailingIcon = {
            IconButton(onClick = {
                if (tienePermisoCamara) mostrarCamara = true
                else permissionLauncher.launch(Manifest.permission.CAMERA)
            }) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Escanear con cámara",
                    tint = NaranjaPrimario
                )
            }
        }
    )

    if (mostrarCamara) {
        Dialog(
            onDismissRequest = { mostrarCamara = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            OcrCameraScreen(
                onTextoDetectado = { texto -> onValueChange(texto) },
                onCerrar = { mostrarCamara = false }
            )
        }
    }

    if (mostrarDialogoPermiso) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermiso = false },
            title = { Text("Permiso de cámara necesario") },
            text = {
                Text(
                    "Para escanear códigos necesitas conceder acceso a la cámara. " +
                    "Ve a Ajustes del sistema → Aplicaciones → tu app → Permisos → Cámara."
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoPermiso = false }) { Text("Entendido") }
            }
        )
    }
}
