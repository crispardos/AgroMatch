package cl.duoc.agromatch.ui.farmer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.model.*
import cl.duoc.agromatch.viewmodel.FarmerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingResultsScreen(
    viewModel: FarmerViewModel,
    need: Need,
    onNavigateBack: () -> Unit,
    onNavigateToRating: (interestId: Long, pilotAlias: String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.activeInterest?.status) {
        if (state.activeInterest?.status == cl.duoc.agromatch.model.InterestStatus.COMPLETED.value) {
            val interest = state.activeInterest!!
            val pilotName = state.matchResults.find { it.pilot.pilotId == interest.pilotId }?.pilot?.alias ?: "el piloto"

            onNavigateToRating(interest.id, pilotName) // <-- CAMBIO AQUÍ
        }
    }

    LaunchedEffect(need.id) {
        viewModel.selectNeedForMatching(need)
    }

    var filterZone by remember { mutableStateOf<String?>(null) }
    var filterSpecialty by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pilotos Disponibles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Para: ${Catalog.cropLabel(need.cropCode)} - ${need.hectares} ha en ${Catalog.zoneLabel(need.zoneCode)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterZone != null,
                    onClick = { filterZone = if (filterZone == null) Catalog.Zone.COLCHAGUA else null },
                    label = { Text("Solo mi zona") }
                )
                FilterChip(
                    selected = filterSpecialty != null,
                    onClick = { filterSpecialty = if (filterSpecialty == null) need.cropCode else null },
                    label = { Text("Solo mi cultivo") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.applyFilters(filterZone, filterSpecialty) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar Filtros")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredResults) { result ->
                        MatchResultCard(
                            result = result,
                            hasActiveInterest = state.activeInterest?.pilotId == result.pilot.pilotId,
                            onSelectPilot = {
                                viewModel.manifestInterest(result.pilot.pilotId)
                            },
                            onCompleteService = {
                                viewModel.completeService()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchResultCard(
    result: MatchResult,
    hasActiveInterest: Boolean,
    onSelectPilot: () -> Unit,
    onCompleteService: () -> Unit
) {
    val borderColor = when (result.compatibility) {
        MatchResult.Compatibility.FULL -> Color(0xFF4CAF50)
        MatchResult.Compatibility.PARTIAL -> Color(0xFFFFC107)
        MatchResult.Compatibility.UNKNOWN -> Color(0xFF2196F3)
        MatchResult.Compatibility.INCOMPATIBLE -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = result.pilot.alias, style = MaterialTheme.typography.titleLarge)

                if (result.pilot.avgRating != null) {
                    Text(
                        text = "Valoracion: ${String.format("%.1f", result.pilot.avgRating)} (${result.pilot.ratingCount} opiniones)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(text = "Sin valoraciones", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (result.compatibility) {
                    MatchResult.Compatibility.FULL -> "[OK] Coincide en zona, cultivo y tiene capacidad."
                    MatchResult.Compatibility.PARTIAL -> "[Alerta] Coincide, pero la capacidad o fechas no son suficientes."
                    MatchResult.Compatibility.UNKNOWN -> "[Duda] Faltan datos para confirmar compatibilidad."
                    MatchResult.Compatibility.INCOMPATIBLE -> "[X] No cubre la zona o el cultivo."
                },
                color = borderColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Capacidad: ${result.pilot.hectaresPerHour} ha/h - ${result.pilot.workingHoursPerDay} h/dia")
            Text(text = "Requerido: ${String.format("%.1f", result.requiredHours)} horas de vuelo estimadas.")

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Botón para manifestar interés
                Button(
                    onClick = onSelectPilot,
                    enabled = !hasActiveInterest && result.compatibility != MatchResult.Compatibility.INCOMPATIBLE,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasActiveInterest) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (hasActiveInterest) "Piloto Seleccionado" else "Manifestar Interés")
                }

                // Botón para finalizar (Solo aparece si este es el piloto seleccionado)
                if (hasActiveInterest) {
                    OutlinedButton(
                        onClick = onCompleteService,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CAF50))
                    ) {
                        Text("Marcar servicio como finalizado")
                    }
                }
            }
        }
    }
}
