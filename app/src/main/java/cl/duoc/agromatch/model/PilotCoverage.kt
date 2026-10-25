package cl.duoc.agromatch.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "pilot_coverages",
    primaryKeys = ["pilotId", "zoneCode"],
    foreignKeys = [
        ForeignKey(
            entity = PilotProfile::class,
            parentColumns = ["userId"],
            childColumns = ["pilotId"]
        )
    ]
)
data class PilotCoverage(
    val pilotId: Long,
    val zoneCode: String
)
