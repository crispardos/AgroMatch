package cl.duoc.agromatch.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class InterestStatus(val value: String) {
    ACTIVE("ACTIVE"),
    COMPLETED("COMPLETED");

    companion object {
        fun fromValue(value: String): InterestStatus =
            entries.firstOrNull { it.value == value } ?: ACTIVE
    }
}

@Entity(
    tableName = "interests",
    foreignKeys = [
        ForeignKey(
            entity = Need::class,
            parentColumns = ["id"],
            childColumns = ["needId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["pilotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("needId"),
        Index("pilotId"),
        // Un piloto no puede tener dos intereses sobre la misma necesidad,
        // ni siquiera históricos. Es una regla de negocio del MVP.
        Index(value = ["needId", "pilotId"], unique = true)
    ]
)
data class Interest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val needId: Long,
    val pilotId: Long,
    val status: String = InterestStatus.ACTIVE.value,
    val createdEpochDay: Long,
    val completedEpochDay: Long? = null
)