package cl.duoc.agromatch.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import cl.duoc.agromatch.repository.AuthFailure

@Composable
fun LoginScreen(
    isWorking: Boolean,
    error: AuthFailure?,
    onLogin: (alias: String, password: String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var alias by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.login_title), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.alias_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password_label)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = authErrorText(error),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onLogin(alias, password) },
            enabled = !isWorking && alias.isNotBlank() && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isWorking) {
                CircularProgressIndicator()
            } else {
                Text(stringResource(R.string.login_action))
            }
        }
        TextButton(onClick = onRegisterClick, enabled = !isWorking) {
            Text(stringResource(R.string.go_to_register))
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.admin_demo_credentials),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
internal fun authErrorText(error: AuthFailure): String = when (error) {
    AuthFailure.INVALID_FIELDS -> stringResource(R.string.auth_error_invalid_fields)
    AuthFailure.ALIAS_ALREADY_EXISTS -> stringResource(R.string.auth_error_alias_taken)
    AuthFailure.INVALID_CREDENTIALS -> stringResource(R.string.auth_error_invalid_credentials)
    AuthFailure.STORAGE_ERROR -> stringResource(R.string.auth_error_storage)
}
