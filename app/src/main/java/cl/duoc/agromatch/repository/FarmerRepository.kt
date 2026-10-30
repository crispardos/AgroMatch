package cl.duoc.agromatch.repository

import cl.duoc.agromatch.database.InterestDao
import cl.duoc.agromatch.database.NeedDao
import cl.duoc.agromatch.database.PilotCoverageDao
import cl.duoc.agromatch.database.PilotProfileDao
import cl.duoc.agromatch.database.PilotSpecialtyDao
import cl.duoc.agromatch.database.RatingDao
import cl.duoc.agromatch.model.*
import kotlinx.coroutines.flow.Flow
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class FarmerRepository(
    private val needDao: NeedDao,
    private val interestDao: InterestDao,
    private val ratingDao: RatingDao,
    private val pilotProfileDao: PilotProfileDao,
    private val pilotCoverageDao: PilotCoverageDao,
    private val pilotSpecialtyDao: PilotSpecialtyDao
) {

    // --- NECESIDADES ---

    fun getNeedsByFarmer(farmerId: Long): Flow<List<Need>> {
        return needDao.getNeedsByFarmer(farmerId)
    }

    suspend fun getNeedById(needId: Long): Need? {
        return needDao.findById(needId)
    }

    suspend fun createNeed(need: Need): Long {
        val errors = AgroMatchValidator.validateNeed(need)
        if (errors.isNotEmpty()) {
            throw IllegalArgumentException("Necesidad inválida: $errors")
        }
        return needDao.insert(need)
    }

    // --- MATCHING Y PILOTOS ---

    fun calculateMatches(need: Need, pilots: List<PilotSnapshot>): List<MatchResult> {
        return MatchingCalculator.calculate(need, pilots)
    }

    fun applyFilters(
        results: List<MatchResult>,
        zoneFilter: String?,
        specialtyFilter: String?
    ): List<MatchResult> {
        return results.filter { result ->
            val matchesZone = zoneFilter == null || result.pilot.zones.contains(zoneFilter)
            val matchesSpecialty = specialtyFilter == null || result.pilot.specialties.contains(specialtyFilter)
            matchesZone && matchesSpecialty
        }
    }

    // --- INTERESES Y FINALIZACIÓN ---

    suspend fun getActiveInterest(needId: Long): Interest? {
        return interestDao.findByNeedAndStatus(needId)
    }

    suspend fun getLatestInterest(needId: Long): Interest? {
        return interestDao.getLatestByNeed(needId)
    }

    suspend fun hasRating(interestId: Long): Boolean {
        return ratingDao.findByInterestId(interestId) != null
    }

    suspend fun manifestInterest(needId: Long, pilotId: Long, currentEpochDay: Long): Long {
        val newInterest = Interest(
            needId = needId,
            pilotId = pilotId,
            createdEpochDay = currentEpochDay
        )
        return interestDao.replaceActiveInterest(newInterest)
    }

    suspend fun completeInterest(interestId: Long, currentEpochDay: Long) {
        val interest = interestDao.findById(interestId)
            ?: throw IllegalArgumentException("Interés no encontrado")

        val errors = AgroMatchValidator.validateCompleteInterest(interest, interest.status)
        if (errors.isNotEmpty()) {
            throw IllegalArgumentException("No se puede completar: $errors")
        }

        interestDao.updateStatus(
            interestId = interestId,
            status = InterestStatus.COMPLETED.value,
            completedEpochDay = currentEpochDay
        )
    }

    // --- VALORACIONES ---

    suspend fun saveRating(
        interestId: Long,
        score: Int,
        comment: String?,
        currentEpochDay: Long
    ): Long {
        val interest = interestDao.findById(interestId)
            ?: throw IllegalArgumentException("Interés no encontrado")

        val rating = Rating(
            interestId = interestId,
            score = score,
            comment = comment,
            createdEpochDay = currentEpochDay,
            origin = RatingOrigin.LOCAL_USER.value
        )

        val errors = AgroMatchValidator.validateRating(rating, interest.status)
        if (errors.isNotEmpty()) {
            throw IllegalArgumentException("Valoración inválida: $errors")
        }

        val existing = ratingDao.findByInterestId(interestId)
        if (existing != null) {
            throw IllegalArgumentException("Este servicio ya fue valorado")
        }

        return ratingDao.insert(rating)
    }

    suspend fun getPilotRatingsSummary(pilotId: Long): Pair<Double?, Int> {
        val avg = ratingDao.getAverageScoreForPilot(pilotId)
        val count = ratingDao.getRatingCountForPilot(pilotId)
        return Pair(avg, count)
    }
    // --- PILOTOS REALES ---

    suspend fun getAllPilotsForMatching(): List<PilotSnapshot> {
        val profiles = pilotProfileDao.getDirectorySnapshot()
        val coverages = pilotCoverageDao.getAllCoverages()
        val specialties = pilotSpecialtyDao.getAllSpecialties()
        val ratingsSummary = ratingDao.getRatingsSummaryByPilot()
        val coverageMap = coverages.groupBy { it.pilotId }
        val specialtyMap = specialties.groupBy { it.pilotId }
        val ratingMap = ratingsSummary.associateBy { it.pilotId }

        return profiles.map { entry ->
            val profile = entry.profile
            val ratings = ratingMap[profile.userId]
            val hasAvailabilityDates = profile.availableFrom != null || profile.availableTo != null
            val availableStartDay = profile.availableFrom?.let(::parseEpochDay)
                ?: if (hasAvailabilityDates) Long.MIN_VALUE else null
            val availableEndDay = profile.availableTo?.let(::parseEpochDay)
                ?: if (hasAvailabilityDates) Long.MAX_VALUE else null

            object : PilotSnapshot {
                override val pilotId = profile.userId
                override val alias = entry.alias
                override val hectaresPerHour = profile.hectaresPerHour
                override val workingHoursPerDay = profile.workingHoursPerDay
                override val zones = coverageMap[profile.userId]?.map { it.zoneCode }?.toSet() ?: emptySet()
                override val specialties = specialtyMap[profile.userId]?.map { it.specialtyCode }?.toSet() ?: emptySet()
                override val avgRating = ratings?.avgScore
                override val ratingCount = ratings?.count ?: 0
                override val availableStartDay = availableStartDay
                override val availableEndDay = availableEndDay
            }
        }
    }

    private fun parseEpochDay(value: String): Long? {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val position = ParsePosition(0)
        val date = formatter.parse(value, position)?.takeIf { position.index == value.length } ?: return null
        return date.time / 86_400_000L
    }
}
