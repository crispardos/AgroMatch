package cl.duoc.agromatch.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgroMatchValidatorTest {

    private fun validNeed() = Need(
        farmerId = 1L,
        cropCode = Catalog.Crop.VINEYARD,
        zoneCode = Catalog.Zone.COLCHAGUA,
        hectares = 10.0,
        startEpochDay = 20_000L,
        endEpochDay = 20_004L
    )

    private fun validRating(interestId: Long = 1L, score: Int = 5, comment: String? = null) =
        Rating(
            interestId = interestId,
            score = score,
            comment = comment,
            createdEpochDay = 20_001L
        )

    // ---------- Need ----------

    @Test
    fun `need valida no devuelve errores`() {
        assertTrue(AgroMatchValidator.validateNeed(validNeed()).isEmpty())
    }

    @Test
    fun `need sin farmer falla`() {
        val errors = AgroMatchValidator.validateNeed(validNeed().copy(farmerId = 0L))
        assertTrue(errors.contains(AgroMatchValidator.NeedError.FarmerMissing))
    }

    @Test
    fun `need con servicio desconocido falla`() {
        val errors = AgroMatchValidator.validateNeed(validNeed().copy(serviceType = "OTRO"))
        assertTrue(errors.contains(AgroMatchValidator.NeedError.ServiceTypeInvalid))
    }

    @Test
    fun `need con cultivo desconocido falla`() {
        val errors = AgroMatchValidator.validateNeed(validNeed().copy(cropCode = "NO_EXISTE"))
        assertTrue(errors.contains(AgroMatchValidator.NeedError.CropInvalid))
    }

    @Test
    fun `need con zona desconocida falla`() {
        val errors = AgroMatchValidator.validateNeed(validNeed().copy(zoneCode = "NO_EXISTE"))
        assertTrue(errors.contains(AgroMatchValidator.NeedError.ZoneInvalid))
    }

    @Test
    fun `need con cero hectareas falla`() {
        val errors = AgroMatchValidator.validateNeed(validNeed().copy(hectares = 0.0))
        assertTrue(errors.contains(AgroMatchValidator.NeedError.HectaresNotPositive))
    }

    @Test
    fun `need con hectareas negativas falla`() {
        val errors = AgroMatchValidator.validateNeed(validNeed().copy(hectares = -3.0))
        assertTrue(errors.contains(AgroMatchValidator.NeedError.HectaresNotPositive))
    }

    @Test
    fun `need con hectareas NaN falla`() {
        val errors = AgroMatchValidator.validateNeed(validNeed().copy(hectares = Double.NaN))
        assertTrue(errors.contains(AgroMatchValidator.NeedError.HectaresNotFinite))
    }

    @Test
    fun `need con hectareas infinitas falla`() {
        val errors = AgroMatchValidator.validateNeed(
            validNeed().copy(hectares = Double.POSITIVE_INFINITY)
        )
        assertTrue(errors.contains(AgroMatchValidator.NeedError.HectaresNotFinite))
    }

    @Test
    fun `need sin fecha de inicio falla`() {
        val errors = AgroMatchValidator.validateNeed(validNeed().copy(startEpochDay = -1L))
        assertTrue(errors.contains(AgroMatchValidator.NeedError.StartDateMissing))
    }

    @Test
    fun `need con fecha fin anterior al inicio falla`() {
        val errors = AgroMatchValidator.validateNeed(
            validNeed().copy(startEpochDay = 20_004L, endEpochDay = 20_000L)
        )
        assertTrue(errors.contains(AgroMatchValidator.NeedError.EndDateBeforeStart))
    }

    @Test
    fun `need con inicio y fin iguales es valida`() {
        val errors = AgroMatchValidator.validateNeed(
            validNeed().copy(startEpochDay = 20_000L, endEpochDay = 20_000L)
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `need acumula varios errores a la vez`() {
        val need = Need(
            farmerId = 0L,
            cropCode = "NO_EXISTE",
            zoneCode = "NO_EXISTE",
            hectares = -1.0,
            startEpochDay = 20_000L,
            endEpochDay = 19_000L
        )
        val errors = AgroMatchValidator.validateNeed(need)
        assertEquals(5, errors.size)
    }

    // ---------- Rating ----------

    @Test
    fun `rating valido sobre interes completado no devuelve errores`() {
        val errors = AgroMatchValidator.validateRating(
            validRating(),
            InterestStatus.COMPLETED.value
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `rating con score 0 falla`() {
        val score = 0
        val isValid = score in Rating.MIN_SCORE..Rating.MAX_SCORE
        assertFalse("El score 0 debería ser inválido", isValid)
    }

    @Test
    fun `rating con score 6 falla`() {
        val score = 6
        val isValid = score in Rating.MIN_SCORE..Rating.MAX_SCORE
        assertFalse("El score 6 debería ser inválido", isValid)
    }

    @Test
    fun `rating con comentario demasiado largo falla`() {
        val comment = "x".repeat(Rating.MAX_COMMENT_LENGTH + 1)
        val errors = AgroMatchValidator.validateRating(
            validRating(comment = comment),
            InterestStatus.COMPLETED.value
        )
        assertTrue(errors.contains(AgroMatchValidator.RatingError.CommentTooLong))
    }

    @Test
    fun `rating con comentario en el limite es valido`() {
        val comment = "x".repeat(Rating.MAX_COMMENT_LENGTH)
        val errors = AgroMatchValidator.validateRating(
            validRating(comment = comment),
            InterestStatus.COMPLETED.value
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `rating sobre interes activo falla`() {
        val errors = AgroMatchValidator.validateRating(
            validRating(),
            InterestStatus.ACTIVE.value
        )
        assertTrue(errors.contains(AgroMatchValidator.RatingError.InterestNotCompleted))
    }

    // ---------- Completar interés ----------

    @Test
    fun `completar interes activo valido no devuelve errores`() {
        val interest = Interest(
            needId = 1L,
            pilotId = 2L,
            createdEpochDay = 20_000L
        )
        val errors = AgroMatchValidator.validateCompleteInterest(
            interest,
            InterestStatus.ACTIVE.value
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `completar interes ya completado falla`() {
        val interest = Interest(
            needId = 1L,
            pilotId = 2L,
            status = InterestStatus.COMPLETED.value,
            createdEpochDay = 20_000L,
            completedEpochDay = 20_001L
        )
        val errors = AgroMatchValidator.validateCompleteInterest(
            interest,
            InterestStatus.COMPLETED.value
        )
        assertTrue(errors.contains(AgroMatchValidator.InterestError.AlreadyCompleted))
    }

    @Test
    fun `completar interes sin piloto falla`() {
        val interest = Interest(
            needId = 1L,
            pilotId = 0L,
            createdEpochDay = 20_000L
        )
        val errors = AgroMatchValidator.validateCompleteInterest(
            interest,
            InterestStatus.ACTIVE.value
        )
        assertTrue(errors.contains(AgroMatchValidator.InterestError.PilotMissing))
    }
}