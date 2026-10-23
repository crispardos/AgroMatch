package cl.duoc.agromatch.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.agromatch.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE aliasKey = :aliasKey LIMIT 1")
    suspend fun findByAliasKey(aliasKey: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun findById(userId: Long): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long
}
