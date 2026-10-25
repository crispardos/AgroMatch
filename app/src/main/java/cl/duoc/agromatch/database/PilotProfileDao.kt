package cl.duoc.agromatch.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cl.duoc.agromatch.model.PilotProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface PilotProfileDao {
    @Query("SELECT * FROM pilot_profiles WHERE userId = :userId LIMIT 1")
    fun observeByUserId(userId: Long): Flow<PilotProfile?>

    @Upsert
    suspend fun upsert(profile: PilotProfile)
}
