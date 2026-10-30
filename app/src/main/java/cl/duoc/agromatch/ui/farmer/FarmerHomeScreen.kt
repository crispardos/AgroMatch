package cl.duoc.agromatch.ui.farmer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.model.Need
import cl.duoc.agromatch.model.InterestStatus
import cl.duoc.agromatch.database.FarmerNeedOverview
import cl.duoc.agromatch.ui.formatEpochDay
import cl.duoc.agromatch.viewmodel.FarmerViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerHomeScreen(
    viewModel: FarmerViewModel,
    onNavigateToCreateNeed: () -> Unit,
    onNavigateToMatching: (Need) -> Unit,
    onNavigateToNeedDetails: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Terrenos") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Salir")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateNeed) {
                Icon(Icons.Default.Add, contentDescription = "Crear Necesidad")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.needOverviews.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No tienes terrenos registrados.\nToca el botón + para crear uno.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.needOverviews) { overview ->
                        NeedCard(
                            overview = overview,
                            onClick = {
                                if (overview.interestStatus == InterestStatus.COMPLETED.value) {
                                    onNavigateToNeedDetails(overview.need.id)
                                } else {
                                    onNavigateToMatching(overview.need)
                                }
                            }
                        )
                    }
                }
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun NeedCard(
    overview: FarmerNeedOverview,
    onClick: () -> Unit
) {
    val need = overview.need
    val statusLabel = when (overview.interestStatus) {
        InterestStatus.ACTIVE.value -> "Solicitud enviada"
        InterestStatus.COMPLETED.value -> "Completada"
        else -> "Sin piloto seleccionado"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Fumigación de ${Catalog.cropLabel(need.cropCode)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Zona: ${Catalog.zoneLabel(need.zoneCode)}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Superficie: ${need.hectares} ha", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Estado: $statusLabel", style = MaterialTheme.typography.bodyMedium)
            if (overview.interestStatus == InterestStatus.COMPLETED.value) {
                Text(
                    text = overview.ratingScore?.let { "Valoración: $it/5" } ?: "Sin valoración",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "Fechas: ${formatEpochDay(need.startEpochDay)} al ${formatEpochDay(need.endEpochDay)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
