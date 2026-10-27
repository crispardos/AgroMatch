package cl.duoc.agromatch.model

import kotlin.math.ceil

object MatchingCalculator {

    /**
     * Calcula la factibilidad y ordena los pilotos según la regla acordada:
     * 1. Factibilidad (FULL > PARTIAL/UNKNOWN > INCOMPATIBLE)
     * 2. Valoración promedio descendente
     * 3. Coincidencia de zona
     * 4. Hectáreas por hora descendente
     * 5. Alias alfabético
     */
    fun calculate(need: Need, pilots: List<PilotSnapshot>): List<MatchResult> {
        val results = pilots.map { pilot -> evaluatePilot(need, pilot) }

        return results.sortedWith(
            compareBy<MatchResult> { it.compatibility.ordinal }
                .thenByDescending { it.pilot.avgRating ?: 0.0 }
                .thenByDescending { it.zoneMatches }
                .thenByDescending { it.pilot.hectaresPerHour }
                .thenBy { it.pilot.alias.lowercase() }
        )
    }

    private fun evaluatePilot(need: Need, pilot: PilotSnapshot): MatchResult {
        // 1. Cálculos de capacidad
        val requiredHours = if (pilot.hectaresPerHour > 0) {
            need.hectares / pilot.hectaresPerHour
        } else {
            Double.POSITIVE_INFINITY
        }

        // 2. Cálculo de días disponibles (diferencia entre fechas)
        val availableDays = calculateAvailableDays(
            needStart = need.startEpochDay,
            needEnd = need.endEpochDay,
            pilotStart = pilot.availableStartDay,
            pilotEnd = pilot.availableEndDay
        )

        val availableWorkHours = availableDays * pilot.workingHoursPerDay
        val hasCapacity = availableWorkHours >= requiredHours && availableDays > 0

        // 3. Coincidencias
        val zoneMatches = pilot.zones.contains(need.zoneCode)
        val specialtyMatches = pilot.specialties.contains(need.cropCode)

        // 4. Determinar compatibilidad
        val compatibility = when {
            !zoneMatches || !specialtyMatches -> MatchResult.Compatibility.INCOMPATIBLE
            !hasCapacity -> MatchResult.Compatibility.PARTIAL
            else -> MatchResult.Compatibility.FULL
        }

        return MatchResult(
            pilot = pilot,
            compatibility = compatibility,
            zoneMatches = zoneMatches,
            specialtyMatches = specialtyMatches,
            hasCapacity = hasCapacity,
            hasAvailability = availableDays > 0,
            requiredHours = requiredHours,
            availableWorkHours = availableWorkHours,
            availableDays = availableDays
        )
    }

    /**
     * Regla de la guía: del 16 al 20 son 4 días (16, 17, 18, 19, 20).
     * Si es el mismo día (ej. 16 al 16), cuenta como mínimo 1 día.
     * Retorna 0 si no hay cruce de fechas entre la necesidad y el piloto.
     */
    internal fun calculateAvailableDays(
        needStart: Long, needEnd: Long,
        pilotStart: Long, pilotEnd: Long
    ): Int {
        val overlapStart = maxOf(needStart, pilotStart)
        val overlapEnd = minOf(needEnd, pilotEnd)

        if (overlapStart > overlapEnd) return 0 // No hay cruce

        // Si overlapStart == overlapEnd, la diferencia es 0, pero cuenta como 1 día.
        return maxOf(1, (overlapEnd - overlapStart).toInt())
    }
}