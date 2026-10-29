package cl.duoc.agromatch.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class RatingOrigin(val value: String) {
    DEMO("DEMO"),
    LOCAL_USER("LOCAL_USER");

    companion object {
        fun fromValue(value: String): RatingOrigin =
            entries.firstOrNull { it.value == value } ?: LOCAL_USER
    }
}

@Entity(
    tableName = "ratings",
    foreignKeys = [
        ForeignKey(
            entity = Interest::class,
            parentColumns = ["id"],
            childColumns = ["interestId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        // Una sola valoración por interés.
        Index(value = ["interestId"], unique = true)
    ]
)
data class Rating(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val interestId: Long,
    val score: Int,
    val comment: String? = null,
    val createdEpochDay: Long,
    val origin: String = RatingOrigin.LOCAL_USER.value
) {
    init {
        require(score in MIN_SCORE..MAX_SCORE) {
            "score debe estar entre $MIN_SCORE y $MAX_SCORE"
        }
    }

    companion object {
        const val MIN_SCORE = 1
        const val MAX_SCORE = 5
        const val MAX_COMMENT_LENGTH = 300
    }
}