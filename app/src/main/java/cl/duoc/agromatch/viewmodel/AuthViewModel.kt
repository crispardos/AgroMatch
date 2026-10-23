package cl.duoc.agromatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agromatch.model.User
import cl.duoc.agromatch.model.UserRole
import cl.duoc.agromatch.repository.AuthFailure
import cl.duoc.agromatch.repository.AuthRepository
import cl.duoc.agromatch.repository.AuthResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isCheckingSession: Boolean = true,
    val isWorking: Boolean = false,
    val user: User? = null,
    val error: AuthFailure? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = try {
                authRepository.initialize()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
            mutableState.value = AuthUiState(isCheckingSession = false, user = user)
        }
    }

    fun login(alias: String, password: String) {
        if (mutableState.value.isWorking) return
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isWorking = true, error = null)
            val result = try {
                authRepository.login(alias, password)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                AuthResult.Failure(AuthFailure.STORAGE_ERROR)
            }
            applyResult(result)
        }
    }

    fun register(alias: String, password: String, confirmation: String, role: UserRole) {
        if (mutableState.value.isWorking) return
        if (password != confirmation) {
            mutableState.value = mutableState.value.copy(error = AuthFailure.INVALID_FIELDS)
            return
        }

        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isWorking = true, error = null)
            val result = try {
                authRepository.register(alias, password, role)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                AuthResult.Failure(AuthFailure.STORAGE_ERROR)
            }
            applyResult(result)
        }
    }

    fun logout() {
        authRepository.logout()
        mutableState.value = mutableState.value.copy(user = null, error = null)
    }

    fun clearError() {
        mutableState.value = mutableState.value.copy(error = null)
    }

    private fun applyResult(result: AuthResult) {
        mutableState.value = when (result) {
            is AuthResult.Success -> AuthUiState(isCheckingSession = false, user = result.user)
            is AuthResult.Failure -> AuthUiState(isCheckingSession = false, error = result.reason)
        }
    }
}
