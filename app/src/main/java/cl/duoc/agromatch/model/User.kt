package cl.duoc.agromatch.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole(val value: String) {
    FARMER("FARMER"),
    PILOT("PILOT"),
    ADMIN("ADMIN");

    companion object {
        fun fromValue(value: String): UserRole =
            entries.firstOrNull { it.value == value } ?: FARMER
    }
}

@Entity(
    tableName = "users",
    indices = [Index(value = ["aliasKey"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alias: String,
    val aliasKey: String,
    val role: String,
    val passwordHash: String,
    val passwordSalt: String
)
