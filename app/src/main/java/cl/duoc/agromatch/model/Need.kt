package cl.duoc.agromatch.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "needs",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["farmerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("farmerId")]
)
data class Need(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val farmerId: Long,
    val serviceType: String = Catalog.ServiceType.FUMIGATION,
    val cropCode: String,
    val zoneCode: String,
    val hectares: Double,
    val startEpochDay: Long,
    val endEpochDay: Long
)