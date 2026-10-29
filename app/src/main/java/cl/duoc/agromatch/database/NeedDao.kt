package cl.duoc.agromatch.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.agromatch.model.Need
import kotlinx.coroutines.flow.Flow

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
}