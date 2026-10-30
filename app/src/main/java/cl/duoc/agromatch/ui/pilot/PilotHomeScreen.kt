package cl.duoc.agromatch.ui.pilot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.R
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.repository.PilotFailure
import cl.duoc.agromatch.ui.formatEpochDay
import cl.duoc.agromatch.ui.formatIsoDate
import cl.duoc.agromatch.viewmodel.PilotUiState

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PilotHomeScreen(
    alias: String,
    state: PilotUiState,
    onEditProfile: () -> Unit,
    onOpenRequest: (Long) -> Unit,
    onRetry: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor = PilotBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.role_pilot_home),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    TextButton(
                        onClick = onLogout,
                        enabled = !state.isWorking,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text(stringResource(R.string.logout_action))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PilotCharcoal)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PilotBackground)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                stringResource(R.string.welcome_alias, alias),
                style = MaterialTheme.typography.headlineSmall,
                color = PilotCharcoal,
                fontWeight = FontWeight.Bold
            )
            when {
                state.isLoading -> CircularProgressIndicator(color = PilotBlue)
                state.error != null -> {
                    Text(pilotErrorText(state.error), color = MaterialTheme.colorScheme.error)
                    Button(
                        onClick = onRetry,
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PilotBlue)
                    ) {
                        Text(stringResource(R.string.pilot_retry))
                    }
                }
                else -> {
                    val profile = state.profile
                    if (profile != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RectangleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, PilotBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    stringResource(R.string.pilot_profile_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PilotCharcoal,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(stringResource(R.string.pilot_service), color = PilotCharcoal)
                                Text(stringResource(R.string.pilot_experience_summary, profile.experienceYears ?: 0))
                                Text(stringResource(R.string.pilot_capacity_summary, profile.hectaresPerHour, profile.workingHoursPerDay))
                                Text(
                                    if (profile.availableFrom == null && profile.availableTo == null) {
                                        stringResource(R.string.pilot_availability_unknown)
                                    } else {
                                        stringResource(
                                            R.string.pilot_availability_range,
                                            profile.availableFrom?.let(::formatIsoDate) ?: "Sin fecha inicial",
                                            profile.availableTo?.let(::formatIsoDate) ?: "Sin fecha final"
                                        )
                                    }
                                )
                                val zones = state.coverages.joinToString { Catalog.zoneLabel(it.zoneCode) }
                                val specialties = state.specialties.joinToString { Catalog.cropLabel(it.specialtyCode) }
                                Text(stringResource(R.string.pilot_coverage_summary, zones.ifBlank { stringResource(R.string.pilot_pending) }))
                                Text(stringResource(R.string.pilot_specialties_summary, specialties.ifBlank { stringResource(R.string.pilot_pending) }))
                            }
                        }
                    }

                    Button(
                        onClick = onEditProfile,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PilotBlue)
                    ) {
                        Text(stringResource(if (profile == null) R.string.pilot_create_profile else R.string.pilot_edit_profile))
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, PilotBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    stringResource(R.string.pilot_requests_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PilotCharcoal,
                                    fontWeight = FontWeight.Bold
                                )
                                if (state.requests.isNotEmpty()) {
                                    Text(
                                        state.requests.size.toString(),
                                        color = PilotBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (state.requests.isEmpty()) {
                                Text(stringResource(R.string.pilot_no_requests), color = PilotCharcoal.copy(alpha = 0.72f))
                            } else {
                                state.requests.forEach { request ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().clickable(enabled = !state.isWorking) {
                                            onOpenRequest(request.interestId)
                                        },
                                        shape = RectangleShape,
                                        color = PilotBackground,
                                        border = BorderStroke(1.dp, PilotBorder)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                stringResource(R.string.pilot_request_from, request.farmerAlias),
                                                style = MaterialTheme.typography.titleSmall,
                                                color = PilotCharcoal,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(stringResource(
                                                R.string.pilot_request_need,
                                                Catalog.cropLabel(request.cropCode),
                                                request.hectares,
                                                Catalog.zoneLabel(request.zoneCode)
                                            ))
                                            Text(stringResource(
                                                R.string.pilot_request_dates,
                                                formatEpochDay(request.startEpochDay),
                                                formatEpochDay(request.endEpochDay)
                                            ), style = MaterialTheme.typography.bodySmall)
                                            Text(
                                                stringResource(
                                                    if (request.status == cl.duoc.agromatch.model.InterestStatus.ACCEPTED.value) {
                                                        R.string.pilot_request_accepted
                                                    } else {
                                                        R.string.pilot_request_pending
                                                    }
                                                ),
                                                color = if (request.status == cl.duoc.agromatch.model.InterestStatus.ACCEPTED.value) {
                                                    PilotSuccess
                                                } else {
                                                    PilotBlue
                                                },
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                stringResource(R.string.pilot_request_open),
                                                color = PilotBlue,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Text(stringResource(R.string.pilot_directory_note), style = MaterialTheme.typography.bodySmall, color = PilotCharcoal.copy(alpha = 0.68f))
                    Text(stringResource(R.string.pilot_ratings_pending), style = MaterialTheme.typography.bodySmall, color = PilotCharcoal.copy(alpha = 0.68f))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
internal fun pilotErrorText(error: PilotFailure): String = stringResource(
    when (error) {
        PilotFailure.INVALID_PROFILE -> R.string.pilot_error_invalid
        PilotFailure.USER_NOT_FOUND, PilotFailure.USER_NOT_PILOT,
        PilotFailure.SESSION_REQUIRED -> R.string.pilot_error_session
        PilotFailure.REQUEST_NOT_AVAILABLE -> R.string.pilot_request_unavailable
        PilotFailure.STORAGE_ERROR -> R.string.pilot_error_storage
    }
)
