package cl.duoc.agromatch.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.agromatch.model.PilotSpecialty
import kotlinx.coroutines.flow.Flow

@Dao
interface PilotSpecialtyDao {
    @Query("SELECT * FROM pilot_specialties WHERE pilotId = :pilotId")
    fun observeByPilotId(pilotId: Long): Flow<List<PilotSpecialty>>

    @Query("SELECT * FROM pilot_specialties ORDER BY pilotId, specialtyCode")
    fun observeAll(): Flow<List<PilotSpecialty>>

    @Query("DELETE FROM pilot_specialties WHERE pilotId = :pilotId")
    suspend fun deleteByPilotId(pilotId: Long)

    @Query("SELECT * FROM pilot_specialties")
    suspend fun getAllSpecialties(): List<PilotSpecialty>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(specialties: List<PilotSpecialty>)
}
