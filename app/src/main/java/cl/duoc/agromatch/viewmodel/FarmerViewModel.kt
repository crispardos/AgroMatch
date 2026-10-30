package cl.duoc.agromatch.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agromatch.database.FarmerNeedOverview
import cl.duoc.agromatch.model.*
import cl.duoc.agromatch.repository.FarmerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FarmerUiState(
    val needs: List<Need> = emptyList(),
    val needOverviews: List<FarmerNeedOverview> = emptyList(),
    val currentNeed: Need? = null,
    val matchResults: List<MatchResult> = emptyList(),
    val filteredResults: List<MatchResult> = emptyList(),
    val activeInterest: Interest? = null,
    val ratingInterestId: Long? = null,
    val hasRating: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class FarmerViewModel(
    private val repository: FarmerRepository,
    private val farmerId: Long
) : ViewModel() {

    private val mutableState = MutableStateFlow(FarmerUiState())
    val state: StateFlow<FarmerUiState> = mutableState.asStateFlow()

    init {
        loadNeeds()
    }

    fun loadNeeds() {
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isLoading = true, error = null)
            try {
                repository.getNeedOverviewsByFarmer(farmerId).collect { overviews ->
                    mutableState.value = mutableState.value.copy(
                        needs = overviews.map { it.need },
                        needOverviews = overviews,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                mutableState.value = mutableState.value.copy(isLoading = false, error = "Error al cargar necesidades")
            }
        }
    }

    fun createNeed(need: Need, photoUris: List<Uri>, onSaved: () -> Unit) {
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isLoading = true, error = null)
            try {
                repository.createNeed(need, photoUris)
                mutableState.value = mutableState.value.copy(isLoading = false)
                onSaved()
            } catch (e: Exception) {
                mutableState.value = mutableState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectNeedForMatching(need: Need) {
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isLoading = true, error = null)
            try {
                val pilots = repository.getAllPilotsForMatching()
                val results = repository.calculateMatches(need, pilots)
                val interest = repository.getLatestInterest(need.id)
                val hasRating = interest?.let { repository.hasRating(it.id) } ?: false

                mutableState.value = mutableState.value.copy(
                    currentNeed = need,
                    matchResults = results,
                    filteredResults = results,
                    activeInterest = interest,
                    hasRating = hasRating,
                    isLoading = false
                )
            } catch (e: Exception) {
                mutableState.value = mutableState.value.copy(isLoading = false, error = "Error al buscar pilotos: ${e.message}")
            }
        }
    }

    fun applyFilters(zone: String?, specialty: String?) {
        val filtered = repository.applyFilters(mutableState.value.matchResults, zone, specialty)
        mutableState.value = mutableState.value.copy(filteredResults = filtered)
    }

    fun manifestInterest(pilotId: Long) {
        val needId = mutableState.value.currentNeed?.id ?: return
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isLoading = true, error = null)
            try {
                val currentEpochDay = System.currentTimeMillis() / 86_400_000L
                repository.manifestInterest(needId, pilotId, currentEpochDay)
                val interest = repository.getLatestInterest(needId)
                val hasRating = interest?.let { repository.hasRating(it.id) } ?: false
                mutableState.value = mutableState.value.copy(
                    activeInterest = interest,
                    hasRating = hasRating,
                    isLoading = false
                )
            } catch (e: Exception) {
                mutableState.value = mutableState.value.copy(isLoading = false, error = "Error al manifestar interés")
            }
        }
    }

    fun completeService() {
        val interestId = mutableState.value.activeInterest?.id ?: return
        val needId = mutableState.value.currentNeed?.id ?: return
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isLoading = true, error = null)
            try {
                val currentEpochDay = System.currentTimeMillis() / 86_400_000L
                repository.completeInterest(interestId, currentEpochDay)
                val updatedInterest = repository.getLatestInterest(needId)
                val hasRating = updatedInterest?.let { repository.hasRating(it.id) } ?: false
                mutableState.value = mutableState.value.copy(
                    isLoading = false,
                    activeInterest = updatedInterest,
                    hasRating = hasRating
                )
            } catch (e: Exception) {
                mutableState.value = mutableState.value.copy(isLoading = false, error = "Error al finalizar servicio")
            }
        }
    }

    fun saveRating(score: Int, comment: String?, onSaved: () -> Unit) {
        val interestId = mutableState.value.ratingInterestId
            ?: mutableState.value.activeInterest?.id
            ?: return
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isLoading = true, error = null)
            try {
                val currentEpochDay = System.currentTimeMillis() / 86_400_000L
                repository.saveRating(interestId, score, comment, currentEpochDay)
                mutableState.value = mutableState.value.copy(isLoading = false)
                onSaved()
            } catch (e: Exception) {
                mutableState.value = mutableState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun setActiveInterestId(interestId: Long) {
        mutableState.value = mutableState.value.copy(ratingInterestId = interestId, error = null)
    }
    fun clearError() {
        mutableState.value = mutableState.value.copy(error = null)
    }
}
