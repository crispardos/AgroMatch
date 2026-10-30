package cl.duoc.agromatch.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.agromatch.model.NeedPhoto

@Dao
interface NeedPhotoDao {
    @Insert
    suspend fun insertAll(photos: List<NeedPhoto>)

    @Query("SELECT * FROM need_photos WHERE needId = :needId ORDER BY sortOrder")
    suspend fun getByNeedId(needId: Long): List<NeedPhoto>
}
