package cl.duoc.agromatch.model

/**
 * Catálogos ficticios fijos del MVP.
 * No son administrables desde la app; existen para evitar strings libres
 * y para que los datos dummy y los filtros usen exactamente los mismos códigos.
 */
object Catalog {

    object ServiceType {
        const val FUMIGATION = "FUMIGATION"
        val all = listOf(FUMIGATION)
    }

    object Crop {
        const val VINEYARD = "VINEYARD"
        const val AVOCADO = "AVOCADO"
        const val WHEAT = "WHEAT"
        const val CORN = "CORN"
        const val APPLE = "APPLE"
        val all = listOf(VINEYARD, AVOCADO, WHEAT, CORN, APPLE)
    }

    object Zone {
        const val COLCHAGUA = "COLCHAGUA"
        const val CURICO = "CURICO"
        const val MAULE = "MAULE"
        const val BIOBIO = "BIOBIO"
        const val VALPARAISO = "VALPARAISO"
        val all = listOf(COLCHAGUA, CURICO, MAULE, BIOBIO, VALPARAISO)
    }

    /** Etiquetas visibles en español para la UI. */
    fun cropLabel(code: String): String = when (code) {
        Crop.VINEYARD -> "Viña"
        Crop.AVOCADO -> "Palto"
        Crop.WHEAT -> "Trigo"
        Crop.CORN -> "Maíz"
        Crop.APPLE -> "Manzano"
        else -> code
    }

    fun zoneLabel(code: String): String = when (code) {
        Zone.COLCHAGUA -> "Colchagua"
        Zone.CURICO -> "Curicó"
        Zone.MAULE -> "Maule"
        Zone.BIOBIO -> "Biobío"
        Zone.VALPARAISO -> "Valparaíso"
        else -> code
    }
}
