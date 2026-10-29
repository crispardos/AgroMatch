package cl.duoc.agromatch.repository

import android.content.Context
import cl.duoc.agromatch.database.AgroMatchDatabase

class AgroMatchContainer(context: Context) {
    private val database = AgroMatchDatabase.getInstance(context)

    val authRepository = AuthRepository(
        context = context.applicationContext,
        userDao = database.userDao()
    )
    val farmerRepository = FarmerRepository(
        needDao = database.needDao(),
        interestDao = database.interestDao(),
        ratingDao = database.ratingDao()
    )
}
