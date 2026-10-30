package cl.duoc.agromatch.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import cl.duoc.agromatch.model.Interest
import cl.duoc.agromatch.model.InterestStatus
import kotlinx.coroutines.flow.Flow

data class PilotRequestSummary(
    val interestId: Long,
    val needId: Long,
    val farmerAlias: String,
    val cropCode: String,
    val zoneCode: String,
    val hectares: Double,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val createdEpochDay: Long
)

@Dao
interface InterestDao {

    @Insert
    suspend fun insert(interest: Interest): Long

    @Query("SELECT * FROM interests WHERE id = :interestId LIMIT 1")
    suspend fun findById(interestId: Long): Interest?

    @Query("SELECT * FROM interests WHERE needId = :needId AND status = :status LIMIT 1")
    suspend fun findByNeedAndStatus(needId: Long, status: String = InterestStatus.ACTIVE.value): Interest?

    @Query("SELECT * FROM interests WHERE needId = :needId ORDER BY createdEpochDay DESC")
    suspend fun getInterestsByNeed(needId: Long): List<Interest>

    @Query("SELECT * FROM interests WHERE needId = :needId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestByNeed(needId: Long): Interest?

    @Query("""
        SELECT interests.id AS interestId,
            needs.id AS needId,
            users.alias AS farmerAlias,
            needs.cropCode AS cropCode,
            needs.zoneCode AS zoneCode,
            needs.hectares AS hectares,
            needs.startEpochDay AS startEpochDay,
            needs.endEpochDay AS endEpochDay,
            interests.createdEpochDay AS createdEpochDay
        FROM interests
        INNER JOIN needs ON needs.id = interests.needId
        INNER JOIN users ON users.id = needs.farmerId
        WHERE interests.pilotId = :pilotId AND interests.status = :status
        ORDER BY interests.createdEpochDay DESC, interests.id DESC
    """)
    fun observeRequestsForPilot(
        pilotId: Long,
        status: String = InterestStatus.ACTIVE.value
    ): Flow<List<PilotRequestSummary>>

    @Query("UPDATE interests SET status = :status, completedEpochDay = :completedEpochDay WHERE id = :interestId")
    suspend fun updateStatus(
        interestId: Long,
        status: String,
        completedEpochDay: Long?
    )

    /**
     * Regla de negocio: Reemplaza el interés activo de una necesidad por uno nuevo.
     * Si ya había un piloto seleccionado, lo cancela (o lo deja como historial)
     * antes de insertar el nuevo, garantizando que nunca haya dos ACTIVE.
     */
    @Transaction
    suspend fun replaceActiveInterest(newInterest: Interest): Long {
        val currentActive = findByNeedAndStatus(newInterest.needId)
        if (currentActive != null) {
            updateStatus(
                interestId = currentActive.id,
                status = InterestStatus.REPLACED.value,
                completedEpochDay = null
            )
        }
        return insert(newInterest)
    }
}
