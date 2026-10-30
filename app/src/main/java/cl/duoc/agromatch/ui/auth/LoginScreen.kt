package cl.duoc.agromatch.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import cl.duoc.agromatch.R
import cl.duoc.agromatch.repository.AuthFailure

@Composable
fun LoginScreen(
    isWorking: Boolean,
    error: AuthFailure?,
    onClearError: () -> Unit,
    onLogin: (alias: String, password: String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var alias by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthScreenLayout(
        title = stringResource(R.string.login_title),
        subtitle = stringResource(R.string.auth_login_subtitle)
    ) {
        OutlinedTextField(
            value = alias,
            onValueChange = {
                alias = it
                onClearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.alias_label)) },
            singleLine = true,
            isError = error == AuthFailure.INVALID_FIELDS || error == AuthFailure.INVALID_CREDENTIALS,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            shape = RectangleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AuthBlue,
                focusedLabelColor = AuthBlue,
                cursorColor = AuthBlue
            )
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                onClearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password_label)) },
            singleLine = true,
            isError = error == AuthFailure.INVALID_FIELDS || error == AuthFailure.INVALID_CREDENTIALS,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RectangleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AuthBlue,
                focusedLabelColor = AuthBlue,
                cursorColor = AuthBlue
            )
        )

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = authErrorText(error),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = { onLogin(alias, password) },
            enabled = !isWorking && alias.isNotBlank() && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = AuthBlue)
        ) {
            if (isWorking) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(stringResource(R.string.login_action))
            }
        }
        TextButton(onClick = onRegisterClick, enabled = !isWorking) {
            Text(
                text = stringResource(R.string.go_to_register),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = AuthBlue
            )
        }
    }
}

@Composable
internal fun authErrorText(error: AuthFailure): String = when (error) {
    AuthFailure.INVALID_FIELDS -> stringResource(R.string.auth_error_invalid_fields)
    AuthFailure.ALIAS_ALREADY_EXISTS -> stringResource(R.string.auth_error_alias_taken)
    AuthFailure.INVALID_CREDENTIALS -> stringResource(R.string.auth_error_invalid_credentials)
    AuthFailure.STORAGE_ERROR -> stringResource(R.string.auth_error_storage)
}
