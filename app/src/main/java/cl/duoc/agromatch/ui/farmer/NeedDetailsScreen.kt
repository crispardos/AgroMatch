package cl.duoc.agromatch.ui.farmer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.database.FarmerNeedOverview
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.model.InterestStatus
import cl.duoc.agromatch.ui.formatEpochDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeedDetailsScreen(
    overview: FarmerNeedOverview,
    onNavigateBack: () -> Unit,
    onFindAnotherPilot: () -> Unit
) {
    val need = overview.need
    val isCompleted = overview.interestStatus == InterestStatus.COMPLETED.value
    val statusLabel = when (overview.interestStatus) {
        InterestStatus.ACTIVE.value -> "Solicitud enviada"
        InterestStatus.COMPLETED.value -> "Completada"
        else -> "Sin piloto seleccionado"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del terreno") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Fumigación de ${Catalog.cropLabel(need.cropCode)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("Estado: $statusLabel", style = MaterialTheme.typography.titleMedium)
            Text("Zona: ${Catalog.zoneLabel(need.zoneCode)}")
            Text("Superficie: ${need.hectares} ha")
            Text("Fechas: ${formatEpochDay(need.startEpochDay)} al ${formatEpochDay(need.endEpochDay)}")

            Spacer(Modifier.height(4.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(if (isCompleted) "Servicio realizado" else "Solicitud de servicio", style = MaterialTheme.typography.titleMedium)
                    overview.pilotAlias?.let { Text("Piloto: $it") }
                    if (isCompleted && overview.ratingScore != null) {
                        Text("Valoración: ${overview.ratingScore}/5")
                        Text("Comentario: ${overview.ratingComment?.takeIf { it.isNotBlank() } ?: "Sin comentario"}")
                    } else if (isCompleted) {
                        Text("Este servicio aún no tiene valoración.")
                    }
                }
            }

            OutlinedButton(onClick = onFindAnotherPilot, modifier = Modifier.fillMaxWidth()) {
                Text("Buscar otro piloto")
            }
        }
    }
}
