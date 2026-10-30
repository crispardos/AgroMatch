package cl.duoc.agromatch.repository

import androidx.room.withTransaction
import cl.duoc.agromatch.database.AgroMatchDatabase
import cl.duoc.agromatch.database.PilotCoverageDao
import cl.duoc.agromatch.database.PilotDirectoryEntry
import cl.duoc.agromatch.database.InterestDao
import cl.duoc.agromatch.database.PilotRequestSummary
import cl.duoc.agromatch.database.PilotProfileDao
import cl.duoc.agromatch.database.PilotSpecialtyDao
import cl.duoc.agromatch.database.UserDao
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.model.PilotCoverage
import cl.duoc.agromatch.model.PilotProfile
import cl.duoc.agromatch.model.PilotSpecialty
import cl.duoc.agromatch.model.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

sealed interface PilotSaveResult {
    data object Success : PilotSaveResult
    data class Failure(val reason: PilotFailure) : PilotSaveResult
}

enum class PilotFailure {
    INVALID_PROFILE,
    USER_NOT_FOUND,
    USER_NOT_PILOT,
    SESSION_REQUIRED,
    STORAGE_ERROR
}

class PilotRepository(
    private val database: AgroMatchDatabase,
    private val userDao: UserDao,
    private val pilotProfileDao: PilotProfileDao,
    private val pilotCoverageDao: PilotCoverageDao,
    private val pilotSpecialtyDao: PilotSpecialtyDao,
    private val interestDao: InterestDao,
    private val authRepository: AuthRepository
) {
    fun observeDirectory(): Flow<List<PilotDirectoryEntry>> =
        pilotProfileDao.observeDirectory()

    fun observeAllCoverages(): Flow<List<PilotCoverage>> =
        pilotCoverageDao.observeAll()

    fun observeAllSpecialties(): Flow<List<PilotSpecialty>> =
        pilotSpecialtyDao.observeAll()

    fun observeProfile(userId: Long): Flow<PilotProfile?> =
        pilotProfileDao.observeByUserId(userId)

    fun observeCoverages(pilotId: Long): Flow<List<PilotCoverage>> =
        pilotCoverageDao.observeByPilotId(pilotId)

    fun observeSpecialties(pilotId: Long): Flow<List<PilotSpecialty>> =
        pilotSpecialtyDao.observeByPilotId(pilotId)

    fun observeRequests(pilotId: Long): Flow<List<PilotRequestSummary>> =
        interestDao.observeRequestsForPilot(pilotId)

    suspend fun saveProfile(
        profile: PilotProfile,
        coverages: List<PilotCoverage>,
        specialties: List<PilotSpecialty>
    ): PilotSaveResult {
        if (!isValidProfile(profile, coverages, specialties)) {
            return PilotSaveResult.Failure(PilotFailure.INVALID_PROFILE)
        }

        return try {
            val currentUser = authRepository.restoreSession()
            if (currentUser == null || currentUser.id != profile.userId) {
                return PilotSaveResult.Failure(PilotFailure.SESSION_REQUIRED)
            }
            database.withTransaction {
                val user = userDao.findById(profile.userId)
                    ?: return@withTransaction PilotSaveResult.Failure(PilotFailure.USER_NOT_FOUND)

                if (user.role != UserRole.PILOT.value) {
                    return@withTransaction PilotSaveResult.Failure(PilotFailure.USER_NOT_PILOT)
                }

                pilotProfileDao.upsert(profile)
                pilotCoverageDao.deleteByPilotId(profile.userId)
                pilotSpecialtyDao.deleteByPilotId(profile.userId)

                if (coverages.isNotEmpty()) {
                    pilotCoverageDao.insertAll(coverages)
                }
                if (specialties.isNotEmpty()) {
                    pilotSpecialtyDao.insertAll(specialties)
                }

                PilotSaveResult.Success
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            PilotSaveResult.Failure(PilotFailure.STORAGE_ERROR)
        }
    }

    private fun isValidProfile(
        profile: PilotProfile,
        coverages: List<PilotCoverage>,
        specialties: List<PilotSpecialty>
    ): Boolean =
        profile.serviceCode == Catalog.ServiceType.FUMIGATION &&
            (profile.experienceYears == null || profile.experienceYears >= 0) &&
            profile.hectaresPerHour.isFinite() && profile.hectaresPerHour > 0.0 &&
            profile.workingHoursPerDay.isFinite() && profile.workingHoursPerDay > 0.0 &&
            isValidAvailability(profile.availableFrom, profile.availableTo) &&
            coverages.all { it.pilotId == profile.userId } &&
            coverages.map { it.zoneCode }.toSet().size == coverages.size &&
            specialties.all { it.pilotId == profile.userId } &&
            specialties.map { it.specialtyCode }.toSet().size == specialties.size

    private fun isValidAvailability(availableFrom: String?, availableTo: String?): Boolean {
        val startDate = availableFrom?.let(::parseIsoDate)
        if (availableFrom != null && startDate == null) return false

        val endDate = availableTo?.let(::parseIsoDate)
        if (availableTo != null && endDate == null) return false

        return startDate == null || endDate == null || !startDate.after(endDate)
    }

    private fun parseIsoDate(value: String): Date? {
        val formatter = SimpleDateFormat(ISO_DATE_PATTERN, Locale.ROOT).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone(UTC_TIME_ZONE)
        }
        val position = ParsePosition(0)
        val date = formatter.parse(value, position)
        return date?.takeIf { position.index == value.length }
    }

    private companion object {
        const val ISO_DATE_PATTERN = "yyyy-MM-dd"
        const val UTC_TIME_ZONE = "UTC"
    }
}
