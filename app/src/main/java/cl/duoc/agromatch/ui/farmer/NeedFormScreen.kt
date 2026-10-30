package cl.duoc.agromatch.ui.farmer

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.model.Need
import cl.duoc.agromatch.model.AgroMatchValidator
import cl.duoc.agromatch.ui.epochDayFromPickerDate
import cl.duoc.agromatch.ui.formatEpochDay
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeedFormScreen(
    farmerId: Long,
    isSaving: Boolean,
    error: String?,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit,
    onSave: (Need) -> Unit
) {
    BackHandler(enabled = isSaving) { }

    var hectaresText by remember { mutableStateOf("") }
    var selectedCrop by remember { mutableStateOf(Catalog.Crop.VINEYARD) }
    var selectedZone by remember { mutableStateOf(Catalog.Zone.COLCHAGUA) }

    var startEpochDay by remember { mutableStateOf<Long?>(null) }
    var endEpochDay by remember { mutableStateOf<Long?>(null) }

    var showValidationError by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }

    val context = LocalContext.current
    fun showDatePicker(initialEpochDay: Long?, onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        initialEpochDay?.let { calendar.timeInMillis = it * 86_400_000L }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(epochDayFromPickerDate(year, month, dayOfMonth))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Necesidad") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !isSaving) {
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cultivo
            var cropExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = cropExpanded,
                onExpandedChange = { cropExpanded = !cropExpanded }
            ) {
                OutlinedTextField(
                    value = Catalog.cropLabel(selectedCrop),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cultivo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = cropExpanded,
                    onDismissRequest = { cropExpanded = false }
                ) {
                    Catalog.Crop.all.forEach { crop ->
                        DropdownMenuItem(
                            text = { Text(Catalog.cropLabel(crop)) },
                            onClick = {
                                selectedCrop = crop
                                cropExpanded = false
                            }
                        )
                    }
                }
            }

            // Zona
            var zoneExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = zoneExpanded,
                onExpandedChange = { zoneExpanded = !zoneExpanded }
            ) {
                OutlinedTextField(
                    value = Catalog.zoneLabel(selectedZone),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Zona") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = zoneExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = zoneExpanded,
                    onDismissRequest = { zoneExpanded = false }
                ) {
                    Catalog.Zone.all.forEach { zone ->
                        DropdownMenuItem(
                            text = { Text(Catalog.zoneLabel(zone)) },
                            onClick = {
                                selectedZone = zone
                                zoneExpanded = false
                            }
                        )
                    }
                }
            }

            // Hectáreas
            OutlinedTextField(
                value = hectaresText,
                onValueChange = { hectaresText = it },
                label = { Text("Hectáreas (ha)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Fechas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker(startEpochDay) { startEpochDay = it } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(startEpochDay?.let(::formatEpochDay) ?: "Fecha Inicio")
                }
                OutlinedButton(
                    onClick = { showDatePicker(endEpochDay) { endEpochDay = it } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(endEpochDay?.let(::formatEpochDay) ?: "Fecha Fin")
                }
            }

            if (showValidationError) {
                Text(text = validationError, color = MaterialTheme.colorScheme.error)
            }
            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

            Button(
                enabled = !isSaving,
                onClick = {
                    onClearError()
                    val hectares = hectaresText.replace(',', '.').toDoubleOrNull()
                    val startDay = startEpochDay
                    val endDay = endEpochDay
                    if (hectares == null || startDay == null || endDay == null) {
                        showValidationError = true
                        validationError = "Por favor completa todos los campos correctamente."
                        return@Button
                    }

                    val newNeed = Need(
                        farmerId = farmerId,
                        cropCode = selectedCrop,
                        zoneCode = selectedZone,
                        hectares = hectares,
                        startEpochDay = startDay,
                        endEpochDay = endDay
                    )

                    val errors = AgroMatchValidator.validateNeed(newNeed)
                    if (errors.isNotEmpty()) {
                        showValidationError = true
                        validationError = "Revisa las fechas y las hectáreas ingresadas."
                    } else {
                        showValidationError = false
                        onSave(newNeed)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Guardar Necesidad")
            }
        }
    }
}
