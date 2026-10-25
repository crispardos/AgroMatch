package cl.duoc.agromatch.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.agromatch.model.PilotCoverage
import kotlinx.coroutines.flow.Flow

@Dao
interface PilotCoverageDao {
    @Query("SELECT * FROM pilot_coverages WHERE pilotId = :pilotId")
    fun observeByPilotId(pilotId: Long): Flow<List<PilotCoverage>>

    @Query("DELETE FROM pilot_coverages WHERE pilotId = :pilotId")
    suspend fun deleteByPilotId(pilotId: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(coverages: List<PilotCoverage>)
}
