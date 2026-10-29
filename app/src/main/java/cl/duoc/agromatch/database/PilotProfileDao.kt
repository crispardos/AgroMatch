package cl.duoc.agromatch.database

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Upsert
import cl.duoc.agromatch.model.PilotProfile
import kotlinx.coroutines.flow.Flow

data class PilotDirectoryEntry(
    @Embedded val profile: PilotProfile,
    val alias: String
)

@Dao
interface PilotProfileDao {
    @Query("SELECT * FROM pilot_profiles WHERE userId = :userId LIMIT 1")
    fun observeByUserId(userId: Long): Flow<PilotProfile?>

    @Query("""
        SELECT pilot_profiles.*, users.alias
        FROM pilot_profiles
        INNER JOIN users ON users.id = pilot_profiles.userId
        WHERE users.role = 'PILOT'
        ORDER BY users.aliasKey
    """)
    fun observeDirectory(): Flow<List<PilotDirectoryEntry>>

    @Query("""
        SELECT pilot_profiles.*, users.alias
        FROM pilot_profiles
        INNER JOIN users ON users.id = pilot_profiles.userId
        WHERE users.role = 'PILOT'
        ORDER BY users.aliasKey
    """)
    suspend fun getDirectorySnapshot(): List<PilotDirectoryEntry>
    @Upsert
    suspend fun upsert(profile: PilotProfile)
}
