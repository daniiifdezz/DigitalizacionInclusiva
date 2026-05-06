package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelasSc(
    onVolver: () -> Unit,
    onEditarParcela: (Int) -> Unit,
    viewModel: ParcelaVm = koinViewModel()
) {
    val parcelasState by viewModel.parcelas.collectAsState()
    var parcelaExpandida by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarParcelas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parcelas") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("< Menu")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.cargarParcelas() }) {
                        Text("Recargar")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = parcelasState) {
            is Result.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cargando parcelas...")
                    }
                }
            }
            is Result.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.cargarParcelas() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is Result.Success -> {
                if (state.data.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay parcelas disponibles")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "${state.data.size} parcela(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(state.data, key = { it.id }) { parcela ->
                            ParcelaCard(
                                parcela = parcela,
                                expandida = parcelaExpandida == parcela.id,
                                onToggleExpand = {
                                    parcelaExpandida = if (parcelaExpandida == parcela.id) null else parcela.id
                                },
                                onEditar = { onEditarParcela(parcela.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParcelaCard(
    parcela: Parcela,
    expandida: Boolean,
    onToggleExpand: () -> Unit,
    onEditar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggleExpand
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = parcela.alias ?: "Parcela ${parcela.id}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Explotación: ${parcela.explotacionId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (expandida) {
                    Text("▲")
                } else {
                    Text("▼")
                }
            }

            if (expandida) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                InfoRow("Orden", parcela.orden?.toString() ?: "No disponible")
                InfoRow("Sistema Asesoramiento", parcela.sistemaAsesoramiento ?: "No disponible")
                InfoRow("Zona Nitratos", parcela.zonaNitratos?.let { if (it) "Sí" else "No" } ?: "No disponible")

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onEditar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar SIGPAC y agronómicos")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall
        )
    }
}