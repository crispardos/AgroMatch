package cl.duoc.agromatch.database

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.agromatch.model.Need
import kotlinx.coroutines.flow.Flow

data class FarmerNeedOverview(
    @Embedded val need: Need,
    val interestStatus: String?,
    val pilotAlias: String?,
    val ratingScore: Int?,
    val ratingComment: String?,
    val photoPaths: String?
)

@Dao
interface NeedDao {

    @Insert
    suspend fun insert(need: Need): Long

    @Update
    suspend fun update(need: Need)

    @Query("SELECT * FROM needs WHERE id = :needId LIMIT 1")
    suspend fun findById(needId: Long): Need?

    @Query("SELECT * FROM needs WHERE farmerId = :farmerId ORDER BY startEpochDay DESC")
    fun getNeedsByFarmer(farmerId: Long): Flow<List<Need>>

    @Query("""
        SELECT needs.*,
            interests.status AS interestStatus,
            pilot.alias AS pilotAlias,
            ratings.score AS ratingScore,
            ratings.comment AS ratingComment,
            (SELECT GROUP_CONCAT(filePath, '|') FROM need_photos WHERE needId = needs.id) AS photoPaths
        FROM needs
        LEFT JOIN interests ON interests.id = (
            SELECT MAX(id) FROM interests WHERE needId = needs.id
        )
        LEFT JOIN users AS pilot ON pilot.id = interests.pilotId
        LEFT JOIN ratings ON ratings.interestId = interests.id
        WHERE needs.farmerId = :farmerId
        ORDER BY needs.startEpochDay DESC
    """)
    fun observeOverviewsByFarmer(farmerId: Long): Flow<List<FarmerNeedOverview>>
}
