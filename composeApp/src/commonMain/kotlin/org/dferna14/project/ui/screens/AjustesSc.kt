package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesSc() {
    var nombreExplotacion by remember { mutableStateOf("") }
    var nombreAsesor by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Datos de la Explotación",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = nombreExplotacion,
                onValueChange = { nombreExplotacion = it },
                label = { Text("Nombre de la explotación") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            Text(
                text = "Datos del Asesor",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = nombreAsesor,
                onValueChange = { nombreAsesor = it },
                label = { Text("Nombre del asesor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            Text(
                text = "Acerca de",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Cuaderno de Campo Digital",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Versión 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "TFG - Daniel Fernández",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}