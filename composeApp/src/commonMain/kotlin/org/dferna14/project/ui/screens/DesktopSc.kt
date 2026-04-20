package org.dferna14.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dferna14.project.ui.viewmodel.ActividadViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopMainSc(
    onVerListado: () -> Unit,
    onVerPendientes: () -> Unit,
    onVerParcelas: () -> Unit,
    onVerProductos: () -> Unit,
    onVerValidar: (Int) -> Unit,
    viewModel: ActividadViewModel = koinViewModel()
) {
    var selectedItem by remember { mutableStateOf(0) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Menu lateral sederhana (Column)
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

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "TFG - Daniel Fernandez",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        // Main content
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

            // Quick stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Pendientes",
                    value = "12",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Validadas hoy",
                    value = "5",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total mes",
                    value = "45",
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