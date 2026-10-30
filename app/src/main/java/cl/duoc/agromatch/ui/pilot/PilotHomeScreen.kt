package cl.duoc.agromatch.ui.pilot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.R
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.repository.PilotFailure
import cl.duoc.agromatch.ui.formatEpochDay
import cl.duoc.agromatch.ui.formatIsoDate
import cl.duoc.agromatch.viewmodel.PilotUiState

@Composable
fun PilotHomeScreen(
    alias: String,
    state: PilotUiState,
    onEditProfile: () -> Unit,
    onRetry: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.role_pilot_home), style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onLogout, enabled = !state.isWorking) {
                    Text(stringResource(R.string.logout_action))
                }
            }
            Text(stringResource(R.string.welcome_alias, alias), style = MaterialTheme.typography.headlineSmall)
            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> {
                    Text(pilotErrorText(state.error), color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetry) { Text(stringResource(R.string.pilot_retry)) }
                }
                else -> {
                    val profile = state.profile
                    if (profile != null) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(stringResource(R.string.pilot_profile_title), style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.pilot_service))
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
                                Text(stringResource(R.string.pilot_coverage_summary,
                                    zones.ifBlank { stringResource(R.string.pilot_pending) }))
                                Text(stringResource(R.string.pilot_specialties_summary,
                                    specialties.ifBlank { stringResource(R.string.pilot_pending) }))
                            }
                        }
                    }
                    Button(onClick = onEditProfile, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(if (profile == null) R.string.pilot_create_profile else R.string.pilot_edit_profile))
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(stringResource(R.string.pilot_requests_title), style = MaterialTheme.typography.titleMedium)
                            if (state.requests.isEmpty()) {
                                Text(stringResource(R.string.pilot_no_requests))
                            } else {
                                state.requests.forEach { request ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                stringResource(R.string.pilot_request_from, request.farmerAlias),
                                                style = MaterialTheme.typography.titleSmall
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
                                            ))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Text(stringResource(R.string.pilot_directory_note), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.pilot_ratings_pending), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
internal fun pilotErrorText(error: PilotFailure): String = stringResource(
    when (error) {
        PilotFailure.INVALID_PROFILE -> R.string.pilot_error_invalid
        PilotFailure.USER_NOT_FOUND, PilotFailure.USER_NOT_PILOT,
        PilotFailure.SESSION_REQUIRED -> R.string.pilot_error_session
        PilotFailure.STORAGE_ERROR -> R.string.pilot_error_storage
    }
)
