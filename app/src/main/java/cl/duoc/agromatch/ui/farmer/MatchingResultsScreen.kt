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
    var pilotToReplace by remember { mutableStateOf<MatchResult?>(null) }

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
                    onClick = { filterZone = if (filterZone == null) need.zoneCode else null },
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
                if (state.error != null) {
                    Text(
                        text = state.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                val currentInterest = state.activeInterest
                if (currentInterest?.status == InterestStatus.ACTIVE.value) {
                    val selectedAlias = state.matchResults
                        .firstOrNull { it.pilot.pilotId == currentInterest.pilotId }
                        ?.pilot?.alias ?: "el piloto seleccionado"
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Interés activo con $selectedAlias. Si eliges otro piloto, cambiarás la selección.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else if (currentInterest?.status == InterestStatus.COMPLETED.value) {
                    val completedPilotAlias = state.matchResults
                        .firstOrNull { it.pilot.pilotId == currentInterest.pilotId }
                        ?.pilot?.alias ?: "el piloto"
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Servicio completado con $completedPilotAlias.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (state.hasRating) {
                                Text("Ya valoraste este servicio.")
                            } else {
                                OutlinedButton(
                                    onClick = { onNavigateToRating(currentInterest.id, completedPilotAlias) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Valorar servicio")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.filteredResults.isEmpty()) {
                        item {
                            Text(
                                text = if (state.matchResults.isEmpty()) {
                                    "Aún no hay pilotos con perfil de servicio registrado."
                                } else {
                                    "No hay pilotos que coincidan con estos filtros."
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    items(state.filteredResults) { result ->
                        val interest = state.activeInterest
                        val isSelected = interest?.status == InterestStatus.ACTIVE.value &&
                            interest.pilotId == result.pilot.pilotId
                        val isCompleted = interest?.status == InterestStatus.COMPLETED.value &&
                            interest.pilotId == result.pilot.pilotId
                        MatchResultCard(
                            result = result,
                            isSelected = isSelected,
                            isCompleted = isCompleted,
                            hasOtherActiveSelection = interest?.status == InterestStatus.ACTIVE.value && !isSelected,
                            onSelectPilot = {
                                if (interest?.status == InterestStatus.ACTIVE.value && !isSelected) {
                                    pilotToReplace = result
                                } else {
                                    viewModel.manifestInterest(result.pilot.pilotId)
                                }
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

    pilotToReplace?.let { result ->
        AlertDialog(
            onDismissRequest = { pilotToReplace = null },
            title = { Text("Cambiar piloto seleccionado") },
            text = { Text("El interés activo se reemplazará por una solicitud a ${result.pilot.alias}. ¿Quieres continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    pilotToReplace = null
                    viewModel.manifestInterest(result.pilot.pilotId)
                }) { Text("Cambiar piloto") }
            },
            dismissButton = {
                TextButton(onClick = { pilotToReplace = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun MatchResultCard(
    result: MatchResult,
    isSelected: Boolean,
    isCompleted: Boolean,
    hasOtherActiveSelection: Boolean,
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
                    MatchResult.Compatibility.UNKNOWN -> if (!result.availabilityKnown) {
                        "[Duda] El piloto aún no informó su disponibilidad."
                    } else {
                        "[Duda] Faltan datos para confirmar compatibilidad."
                    }
                    MatchResult.Compatibility.INCOMPATIBLE -> "[X] No cubre la zona o el cultivo."
                },
                color = borderColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Capacidad: ${result.pilot.hectaresPerHour} ha/h - ${result.pilot.workingHoursPerDay} h/dia")
            Text(text = "Requerido: ${String.format("%.1f", result.requiredHours)} horas de vuelo estimadas.")
            if (!result.availabilityKnown) {
                Text("Disponibilidad: por confirmar")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Botón para manifestar interés
                if (isCompleted) {
                    Text(
                        text = "Servicio completado",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Button(
                        onClick = onSelectPilot,
                        enabled = !isSelected && result.compatibility != MatchResult.Compatibility.INCOMPATIBLE,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(when {
                            isSelected -> "Piloto seleccionado"
                            hasOtherActiveSelection -> "Cambiar piloto"
                            else -> "Manifestar interés"
                        })
                    }
                }

                if (isSelected) {
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
