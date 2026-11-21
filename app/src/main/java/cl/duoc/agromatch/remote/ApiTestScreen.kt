package cl.duoc.agromatch.ui.remote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.remote.NeedRemote
import cl.duoc.agromatch.remote.PatchNeedRequest
import cl.duoc.agromatch.remote.RetrofitInstance
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiTestScreen(onNavigateBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("Los resultados aparecerán aquí...") }

    var farmerIdText by remember { mutableStateOf("1") }
    var hectaresText by remember { mutableStateOf("10.5") }
    var cropText by remember { mutableStateOf("VINEYARD") }
    var targetIdText by remember { mutableStateOf("1") }

    fun runApiCall(block: suspend () -> String) {
        scope.launch {
            isLoading = true
            resultText = "Conectando con Xano..."
            try {
                resultText = block()
            } catch (e: Exception) {
                // Imprimimos en consola para depurar
                e.printStackTrace()
                resultText = "❌ Error: ${e.message ?: e.toString()}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laboratorio de APIs (Xano)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Datos para Pruebas", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = farmerIdText,
                    onValueChange = { farmerIdText = it },
                    label = { Text("Farmer ID") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = hectaresText,
                    onValueChange = { hectaresText = it },
                    label = { Text("Hectáreas") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = cropText,
                onValueChange = { cropText = it },
                label = { Text("Cultivo") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = targetIdText,
                onValueChange = { targetIdText = it },
                label = { Text("ID a buscar/borrar/actualizar") },
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            Text("Endpoints (7)", fontWeight = FontWeight.Bold)

            // 1. GET PERSONALIZADO
            Button(
                onClick = {
                    runApiCall {
                        val needs = RetrofitInstance.api.getNeedsCustom()
                        "✅ GET (Custom): Se encontraron ${needs.size} terrenos."
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) { Text("1. GET get-needs (Custom)") }

            // 2. POST PERSONALIZADO
            Button(
                onClick = {
                    runApiCall {
                        val response = RetrofitInstance.api.createNeedCustom(
                            NeedRemote(
                                farmerId = farmerIdText.toIntOrNull() ?: 1,
                                cropCode = cropText,
                                hectares = hectaresText.toDoubleOrNull() ?: 0.0
                            )
                        )
                        "✅ POST (Custom): Creado con ID ${response.id}"
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) { Text("2. POST create-need (Custom)") }

            // 3. GET POR ID (CRUD)
            Button(
                onClick = {
                    runApiCall {
                        val need = RetrofitInstance.api.getNeedById(targetIdText.toIntOrNull() ?: 1)
                        "✅ GET (CRUD) ID ${need.id}: ${need.cropCode}, ${need.hectares} ha."
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) { Text("3. GET needs/{id} (CRUD)") }

            // 4. PUT ACTUALIZAR (CRUD)
            Button(
                onClick = {
                    runApiCall {
                        val response = RetrofitInstance.api.updateNeed(
                            id = targetIdText.toIntOrNull() ?: 1,
                            need = NeedRemote(
                                farmerId = farmerIdText.toIntOrNull() ?: 1,
                                cropCode = cropText,
                                hectares = hectaresText.toDoubleOrNull() ?: 0.0
                            )
                        )
                        "✅ PUT: ID ${response.id} actualizado."
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
            ) { Text("4. PUT needs/{id} (CRUD)") }

            // 5. PATCH ACTUALIZAR (CRUD)
            Button(
                onClick = {
                    runApiCall {
                        val request = PatchNeedRequest(
                            hectares = hectaresText.toDoubleOrNull() ?: 0.0,
                            cropCode = cropText
                        )
                        val response = RetrofitInstance.api.patchNeed(
                            id = targetIdText.toIntOrNull() ?: 1,
                            need = request
                        )
                        "✅ PATCH: ID ${response.id} actualizado a ${response.hectares} ha y cultivo ${response.cropCode}."
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
            ) { Text("5. PATCH needs/{id} (CRUD)") }

            // 6. DELETE (CRUD)
            Button(
                onClick = {
                    runApiCall {
                        RetrofitInstance.api.deleteNeed(targetIdText.toIntOrNull() ?: 1)
                        "✅ DELETE: ID ${targetIdText} eliminado."
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) { Text("6. DELETE needs/{id} (CRUD)") }

            // 7. STATUS (PING)
            Button(
                onClick = {
                    runApiCall {
                        val status = RetrofitInstance.api.checkStatus()
                        "✅ STATUS: $status"
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
            ) { Text("7. GET status (Ping)") }

            Divider()

            Text("Resultado de la última operación:", fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFF0F0F0), shape = MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Text(text = resultText)
                }
            }
        }
    }
}