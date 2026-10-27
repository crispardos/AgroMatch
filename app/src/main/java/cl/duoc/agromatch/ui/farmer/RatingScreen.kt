package cl.duoc.agromatch.ui.farmer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.viewmodel.FarmerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    viewModel: FarmerViewModel,
    pilotAlias: String,
    onNavigateBack: () -> Unit,
    onRatingSaved: () -> Unit
) {
    var score by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Valorar Servicio") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Cómo fue tu experiencia con $pilotAlias?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Estrellas de 1 a 5
            Row(horizontalArrangement = Arrangement.Center) {
                for (i in 1..5) {
                    IconButton(onClick = { score = i }) {
                        Icon(
                            imageVector = if (i <= score) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "$i estrellas",
                            tint = if (i <= score) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { if (it.length <= 300) comment = it },
                label = { Text("Comentario (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                supportingText = { Text("${comment.length}/300") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.error != null) {
                Text(text = state.error ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (score > 0) {
                        viewModel.saveRating(score, comment.ifBlank { null })
                        onRatingSaved()
                    }
                },
                enabled = score > 0 && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Enviar Valoración")
                }
            }
        }
    }
}