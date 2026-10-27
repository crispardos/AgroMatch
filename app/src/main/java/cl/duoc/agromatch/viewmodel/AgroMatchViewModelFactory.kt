package cl.duoc.agromatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.agromatch.repository.AuthRepository
import cl.duoc.agromatch.repository.FarmerRepository

class AgroMatchViewModelFactory(
    private val authRepository: AuthRepository,
    private val farmerRepository: FarmerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        if (modelClass.isAssignableFrom(FarmerViewModel::class.java)) {
            return FarmerViewModel(farmerRepository, farmerId = 1L) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}