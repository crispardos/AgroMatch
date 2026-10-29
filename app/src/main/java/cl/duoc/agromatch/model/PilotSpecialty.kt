package cl.duoc.agromatch.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "pilot_specialties",
    primaryKeys = ["pilotId", "specialtyCode"],
    foreignKeys = [
        ForeignKey(
            entity = PilotProfile::class,
            parentColumns = ["userId"],
            childColumns = ["pilotId"]
        )
    ]
)
data class PilotSpecialty(
    val pilotId: Long,
    val specialtyCode: String
)
