package cl.duoc.agromatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.model.PilotCoverage
import cl.duoc.agromatch.model.PilotProfile
import cl.duoc.agromatch.model.PilotSpecialty
import cl.duoc.agromatch.model.UserRole
import cl.duoc.agromatch.database.PilotRequestSummary
import cl.duoc.agromatch.repository.AuthRepository
import cl.duoc.agromatch.repository.PilotFailure
import cl.duoc.agromatch.repository.PilotRepository
import cl.duoc.agromatch.repository.PilotSaveResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class PilotUiState(
    val profile: PilotProfile? = null,
    val coverages: List<PilotCoverage> = emptyList(),
    val specialties: List<PilotSpecialty> = emptyList(),
    val requests: List<PilotRequestSummary> = emptyList(),
    val experienceYears: String = "0",
    val hectaresPerHour: String = "",
    val workingHoursPerDay: String = "",
    val availableFrom: String? = null,
    val availableTo: String? = null,
    val selectedZones: Set<String> = emptySet(),
    val selectedSpecialties: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val profileSaved: Boolean = false,
    val experienceError: Boolean = false,
    val hectaresError: Boolean = false,
    val workingHoursError: Boolean = false,
    val availabilityError: Boolean = false,
    val error: PilotFailure? = null
)

class PilotViewModel(
    private val pilotRepository: PilotRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(PilotUiState())
    val state: StateFlow<PilotUiState> = mutableState.asStateFlow()
    private var userId: Long = 0
    private var draftLoaded = false
    private var loadJob: Job? = null

    init {
        loadProfile()
    }

    fun loadProfile() {
        if (mutableState.value.isWorking) return
        loadJob?.cancel()
        mutableState.value = mutableState.value.copy(isLoading = true, error = null)
        loadJob = viewModelScope.launch {
            try {
                val user = authRepository.restoreSession()
                if (user == null || user.role != UserRole.PILOT.value) {
                    mutableState.value = mutableState.value.copy(
                        isLoading = false, error = PilotFailure.SESSION_REQUIRED
                    )
                    return@launch
                }
                userId = user.id
                combine(
                    pilotRepository.observeProfile(userId),
                    pilotRepository.observeCoverages(userId),
                    pilotRepository.observeSpecialties(userId),
                    pilotRepository.observeRequests(userId)
                ) { profile, coverages, specialties, requests ->
                    PilotProfileState(profile, coverages, specialties, requests)
                }
                    .collect { profileState ->
                        var updated = mutableState.value.copy(
                            profile = profileState.profile,
                            coverages = profileState.coverages,
                            specialties = profileState.specialties,
                            requests = profileState.requests,
                            isLoading = false, error = null
                        )
                        // Cargar el borrador una sola vez: las emisiones no borran lo escrito.
                        if (!draftLoaded) {
                            updated = updated.copy(
                                experienceYears = (profileState.profile?.experienceYears ?: 0).toString(),
                                hectaresPerHour = profileState.profile?.hectaresPerHour?.toString() ?: "",
                                workingHoursPerDay = profileState.profile?.workingHoursPerDay?.toString() ?: "",
                                availableFrom = profileState.profile?.availableFrom,
                                availableTo = profileState.profile?.availableTo,
                                selectedZones = profileState.coverages.map { it.zoneCode }.toSet(),
                                selectedSpecialties = profileState.specialties.map { it.specialtyCode }.toSet()
                            )
                            draftLoaded = true
                        }
                        mutableState.value = updated
                    }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = mutableState.value.copy(
                    isLoading = false, error = PilotFailure.STORAGE_ERROR
                )
            }
        }
    }

    private data class PilotProfileState(
        val profile: PilotProfile?,
        val coverages: List<PilotCoverage>,
        val specialties: List<PilotSpecialty>,
        val requests: List<PilotRequestSummary>
    )

    fun updateExperience(value: String) {
        mutableState.value = mutableState.value.copy(
            experienceYears = value, experienceError = false, profileSaved = false
        )
    }

    fun updateHectares(value: String) {
        mutableState.value = mutableState.value.copy(
            hectaresPerHour = value, hectaresError = false, profileSaved = false
        )
    }

    fun updateWorkingHours(value: String) {
        mutableState.value = mutableState.value.copy(
            workingHoursPerDay = value, workingHoursError = false, profileSaved = false
        )
    }

    fun updateAvailableFrom(value: String?) {
        mutableState.value = mutableState.value.copy(
            availableFrom = value,
            availabilityError = false,
            profileSaved = false
        )
    }

    fun updateAvailableTo(value: String?) {
        mutableState.value = mutableState.value.copy(
            availableTo = value,
            availabilityError = false,
            profileSaved = false
        )
    }

    fun clearAvailability() {
        mutableState.value = mutableState.value.copy(
            availableFrom = null,
            availableTo = null,
            availabilityError = false,
            profileSaved = false
        )
    }

    fun toggleZone(code: String) {
        val zones = mutableState.value.selectedZones
        mutableState.value = mutableState.value.copy(
            selectedZones = if (code in zones) zones - code else zones + code,
            profileSaved = false
        )
    }

    fun toggleSpecialty(code: String) {
        val specialties = mutableState.value.selectedSpecialties
        mutableState.value = mutableState.value.copy(
            selectedSpecialties = if (code in specialties) specialties - code else specialties + code,
            profileSaved = false
        )
    }

    fun clearFeedback() {
        mutableState.value = mutableState.value.copy(profileSaved = false)
    }

    fun saveProfile() {
        val draft = mutableState.value
        if (draft.isLoading || draft.isWorking || !draftLoaded) return
        val experience = draft.experienceYears.trim().toIntOrNull()
        val hectares = draft.hectaresPerHour.trim().replace(',', '.').toDoubleOrNull()
        val hours = draft.workingHoursPerDay.trim().replace(',', '.').toDoubleOrNull()
        val experienceInvalid = experience == null || experience < 0
        val hectaresInvalid = hectares == null || !hectares.isFinite() || hectares <= 0
        val hoursInvalid = hours == null || !hours.isFinite() || hours <= 0
        val availabilityInvalid = draft.availableFrom != null && draft.availableTo != null &&
            draft.availableFrom > draft.availableTo
        mutableState.value = draft.copy(
            experienceError = experienceInvalid, hectaresError = hectaresInvalid,
            workingHoursError = hoursInvalid, availabilityError = availabilityInvalid,
            profileSaved = false, error = null
        )
        if (experienceInvalid || hectaresInvalid || hoursInvalid || availabilityInvalid) return

        val profile = PilotProfile(
            userId = userId,
            serviceCode = Catalog.ServiceType.FUMIGATION,
            experienceYears = experience,
            hectaresPerHour = hectares,
            workingHoursPerDay = hours,
            availableFrom = draft.availableFrom,
            availableTo = draft.availableTo
        )
        mutableState.value = mutableState.value.copy(isWorking = true)
        viewModelScope.launch {
            val result = pilotRepository.saveProfile(
                profile,
                draft.selectedZones.map { PilotCoverage(userId, it) },
                draft.selectedSpecialties.map { PilotSpecialty(userId, it) }
            )
            mutableState.value = when (result) {
                PilotSaveResult.Success -> mutableState.value.copy(isWorking = false, profileSaved = true)
                is PilotSaveResult.Failure -> mutableState.value.copy(isWorking = false, error = result.reason)
            }
        }
    }
}
