package cl.duoc.agromatch.ui.pilot

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import cl.duoc.agromatch.R
import cl.duoc.agromatch.database.PilotRequestSummary
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.model.InterestStatus
import cl.duoc.agromatch.ui.formatEpochDay
import cl.duoc.agromatch.ui.farmer.NeedPhotoThumbnail
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PilotRequestDetailsScreen(
    request: PilotRequestSummary,
    isWorking: Boolean,
    errorMessage: String?,
    onNavigateBack: () -> Unit,
    onAcceptRequest: () -> Unit
) {
    val isAccepted = request.status == InterestStatus.ACCEPTED.value
    val photoPaths = request.photoPaths.orEmpty().split('|').filter(String::isNotBlank)

    Scaffold(
        containerColor = PilotBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.pilot_request_detail_title),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !isWorking) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = androidx.compose.ui.res.stringResource(R.string.pilot_back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PilotCharcoal)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.pilot_request_from, request.farmerAlias),
                style = MaterialTheme.typography.headlineSmall,
                color = PilotCharcoal,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RectangleShape,
                color = if (isAccepted) PilotSuccess.copy(alpha = 0.1f) else PilotBlueSoft,
                border = BorderStroke(1.dp, if (isAccepted) PilotSuccess else PilotBlue)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        if (isAccepted) R.string.pilot_request_accepted else R.string.pilot_request_pending
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    color = if (isAccepted) PilotSuccess else PilotBlue,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                color = Color.White,
                border = BorderStroke(1.dp, PilotBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text(
                        text = "Fumigación de ${Catalog.cropLabel(request.cropCode)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = PilotCharcoal,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Zona: ${Catalog.zoneLabel(request.zoneCode)}")
                    Text("Superficie: ${request.hectares} ha")
                    Text("Fechas solicitadas: ${formatEpochDay(request.startEpochDay)} al ${formatEpochDay(request.endEpochDay)}")
                    Text("Solicitud recibida: ${formatEpochDay(request.createdEpochDay)}")
                }
            }

            Text(
                text = androidx.compose.ui.res.stringResource(R.string.pilot_request_photos),
                style = MaterialTheme.typography.titleMedium,
                color = PilotCharcoal,
                fontWeight = FontWeight.Bold
            )
            if (photoPaths.isEmpty()) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.pilot_request_no_photos),
                    color = PilotCharcoal.copy(alpha = 0.7f)
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(photoPaths) { index, path ->
                        NeedPhotoThumbnail(
                            uri = Uri.fromFile(File(path)),
                            contentDescription = "Foto del terreno ${index + 1}",
                            modifier = Modifier.height(150.dp).fillMaxWidth(0.62f)
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
            if (isAccepted) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.pilot_request_accepted),
                    color = PilotSuccess,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Button(
                    onClick = onAcceptRequest,
                    enabled = !isWorking,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PilotBlue)
                ) {
                    Text(
                        if (isWorking) "Procesando…"
                        else androidx.compose.ui.res.stringResource(R.string.pilot_request_accept)
                    )
                }
            }
            if (isWorking) {
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}
