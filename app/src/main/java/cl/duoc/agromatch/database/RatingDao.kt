package cl.duoc.agromatch.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.agromatch.model.Rating

data class PilotRatingSummary(
    val pilotId: Long,
    val avgScore: Double?,
    val count: Int
)
@Dao
interface RatingDao {

    @Insert
    suspend fun insert(rating: Rating): Long

    @Query("SELECT * FROM ratings WHERE interestId = :interestId LIMIT 1")
    suspend fun findByInterestId(interestId: Long): Rating?

    @Query("SELECT * FROM ratings WHERE interestId IN (SELECT id FROM interests WHERE pilotId = :pilotId)")
    suspend fun getRatingsForPilot(pilotId: Long): List<Rating>

    // Calcula el promedio y la cantidad de valoraciones de un piloto en una sola consulta.
    @Query("""
        SELECT AVG(r.score) 
        FROM ratings r 
        INNER JOIN interests i ON r.interestId = i.id 
        WHERE i.pilotId = :pilotId
    """)
    suspend fun getAverageScoreForPilot(pilotId: Long): Double?

    @Query("""
        SELECT COUNT(r.id) 
        FROM ratings r 
        INNER JOIN interests i ON r.interestId = i.id 
        WHERE i.pilotId = :pilotId
    """)
    suspend fun getRatingCountForPilot(pilotId: Long): Int

    @Query("""
        SELECT i.pilotId, AVG(r.score) as avgScore, COUNT(r.id) as count
        FROM ratings r
        INNER JOIN interests i ON r.interestId = i.id
        GROUP BY i.pilotId
    """)
    suspend fun getRatingsSummaryByPilot(): List<PilotRatingSummary>
}