package cl.duoc.agromatch.model

/**
 * Resultado explicable del matching para un piloto y una necesidad específica.
 * No se guarda en Room, se calcula al vuelo.
 */
data class MatchResult(
    val pilot: PilotSnapshot,
    val compatibility: Compatibility,

    // Explicaciones para mostrar en la UI ("Por qué coincide o no")
    val zoneMatches: Boolean,
    val specialtyMatches: Boolean,
    val hasCapacity: Boolean,
    val hasAvailability: Boolean,

    // Datos calculados
    val requiredHours: Double,
    val availableWorkHours: Double,
    val availableDays: Int
) {
    enum class Compatibility {
        FULL,       // Cumple servicio, zona, capacidad y disponibilidad
        PARTIAL,    // Coincide en algo, pero falta capacidad o disponibilidad
        UNKNOWN,    // Faltan datos del piloto (ej. no declaró horas)
        INCOMPATIBLE // Zona o especialidad no coinciden
    }
}