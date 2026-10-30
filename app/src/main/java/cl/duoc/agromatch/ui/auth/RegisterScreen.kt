package cl.duoc.agromatch.ui.auth

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    onClearError: () -> Unit,
    onRegister: (alias: String, password: String, confirmation: String, role: UserRole) -> Unit,
    onLoginClick: () -> Unit
) {
    var alias by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(UserRole.FARMER) }
    val aliasHasError = error == AuthFailure.ALIAS_ALREADY_EXISTS ||
        (error == AuthFailure.INVALID_FIELDS && alias.trim().length !in 3..40)
    val passwordHasError = error == AuthFailure.INVALID_FIELDS && !isValidRegistrationPassword(password)
    val confirmationHasError = error == AuthFailure.INVALID_FIELDS && password != confirmation

    AuthScreenLayout(
        title = stringResource(R.string.register_title),
        subtitle = stringResource(R.string.auth_register_subtitle)
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
            isError = aliasHasError,
            shape = RectangleShape,
            colors = authFieldColors()
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
            supportingText = { Text(stringResource(R.string.password_policy)) },
            singleLine = true,
            isError = passwordHasError,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RectangleShape,
            colors = authFieldColors()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmation,
            onValueChange = {
                confirmation = it
                onClearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.confirm_password_label)) },
            singleLine = true,
            isError = confirmationHasError,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RectangleShape,
            colors = authFieldColors()
        )

        Text(
            stringResource(R.string.role_label),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleSmall,
            color = AuthCharcoal
        )
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

        Button(
            onClick = { onRegister(alias, password, confirmation, role) },
            enabled = !isWorking && alias.isNotBlank() && password.isNotEmpty() && confirmation.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = AuthBlue)
        ) {
            if (isWorking) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(stringResource(R.string.register_action))
            }
        }
        TextButton(onClick = onLoginClick, enabled = !isWorking) {
            Text(
                text = stringResource(R.string.go_to_login),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = AuthBlue
            )
        }
    }
}

@Composable
private fun RoleOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) AuthBlue.copy(alpha = 0.06f) else Color.White)
            .border(
                BorderStroke(1.dp, if (selected) AuthBlue else Color(0xFFD7DCE2)),
                RectangleShape
            )
            .clickable(onClick = onClick),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = AuthBlue)
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp),
            color = if (selected) AuthBlue else AuthCharcoal
        )
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AuthBlue,
    focusedLabelColor = AuthBlue,
    cursorColor = AuthBlue
)

private fun isValidRegistrationPassword(password: String): Boolean =
    password.length in 8..128 &&
        password.any(Char::isUpperCase) &&
        password.any(Char::isLowerCase) &&
        password.any(Char::isDigit)
