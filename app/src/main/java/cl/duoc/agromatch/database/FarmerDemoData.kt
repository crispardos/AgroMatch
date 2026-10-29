package cl.duoc.agromatch.database

import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.model.Interest
import cl.duoc.agromatch.model.InterestStatus
import cl.duoc.agromatch.model.Need
import cl.duoc.agromatch.model.Rating
import cl.duoc.agromatch.model.RatingOrigin

/**
 * Datos ficticios del flujo agricultor, para tests, previews y futura
 * precarga idempotente del seeder. No insertan nada por sí solos.
 *
 * Los IDs son placeholders; al insertar en Room se auto-generan y hay que
 * reemplazarlos por los IDs reales devueltos.
 */
object FarmerDemoData {

    // IDs ficticios para referencias cruzadas en tests/previews.
    private const val FARMER_ANA = 1L
    private const val FARMER_PEDRO = 2L
    private const val PILOT_JUAN = 100L
    private const val PILOT_MARIA = 101L

    val needs: List<Need> = listOf(
        Need(
            farmerId = FARMER_ANA,
            cropCode = Catalog.Crop.VINEYARD,
            zoneCode = Catalog.Zone.COLCHAGUA,
            hectares = 12.5,
            startEpochDay = 20_000L,
            endEpochDay = 20_004L
        ),
        Need(
            farmerId = FARMER_PEDRO,
            cropCode = Catalog.Crop.AVOCADO,
            zoneCode = Catalog.Zone.CURICO,
            hectares = 6.0,
            startEpochDay = 20_010L,
            endEpochDay = 20_010L // mismo día: cuenta como 1 día de trabajo
        )
    )

    val interests: List<Interest> = listOf(
        Interest(
            needId = 1L,
            pilotId = PILOT_JUAN,
            status = InterestStatus.ACTIVE.value,
            createdEpochDay = 20_001L
        ),
        Interest(
            needId = 2L,
            pilotId = PILOT_MARIA,
            status = InterestStatus.COMPLETED.value,
            createdEpochDay = 20_005L,
            completedEpochDay = 20_012L
        )
    )

    val ratings: List<Rating> = listOf(
        Rating(
            interestId = 2L,
            score = 5,
            comment = "Muy buen trabajo, rápido y prolijo.",
            createdEpochDay = 20_013L,
            origin = RatingOrigin.LOCAL_USER.value
        ),
        Rating(
            interestId = 1L,
            score = 4,
            comment = null,
            createdEpochDay = 20_002L,
            origin = RatingOrigin.DEMO.value
        )
    )
}