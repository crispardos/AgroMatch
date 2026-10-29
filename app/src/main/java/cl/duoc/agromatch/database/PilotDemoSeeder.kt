package cl.duoc.agromatch.database

import cl.duoc.agromatch.model.User
import cl.duoc.agromatch.model.UserRole
import cl.duoc.agromatch.repository.PasswordHasher
import java.util.Locale

class PilotDemoSeeder(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher
) {
    suspend fun seedPilotsIfMissing() {
        val pilots = listOf(
            Triple("juan_dron", "Juan el Dronero", 101L),
            Triple("maria_agri", "Maria Agricola", 102L),
            Triple("pedro_vuela", "Pedro Vuela Alto", 103L)
        )

        pilots.forEach { (alias, _, _) ->
            val aliasKey = alias.lowercase(Locale.ROOT)
            if (userDao.findByAliasKey(aliasKey) == null) {
                val credentials = passwordHasher.createCredentials("Agro1234")
                userDao.insert(
                    User(
                        alias = alias,
                        aliasKey = aliasKey,
                        role = UserRole.PILOT.value,
                        passwordHash = credentials.hash,
                        passwordSalt = credentials.salt
                    )
                )
            }
        }
    }
}