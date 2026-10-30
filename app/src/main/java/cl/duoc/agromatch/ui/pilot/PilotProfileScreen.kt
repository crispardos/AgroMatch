package cl.duoc.agromatch.ui.pilot

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.R
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.repository.AuthFailure
import cl.duoc.agromatch.ui.formatIsoDate
import cl.duoc.agromatch.ui.isoDateFromPickerDate
import cl.duoc.agromatch.ui.parseIsoDateToEpochDay
import cl.duoc.agromatch.viewmodel.AuthUiState
import cl.duoc.agromatch.viewmodel.PilotUiState
import java.util.Calendar
import java.util.TimeZone

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PilotProfileScreen(
    state: PilotUiState,
    authState: AuthUiState,
    onUpdateAlias: (String) -> Unit,
    onClearAliasFeedback: () -> Unit,
    onExperienceChange: (String) -> Unit,
    onHectaresChange: (String) -> Unit,
    onWorkingHoursChange: (String) -> Unit,
    onAvailabilityFromChange: (String?) -> Unit,
    onAvailabilityToChange: (String?) -> Unit,
    onClearAvailability: () -> Unit,
    onToggleZone: (String) -> Unit,
    onToggleSpecialty: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val user = authState.user ?: return
    val context = LocalContext.current
    var alias by rememberSaveable(user.id) { mutableStateOf(user.alias) }
    val enabled = !state.isLoading && !state.isWorking && !authState.isWorking

    Scaffold(
        containerColor = PilotBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.pilot_profile_title),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = enabled) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.pilot_back),
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it; onClearAliasFeedback() },
                label = { Text(stringResource(R.string.alias_label)) },
                singleLine = true,
                enabled = enabled,
                isError = authState.error != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = pilotFieldColors()
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
            HorizontalDivider(color = PilotBorder)

            PilotSectionTitle(stringResource(R.string.pilot_service))
            OutlinedTextField(
                value = state.experienceYears,
                onValueChange = onExperienceChange,
                label = { Text(stringResource(R.string.pilot_experience_label)) },
                isError = state.experienceError,
                supportingText = {
                    if (state.experienceError) Text(stringResource(R.string.pilot_experience_error))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = pilotFieldColors()
            )
            OutlinedTextField(
                value = state.hectaresPerHour,
                onValueChange = onHectaresChange,
                label = { Text(stringResource(R.string.pilot_hectares_label)) },
                isError = state.hectaresError,
                supportingText = {
                    if (state.hectaresError) Text(stringResource(R.string.pilot_positive_error))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = pilotFieldColors()
            )
            OutlinedTextField(
                value = state.workingHoursPerDay,
                onValueChange = onWorkingHoursChange,
                label = { Text(stringResource(R.string.pilot_hours_label)) },
                isError = state.workingHoursError,
                supportingText = {
                    if (state.workingHoursError) Text(stringResource(R.string.pilot_positive_error))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = pilotFieldColors()
            )

            PilotSectionTitle(stringResource(R.string.pilot_availability))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        showAvailabilityPicker(context, state.availableFrom) { onAvailabilityFromChange(it) }
                    },
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PilotBlue)
                ) {
                    Text(
                        "${stringResource(R.string.pilot_availability_start)}: " +
                            (state.availableFrom?.let(::formatIsoDate) ?: stringResource(R.string.pilot_availability_choose))
                    )
                }
                OutlinedButton(
                    onClick = {
                        showAvailabilityPicker(context, state.availableTo) { onAvailabilityToChange(it) }
                    },
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PilotBlue)
                ) {
                    Text(
                        "${stringResource(R.string.pilot_availability_end)}: " +
                            (state.availableTo?.let(::formatIsoDate) ?: stringResource(R.string.pilot_availability_choose))
                    )
                }
            }
            if (state.availableFrom != null || state.availableTo != null) {
                TextButton(onClick = onClearAvailability, enabled = enabled) {
                    Text(stringResource(R.string.pilot_availability_clear), color = PilotBlue)
                }
            }
            if (state.availabilityError) {
                Text(stringResource(R.string.pilot_availability_error), color = MaterialTheme.colorScheme.error)
            }
            Text(stringResource(R.string.pilot_calendar_note), style = MaterialTheme.typography.bodySmall, color = PilotCharcoal.copy(alpha = 0.7f))
            HorizontalDivider(color = PilotBorder)

            PilotSectionTitle(stringResource(R.string.pilot_coverage_title))
            (Catalog.Zone.all + state.selectedZones).distinct().forEach { code ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = code in state.selectedZones,
                        enabled = enabled,
                        onCheckedChange = { onToggleZone(code) },
                        colors = CheckboxDefaults.colors(checkedColor = PilotBlue)
                    )
                    Text(Catalog.zoneLabel(code), color = PilotCharcoal)
                }
            }
            PilotSectionTitle(stringResource(R.string.pilot_specialties_title))
            (Catalog.Crop.all + state.selectedSpecialties).distinct().forEach { code ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = code in state.selectedSpecialties,
                        enabled = enabled,
                        onCheckedChange = { onToggleSpecialty(code) },
                        colors = CheckboxDefaults.colors(checkedColor = PilotBlue)
                    )
                    Text(Catalog.cropLabel(code), color = PilotCharcoal)
                }
            }
            Text(stringResource(R.string.pilot_lists_note), style = MaterialTheme.typography.bodySmall, color = PilotCharcoal.copy(alpha = 0.7f))
            HorizontalDivider(color = PilotBorder)

            Text(stringResource(R.string.pilot_alias_note), style = MaterialTheme.typography.bodySmall, color = PilotCharcoal.copy(alpha = 0.7f))
            OutlinedButton(
                onClick = { onUpdateAlias(alias) },
                enabled = enabled && alias.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PilotBlue)
            ) {
                Text(stringResource(R.string.pilot_save_user))
            }
            if (authState.aliasSaved) Text(stringResource(R.string.pilot_alias_saved), color = PilotSuccess)
            if (authState.isWorking) CircularProgressIndicator(color = PilotBlue)
            state.error?.let { Text(pilotErrorText(it), color = MaterialTheme.colorScheme.error) }
            if (state.profileSaved) Text(stringResource(R.string.pilot_profile_saved), color = PilotSuccess)
            if (state.isWorking || state.isLoading) CircularProgressIndicator(color = PilotBlue)
            Button(
                onClick = onSave,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PilotBlue)
            ) {
                Text(stringResource(R.string.pilot_save_profile))
            }
        }
    }
}

@Composable
private fun PilotSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = PilotCharcoal,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun pilotFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PilotBlue,
    focusedLabelColor = PilotBlue,
    cursorColor = PilotBlue,
    errorBorderColor = MaterialTheme.colorScheme.error,
    errorLabelColor = MaterialTheme.colorScheme.error
)

private fun showAvailabilityPicker(
    context: Context,
    currentIsoDate: String?,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    parseIsoDateToEpochDay(currentIsoDate)?.let { calendar.timeInMillis = it * 86_400_000L }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(isoDateFromPickerDate(year, month, dayOfMonth))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
