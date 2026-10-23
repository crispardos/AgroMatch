package cl.duoc.agromatch.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.agromatch.R
import cl.duoc.agromatch.model.UserRole
import cl.duoc.agromatch.repository.AuthFailure

@Composable
fun RegisterScreen(
    isWorking: Boolean,
    error: AuthFailure?,
    onRegister: (alias: String, password: String, confirmation: String, role: UserRole) -> Unit,
    onLoginClick: () -> Unit
) {
    var alias by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(UserRole.FARMER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.register_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.alias_label)) },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password_label)) },
            supportingText = { Text(stringResource(R.string.password_policy)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmation,
            onValueChange = { confirmation = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.confirm_password_label)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.role_label), modifier = Modifier.fillMaxWidth())
        RoleOption(
            text = stringResource(R.string.farmer_role),
            selected = role == UserRole.FARMER,
            onClick = { role = UserRole.FARMER }
        )
        RoleOption(
            text = stringResource(R.string.pilot_role),
            selected = role == UserRole.PILOT,
            onClick = { role = UserRole.PILOT }
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = authErrorText(error),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onRegister(alias, password, confirmation, role) },
            enabled = !isWorking && alias.isNotBlank() && password.isNotEmpty() && confirmation.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isWorking) {
                CircularProgressIndicator()
            } else {
                Text(stringResource(R.string.register_action))
            }
        }
        TextButton(onClick = onLoginClick, enabled = !isWorking) {
            Text(stringResource(R.string.go_to_login))
        }
    }
}

@Composable
private fun RoleOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = text, modifier = Modifier.padding(start = 8.dp))
    }
}
