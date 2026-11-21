package cl.duoc.agromatch.ui.farmer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.remote.NeedRemote
import cl.duoc.agromatch.remote.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun CloudSyncCard(farmerId: Int) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var hectaresText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("☁️ Conexión con la Nube (Xano)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Botón GET
            Button(
                onClick = {
                    isLoading = true
                    statusMessage = null
                    scope.launch {
                        try {
                            val needs = RetrofitInstance.api.getNeedsCustom()
                            statusMessage = "✅ GET Éxitoso: ${needs.size} registros en Xano."
                        } catch (e: Exception) {
                            statusMessage = "❌ Error GET: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Probar GET (Ver datos en Xano)")
            }

            // Formulario POST
            OutlinedTextField(
                value = hectaresText,
                onValueChange = { hectaresText = it },
                label = { Text("Hectáreas para POST") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    isLoading = true
                    statusMessage = null
                    scope.launch {
                        try {
                            val hectareas = hectaresText.toDoubleOrNull() ?: 0.0
                            val response = RetrofitInstance.api.createNeedCustom(
                                NeedRemote(
                                    farmerId = farmerId,
                                    cropCode = "VINEYARD",
                                    hectares = hectareas
                                )
                            )
                            statusMessage = "✅ POST Éxitoso: Terreno ID ${response.id} creado."
                            hectaresText = ""
                        } catch (e: Exception) {
                            statusMessage = "❌ Error POST: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && hectaresText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Probar POST (Crear en Xano)")
            }

            // Mensaje de resultado
            statusMessage?.let {
                Text(it, color = if (it.startsWith("✅")) Color(0xFF2E7D32) else Color.Red)
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
            }
        }
    }
}