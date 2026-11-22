package cl.duoc.agromatch.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.agromatch.repository.AuthRepository
import cl.duoc.agromatch.repository.PilotRepository
import cl.duoc.agromatch.repository.FarmerRepository
import cl.duoc.agromatch.utils.LocationHelper

class AgroMatchViewModelFactory(
    private val authRepository: AuthRepository,
    private val farmerRepository: FarmerRepository,
    private val pilotRepository: PilotRepository,
    private val context: Context,
    private val farmerId: Long? = null
) : ViewModelProvider.Factory {

    fun forFarmer(userId: Long) = AgroMatchViewModelFactory(
        authRepository = authRepository,
        farmerRepository = farmerRepository,
        pilotRepository = pilotRepository,
        context = context,
        farmerId = userId
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        if (modelClass.isAssignableFrom(PilotViewModel::class.java)) {
            return PilotViewModel(pilotRepository, authRepository) as T
        }
        if (modelClass.isAssignableFrom(FarmerViewModel::class.java)) {
            val activeFarmerId = farmerId ?: error("Farmer ID is required")
            return FarmerViewModel(
                repository = farmerRepository,
                farmerId = activeFarmerId,
                locationHelper = LocationHelper(context)
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}