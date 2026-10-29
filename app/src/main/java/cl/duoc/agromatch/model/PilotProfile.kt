package cl.duoc.agromatch.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Perfil de servicio de una cuenta con rol PILOT.
 * Las fechas de disponibilidad usan ISO-8601 (yyyy-MM-dd).
 */
@Entity(
    tableName = "pilot_profiles",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"]
        )
    ]
)
data class PilotProfile(
    @PrimaryKey
    val userId: Long,
    val serviceCode: String,
    val experienceYears: Int?,
    val hectaresPerHour: Double,
    val workingHoursPerDay: Double,
    val availableFrom: String?,
    val availableTo: String?
)
