package cl.duoc.agromatch.ui.pilot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.R
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.repository.AuthFailure
import cl.duoc.agromatch.viewmodel.AuthUiState
import cl.duoc.agromatch.viewmodel.PilotUiState

@Composable
fun PilotProfileScreen(
    state: PilotUiState,
    authState: AuthUiState,
    onUpdateAlias: (String) -> Unit,
    onClearAliasFeedback: () -> Unit,
    onExperienceChange: (String) -> Unit,
    onHectaresChange: (String) -> Unit,
    onWorkingHoursChange: (String) -> Unit,
    onToggleZone: (String) -> Unit,
    onToggleSpecialty: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val user = authState.user ?: return
    var alias by rememberSaveable(user.id) { mutableStateOf(user.alias) }
    val enabled = !state.isLoading && !state.isWorking && !authState.isWorking

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onBack, enabled = enabled) { Text(stringResource(R.string.pilot_back)) }
            Text(stringResource(R.string.pilot_profile_title), style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it; onClearAliasFeedback() },
                label = { Text(stringResource(R.string.alias_label)) },
                singleLine = true, enabled = enabled,
                isError = authState.error != null,
                modifier = Modifier.fillMaxWidth()
            )
            authState.error?.let { error ->
                val message = when (error) {
                    AuthFailure.ALIAS_ALREADY_EXISTS -> R.string.auth_error_alias_taken
                    AuthFailure.INVALID_FIELDS -> R.string.auth_error_invalid_fields
                    AuthFailure.INVALID_CREDENTIALS -> R.string.pilot_error_session
                    AuthFailure.STORAGE_ERROR -> R.string.auth_error_storage
                }
                Text(stringResource(message), color = MaterialTheme.colorScheme.error)
            }
            HorizontalDivider()

            Text(stringResource(R.string.pilot_service), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.experienceYears, onValueChange = onExperienceChange,
                label = { Text(stringResource(R.string.pilot_experience_label)) },
                isError = state.experienceError,
                supportingText = {
                    if (state.experienceError) Text(stringResource(R.string.pilot_experience_error))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.hectaresPerHour, onValueChange = onHectaresChange,
                label = { Text(stringResource(R.string.pilot_hectares_label)) },
                isError = state.hectaresError,
                supportingText = {
                    if (state.hectaresError) Text(stringResource(R.string.pilot_positive_error))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.workingHoursPerDay, onValueChange = onWorkingHoursChange,
                label = { Text(stringResource(R.string.pilot_hours_label)) },
                isError = state.workingHoursError,
                supportingText = {
                    if (state.workingHoursError) Text(stringResource(R.string.pilot_positive_error))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.pilot_availability))
            Text(stringResource(R.string.pilot_calendar_note), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider()

            Text(stringResource(R.string.pilot_coverage_title), style = MaterialTheme.typography.titleMedium)
            (Catalog.Zone.all + state.selectedZones).distinct().forEach { code ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = code in state.selectedZones, enabled = enabled,
                        onCheckedChange = { onToggleZone(code) })
                    Text(Catalog.zoneLabel(code))
                }
            }
            Text(stringResource(R.string.pilot_specialties_title), style = MaterialTheme.typography.titleMedium)
            (Catalog.Crop.all + state.selectedSpecialties).distinct().forEach { code ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = code in state.selectedSpecialties, enabled = enabled,
                        onCheckedChange = { onToggleSpecialty(code) })
                    Text(Catalog.cropLabel(code))
                }
            }
            Text(stringResource(R.string.pilot_lists_note), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider()
            Text(stringResource(R.string.pilot_alias_note), style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = { onUpdateAlias(alias) },
                enabled = enabled && alias.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.pilot_save_user))
            }
            if (authState.aliasSaved) Text(stringResource(R.string.pilot_alias_saved))
            if (authState.isWorking) CircularProgressIndicator()
            state.error?.let { Text(pilotErrorText(it), color = MaterialTheme.colorScheme.error) }
            if (state.profileSaved) Text(stringResource(R.string.pilot_profile_saved))
            if (state.isWorking || state.isLoading) CircularProgressIndicator()
            Button(onClick = onSave, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pilot_save_profile))
            }
        }
    }
}
