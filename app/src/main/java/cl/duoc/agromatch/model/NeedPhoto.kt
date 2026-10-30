package cl.duoc.agromatch.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

const val MAX_PHOTOS_PER_NEED = 5

@Entity(
    tableName = "need_photos",
    foreignKeys = [
        ForeignKey(
            entity = Need::class,
            parentColumns = ["id"],
            childColumns = ["needId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("needId")]
)
data class NeedPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val needId: Long,
    val filePath: String,
    val sortOrder: Int
)
