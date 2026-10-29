package cl.duoc.agromatch.ui.farmer

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeedFormScreen(
    farmerId: Long,
    onNavigateBack: () -> Unit,
    onSave: (Need) -> Unit
) {
    var hectaresText by remember { mutableStateOf("") }
    var selectedCrop by remember { mutableStateOf(Catalog.Crop.VINEYARD) }
    var selectedZone by remember { mutableStateOf(Catalog.Zone.COLCHAGUA) }

    // Fechas en milisegundos (Long)
    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Convertir de milisegundos a epochDay para la base de datos
    fun toEpochDay(millis: Long?): Long {
        return millis?.let { it / 86_400_000L } ?: -1L
    }

    fun showDatePicker(initialMillis: Long?, onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        initialMillis?.let { calendar.timeInMillis = it }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance()
                selected.set(year, month, dayOfMonth)
                onDateSelected(selected.timeInMillis)
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
                    onClick = { showDatePicker(startDateMillis) { startDateMillis = it } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(startDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Fecha Inicio")
                }
                OutlinedButton(
                    onClick = { showDatePicker(endDateMillis) { endDateMillis = it } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(endDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Fecha Fin")
                }
            }

            if (showError) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val hectares = hectaresText.replace(',', '.').toDoubleOrNull()
                    if (hectares == null || startDateMillis == null || endDateMillis == null) {
                        showError = true
                        errorMessage = "Por favor completa todos los campos correctamente."
                        return@Button
                    }

                    val newNeed = Need(
                        farmerId = farmerId,
                        cropCode = selectedCrop,
                        zoneCode = selectedZone,
                        hectares = hectares,
                        startEpochDay = toEpochDay(startDateMillis),
                        endEpochDay = toEpochDay(endDateMillis)
                    )

                    val errors = AgroMatchValidator.validateNeed(newNeed)
                    if (errors.isNotEmpty()) {
                        showError = true
                        errorMessage = "Revisa las fechas y las hectáreas ingresadas."
                    } else {
                        onSave(newNeed)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Necesidad")
            }
        }
    }
}