package cl.duoc.agromatch.ui.farmer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.database.FarmerNeedOverview
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.model.InterestStatus
import cl.duoc.agromatch.ui.formatEpochDay
import android.net.Uri
import java.io.File

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
        InterestStatus.ACCEPTED.value -> "Piloto aceptó"
        InterestStatus.COMPLETED.value -> "Completada"
        else -> "Sin piloto seleccionado"
    }
    val statusColor = if (isCompleted) FarmerSuccess else FarmerBlue
    val statusBackground = if (isCompleted) FarmerSuccessSoft else FarmerBlueSoft

    Scaffold(
        containerColor = FarmerBackground,
        topBar = {
            TopAppBar(
                title = { Text("Detalle del terreno", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FarmerCharcoal)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Fumigación de ${Catalog.cropLabel(need.cropCode)}",
                style = MaterialTheme.typography.headlineSmall,
                color = FarmerCharcoal,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = statusLabel,
                modifier = Modifier.background(statusBackground).padding(horizontal = 8.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelLarge,
                color = statusColor,
                fontWeight = FontWeight.SemiBold
            )
            Text("Zona: ${Catalog.zoneLabel(need.zoneCode)}")
            Text("Superficie: ${need.hectares} ha")
            Text("Fechas: ${formatEpochDay(need.startEpochDay)} al ${formatEpochDay(need.endEpochDay)}")

            val photoPaths = overview.photoPaths.orEmpty().split('|').filter(String::isNotBlank)
            if (photoPaths.isNotEmpty()) {
                Text("Fotos del terreno", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(photoPaths) { index, path ->
                        NeedPhotoThumbnail(
                            uri = Uri.fromFile(File(path)),
                            contentDescription = "Foto del terreno ${index + 1}",
                            modifier = Modifier.size(width = 192.dp, height = 132.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                color = Color.White,
                border = BorderStroke(1.dp, FarmerBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        if (isCompleted) "Servicio realizado" else "Solicitud de servicio",
                        style = MaterialTheme.typography.titleMedium,
                        color = FarmerCharcoal,
                        fontWeight = FontWeight.SemiBold
                    )
                    overview.pilotAlias?.let { Text("Piloto: $it") }
                    if (isCompleted && overview.ratingScore != null) {
                        Text("Valoración: ${overview.ratingScore}/5")
                        Text("Comentario: ${overview.ratingComment?.takeIf { it.isNotBlank() } ?: "Sin comentario"}")
                    } else if (isCompleted) {
                        Text("Este servicio aún no tiene valoración.")
                    }
                }
            }

            OutlinedButton(
                onClick = onFindAnotherPilot,
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FarmerBlue)
            ) {
                Text("Buscar otro piloto")
            }
        }
    }
}
