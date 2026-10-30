package cl.duoc.agromatch.ui.farmer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.database.FarmerNeedOverview
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.model.InterestStatus
import cl.duoc.agromatch.model.Need
import cl.duoc.agromatch.ui.formatEpochDay
import cl.duoc.agromatch.viewmodel.FarmerViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
        containerColor = FarmerBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Terrenos",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    TextButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text("Salir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FarmerCharcoal)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateNeed,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva solicitud", fontWeight = FontWeight.SemiBold) },
                shape = RectangleShape,
                containerColor = FarmerBlue,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Solicitudes de servicio",
                    style = MaterialTheme.typography.titleMedium,
                    color = FarmerCharcoal,
                    fontWeight = FontWeight.Bold
                )
                if (state.needOverviews.isNotEmpty()) {
                    Text(
                        text = state.needOverviews.size.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = FarmerBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Consulta el estado de tus terrenos y servicios.",
                style = MaterialTheme.typography.bodySmall,
                color = FarmerCharcoal.copy(alpha = 0.68f)
            )
            Spacer(Modifier.height(16.dp))
            if (state.error != null) {
                Text(
                    text = state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
            }

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FarmerBlue)
                }

                state.needOverviews.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Todavía no tienes solicitudes",
                            style = MaterialTheme.typography.titleMedium,
                            color = FarmerCharcoal,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Crea una para encontrar un piloto para tu terreno.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FarmerCharcoal.copy(alpha = 0.68f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.needOverviews, key = { it.need.id }) { overview ->
                        NeedRow(
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
        }
    }
}

@Composable
private fun NeedRow(
    overview: FarmerNeedOverview,
    onClick: () -> Unit
) {
    val need = overview.need
    val isCompleted = overview.interestStatus == InterestStatus.COMPLETED.value
    val statusLabel = when (overview.interestStatus) {
        InterestStatus.ACTIVE.value -> "Solicitud enviada"
        InterestStatus.COMPLETED.value -> "Completada"
        else -> "Sin piloto seleccionado"
    }
    val statusColor = if (isCompleted) FarmerSuccess else FarmerBlue
    val statusBackground = if (isCompleted) FarmerSuccessSoft else FarmerBlueSoft

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        shape = RectangleShape,
        border = BorderStroke(1.dp, FarmerBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (isCompleted) FarmerSuccess else FarmerBlue)
            )
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fumigación de ${Catalog.cropLabel(need.cropCode)}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        color = FarmerCharcoal,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = statusLabel,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(statusBackground)
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${Catalog.zoneLabel(need.zoneCode)} · ${need.hectares} ha",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FarmerCharcoal.copy(alpha = 0.78f)
                )
                Text(
                    text = "${formatEpochDay(need.startEpochDay)} al ${formatEpochDay(need.endEpochDay)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FarmerCharcoal.copy(alpha = 0.65f)
                )
                if (isCompleted) {
                    Text(
                        text = overview.ratingScore?.let { "Valoración: $it/5" } ?: "Sin valoración",
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
