package cl.duoc.agromatch.ui.farmer

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import cl.duoc.agromatch.R
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.model.MAX_PHOTOS_PER_NEED
import cl.duoc.agromatch.model.Need
import cl.duoc.agromatch.model.AgroMatchValidator
import cl.duoc.agromatch.ui.epochDayFromPickerDate
import cl.duoc.agromatch.ui.formatEpochDay
import java.util.Calendar
import java.util.TimeZone
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeedFormScreen(
    farmerId: Long,
    isSaving: Boolean,
    error: String?,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit,
    onSave: (Need, List<Uri>) -> Unit
) {
    BackHandler(enabled = isSaving) { }

    var hectaresText by remember { mutableStateOf("") }
    var selectedCrop by remember { mutableStateOf(Catalog.Crop.VINEYARD) }
    var selectedZone by remember { mutableStateOf(Catalog.Zone.COLCHAGUA) }

    var startEpochDay by remember { mutableStateOf<Long?>(null) }
    var endEpochDay by remember { mutableStateOf<Long?>(null) }

    var showValidationError by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }
    val selectedPhotoUris = remember { mutableStateListOf<Uri>() }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var photoError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(MAX_PHOTOS_PER_NEED)
    ) { selectedUris ->
        photoError = null
        val remainingSlots = MAX_PHOTOS_PER_NEED - selectedPhotoUris.size
        val uniqueUris = selectedUris.distinct().filterNot(selectedPhotoUris::contains)
        uniqueUris.take(remainingSlots).forEach(selectedPhotoUris::add)
        if (uniqueUris.size > remainingSlots) {
            photoError = context.getString(R.string.need_photos_limit)
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val outputUri = pendingCameraUri
        val outputFile = pendingCameraFile
        if (success && outputUri != null && selectedPhotoUris.size < MAX_PHOTOS_PER_NEED) {
            selectedPhotoUris.add(outputUri)
            photoError = null
        } else {
            outputFile?.delete()
        }
        pendingCameraUri = null
        pendingCameraFile = null
    }

    fun startCameraCapture() {
        try {
            val cameraDirectory = File(context.cacheDir, "need_camera")
            check(cameraDirectory.exists() || cameraDirectory.mkdirs())
            val outputFile = File.createTempFile("capture_", ".jpg", cameraDirectory)
            val outputUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )
            pendingCameraUri = outputUri
            pendingCameraFile = outputFile
            cameraLauncher.launch(outputUri)
        } catch (_: ActivityNotFoundException) {
            pendingCameraFile?.delete()
            pendingCameraUri = null
            pendingCameraFile = null
            photoError = context.getString(R.string.need_photo_camera_unavailable)
        } catch (_: Exception) {
            pendingCameraFile?.delete()
            pendingCameraUri = null
            pendingCameraFile = null
            photoError = context.getString(R.string.need_photo_capture_error)
        }
    }

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
        containerColor = FarmerBackground,
        topBar = {
            TopAppBar(
                title = { Text("Nueva Necesidad", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !isSaving) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FarmerCharcoal)
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
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RectangleShape,
                    colors = farmerFieldColors()
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
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RectangleShape,
                    colors = farmerFieldColors()
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
                onValueChange = {
                    hectaresText = it
                    showValidationError = false
                },
                label = { Text("Hectáreas (ha)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = farmerFieldColors()
            )

            // Fechas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker(startEpochDay) { startEpochDay = it } },
                    modifier = Modifier.weight(1f),
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FarmerBlue)
                ) {
                    Text(startEpochDay?.let(::formatEpochDay) ?: "Fecha Inicio")
                }
                OutlinedButton(
                    onClick = { showDatePicker(endEpochDay) { endEpochDay = it } },
                    modifier = Modifier.weight(1f),
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FarmerBlue)
                ) {
                    Text(endEpochDay?.let(::formatEpochDay) ?: "Fecha Fin")
                }
            }

            Text(
                text = stringResource(R.string.need_photos_count, selectedPhotoUris.size, MAX_PHOTOS_PER_NEED),
                style = MaterialTheme.typography.titleSmall,
                color = FarmerCharcoal,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.need_photos_note),
                style = MaterialTheme.typography.bodySmall,
                color = FarmerCharcoal.copy(alpha = 0.7f)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    enabled = !isSaving && selectedPhotoUris.size < MAX_PHOTOS_PER_NEED,
                    modifier = Modifier.weight(1f),
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FarmerBlue)
                ) {
                    Text(stringResource(R.string.need_photos_gallery))
                }
                OutlinedButton(
                    onClick = ::startCameraCapture,
                    enabled = !isSaving && selectedPhotoUris.size < MAX_PHOTOS_PER_NEED,
                    modifier = Modifier.weight(1f),
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FarmerBlue)
                ) {
                    Text(stringResource(R.string.need_photos_camera))
                }
            }
            if (selectedPhotoUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(selectedPhotoUris.toList(), key = { _, uri -> uri.toString() }) { index, uri ->
                        Box(modifier = Modifier.size(88.dp)) {
                            NeedPhotoThumbnail(
                                uri = uri,
                                contentDescription = stringResource(R.string.need_photo_description, index + 1),
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { selectedPhotoUris.remove(uri) },
                                modifier = Modifier.align(Alignment.TopEnd).size(28.dp).background(FarmerCharcoal)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.need_photo_remove),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
            photoError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

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
                        onSave(newNeed, selectedPhotoUris.toList())
                    }
                },
                modifier = Modifier.fillMaxWidth()
                    .height(52.dp),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = FarmerBlue)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text("Guardar Necesidad")
            }
        }
    }
}

@Composable
private fun farmerFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FarmerBlue,
    focusedLabelColor = FarmerBlue,
    cursorColor = FarmerBlue
)
