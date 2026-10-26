package cl.duoc.agromatch.model

/**
 * Validaciones de entrada del MVP. Funciones puras, sin dependencias
 * de Android/Compose/Room, para poder probarlas como unit tests.
 *
 * Cada método devuelve una lista de [ValidationError] vacía si todo está bien.
 * La UI decide cómo presentar cada error; el ViewModel solo comprueba
 * `errors.isEmpty()` o expone los errores a la pantalla.
 */
object AgroMatchValidator {

    // ---------- Need ----------

    sealed interface NeedError {
        data object FarmerMissing : NeedError
        data object ServiceTypeInvalid : NeedError
        data object CropInvalid : NeedError
        data object ZoneInvalid : NeedError
        data object HectaresNotFinite : NeedError
        data object HectaresNotPositive : NeedError
        data object StartDateMissing : NeedError
        data object EndDateBeforeStart : NeedError
    }

    fun validateNeed(need: Need): List<NeedError> = buildList {
        if (need.farmerId <= 0L) add(NeedError.FarmerMissing)
        if (need.serviceType !in Catalog.ServiceType.all) add(NeedError.ServiceTypeInvalid)
        if (need.cropCode !in Catalog.Crop.all) add(NeedError.CropInvalid)
        if (need.zoneCode !in Catalog.Zone.all) add(NeedError.ZoneInvalid)

        when {
            !need.hectares.isFinite() -> add(NeedError.HectaresNotFinite)
            need.hectares <= 0.0 -> add(NeedError.HectaresNotPositive)
        }

        // epochDay 0 es un valor válido (1970-01-01); usamos < 0 como "sin fecha".
        if (need.startEpochDay < 0L) add(NeedError.StartDateMissing)
        if (need.endEpochDay < need.startEpochDay) add(NeedError.EndDateBeforeStart)
    }

    // ---------- Rating ----------

    sealed interface RatingError {
        data object ScoreOutOfRange : RatingError
        data object CommentTooLong : RatingError
        data object InterestNotCompleted : RatingError
    }

    /**
     * Valida un rating contra el estado del interés que lo origina.
     * [interestStatus] debe ser el valor String de [InterestStatus].
     */
    fun validateRating(rating: Rating, interestStatus: String): List<RatingError> = buildList {
        if (rating.score !in Rating.MIN_SCORE..Rating.MAX_SCORE) {
            add(RatingError.ScoreOutOfRange)
        }
        val commentLength = rating.comment?.length ?: 0
        if (commentLength > Rating.MAX_COMMENT_LENGTH) {
            add(RatingError.CommentTooLong)
        }
        if (InterestStatus.fromValue(interestStatus) != InterestStatus.COMPLETED) {
            add(RatingError.InterestNotCompleted)
        }
    }

    // ---------- Interest (transición de estado) ----------

    sealed interface InterestError {
        data object PilotMissing : InterestError
        data object NeedMissing : InterestError
        data object AlreadyCompleted : InterestError
    }

    /**
     * Valida que se pueda marcar un interés existente como finalizado.
     * [currentStatus] es el valor String del estado actual.
     */
    fun validateCompleteInterest(
        interest: Interest,
        currentStatus: String
    ): List<InterestError> = buildList {
        if (interest.pilotId <= 0L) add(InterestError.PilotMissing)
        if (interest.needId <= 0L) add(InterestError.NeedMissing)
        if (InterestStatus.fromValue(currentStatus) == InterestStatus.COMPLETED) {
            add(InterestError.AlreadyCompleted)
        }
    }
}