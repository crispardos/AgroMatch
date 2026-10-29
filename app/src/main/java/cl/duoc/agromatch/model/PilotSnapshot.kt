package cl.duoc.agromatch.model

/**
 * Interfaz que representa los datos mínimos del piloto que necesita el
 * motor de matching. El Integrante 3 hará que su entidad PilotProfile
 * implemente esta interfaz.
 */
interface PilotSnapshot {
    val pilotId: Long
    val alias: String
    val hectaresPerHour: Double
    val workingHoursPerDay: Double

    // Usamos Sets para búsquedas rápidas O(1) al verificar coincidencias
    val zones: Set<String>
    val specialties: Set<String>

    val avgRating: Double?
    val ratingCount: Int

    // Rango de días disponibles en epochDay (inclusivo)
    val availableStartDay: Long
    val availableEndDay: Long
}