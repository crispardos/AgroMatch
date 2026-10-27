package cl.duoc.agromatch.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import cl.duoc.agromatch.model.Interest
import cl.duoc.agromatch.model.InterestStatus

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
                status = InterestStatus.COMPLETED.value,
                completedEpochDay = newInterest.createdEpochDay
            )
        }
        return insert(newInterest)
    }
}