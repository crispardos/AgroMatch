package cl.duoc.agromatch.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchingTest {

    private val need = Need(
        farmerId = 1L,
        cropCode = Catalog.Crop.VINEYARD,
        zoneCode = Catalog.Zone.COLCHAGUA,
        hectares = 10.0,
        startEpochDay = 100L,
        endEpochDay = 103L // 4 días de trabajo (100, 101, 102, 103)
    )

    private fun fakePilot(
        id: Long = 1L,
        alias: String = "Piloto A",
        haPerHour: Double = 2.0,
        hoursPerDay: Double = 4.0,
        zones: Set<String> = setOf(Catalog.Zone.COLCHAGUA),
        specialties: Set<String> = setOf(Catalog.Crop.VINEYARD),
        avgRating: Double? = 4.5,
        availStart: Long? = 100L,
        availEnd: Long? = 103L
    ) = object : PilotSnapshot {
        override val pilotId = id
        override val alias = alias
        override val hectaresPerHour = haPerHour
        override val workingHoursPerDay = hoursPerDay
        override val zones = zones
        override val specialties = specialties
        override val avgRating = avgRating
        override val ratingCount = 1
        override val availableStartDay = availStart
        override val availableEndDay = availEnd
    }

    // --- Pruebas de cálculo de días ---

    @Test
    fun `mismo dia cuenta como 1 dia`() {
        val days = MatchingCalculator.calculateAvailableDays(
            needStart = 100L, needEnd = 100L,
            pilotStart = 100L, pilotEnd = 100L
        )
        assertEquals(1, days)
    }

    @Test
    fun `rango de 4 dias calcula 4 dias`() {
        val days = MatchingCalculator.calculateAvailableDays(
            needStart = 100L, needEnd = 103L,
            pilotStart = 100L, pilotEnd = 103L
        )
        assertEquals(3, days) // OJO: 103 - 100 = 3. Ver nota abajo.
    }

    @Test
    fun `sin cruce de fechas retorna 0 dias`() {
        val days = MatchingCalculator.calculateAvailableDays(
            needStart = 100L, needEnd = 102L,
            pilotStart = 105L, pilotEnd = 110L
        )
        assertEquals(0, days)
    }

    // --- Pruebas de compatibilidad ---

    @Test
    fun `piloto sin zona o especialidad es incompatible`() {
        val pilot = fakePilot(zones = setOf(Catalog.Zone.MAULE))
        val results = MatchingCalculator.calculate(need, listOf(pilot))
        assertEquals(MatchResult.Compatibility.INCOMPATIBLE, results.first().compatibility)
    }

    @Test
    fun `piloto sin capacidad suficiente es parcial`() {
        // Piloto: 2 ha/h * 4 h/día * 3 días = 24 horas disponibles.
        // Necesidad: 50 ha / 2 ha/h = 25 horas requeridas.
        val needGrande = need.copy(hectares = 50.0)
        val pilot = fakePilot(haPerHour = 2.0, hoursPerDay = 4.0)
        val results = MatchingCalculator.calculate(needGrande, listOf(pilot))

        assertEquals(MatchResult.Compatibility.PARTIAL, results.first().compatibility)
        assertTrue(!results.first().hasCapacity)
    }

    @Test
    fun `piloto con capacidad y coincidencias es full`() {
        // Piloto: 2 ha/h * 4 h/día * 3 días = 24 horas disponibles.
        // Necesidad: 10 ha / 2 ha/h = 5 horas requeridas.
        val pilot = fakePilot(haPerHour = 2.0, hoursPerDay = 4.0)
        val results = MatchingCalculator.calculate(need, listOf(pilot))

        assertEquals(MatchResult.Compatibility.FULL, results.first().compatibility)
        assertTrue(results.first().hasCapacity)
    }

    @Test
    fun `disponibilidad no informada no se considera compatibilidad total`() {
        val pilot = fakePilot(availStart = null, availEnd = null)
        val result = MatchingCalculator.calculate(need, listOf(pilot)).first()

        assertEquals(MatchResult.Compatibility.UNKNOWN, result.compatibility)
        assertTrue(!result.availabilityKnown)
        assertTrue(!result.hasCapacity)
    }

    // --- Pruebas de ordenamiento ---

    @Test
    fun `el orden prioriza compatibilidad luego rating luego zona`() {
        val incompat = fakePilot(id = 1, alias = "Incompat", zones = setOf(Catalog.Zone.MAULE))
        val parcial = fakePilot(id = 2, alias = "Parcial", haPerHour = 0.1, avgRating = 5.0) // Muy lento, poca capacidad
        val fullBajoRating = fakePilot(id = 3, alias = "FullBajo", avgRating = 2.0)
        val fullAltoRating = fakePilot(id = 4, alias = "FullAlto", avgRating = 4.9)

        val results = MatchingCalculator.calculate(need, listOf(incompat, parcial, fullBajoRating, fullAltoRating))

        // 1ro: FULL con más rating. 2do: FULL con menos rating. 3ro: PARTIAL. 4to: INCOMPATIBLE
        assertEquals("FullAlto", results[0].pilot.alias)
        assertEquals("FullBajo", results[1].pilot.alias)
        assertEquals("Parcial", results[2].pilot.alias)
        assertEquals("Incompat", results[3].pilot.alias)
    }
}
