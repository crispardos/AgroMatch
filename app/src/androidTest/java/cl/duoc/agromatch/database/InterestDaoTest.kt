package cl.duoc.agromatch.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.agromatch.model.Catalog
import cl.duoc.agromatch.model.Interest
import cl.duoc.agromatch.model.InterestStatus
import cl.duoc.agromatch.model.Need
import cl.duoc.agromatch.model.Rating
import cl.duoc.agromatch.model.User
import cl.duoc.agromatch.model.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InterestDaoTest {
    private lateinit var database: AgroMatchDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AgroMatchDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun activeRequestAppearsForPilotAndPreviousPilotCanBeSelectedAgain() = runBlocking {
        val farmerId = database.userDao().insert(
            User(alias = "Agricultor", aliasKey = "agricultor", role = UserRole.FARMER.value,
                passwordHash = "hash", passwordSalt = "salt")
        )
        val firstPilotId = database.userDao().insert(
            User(alias = "Piloto Uno", aliasKey = "piloto-uno", role = UserRole.PILOT.value,
                passwordHash = "hash", passwordSalt = "salt")
        )
        val secondPilotId = database.userDao().insert(
            User(alias = "Piloto Dos", aliasKey = "piloto-dos", role = UserRole.PILOT.value,
                passwordHash = "hash", passwordSalt = "salt")
        )
        val needId = database.needDao().insert(
            Need(
                farmerId = farmerId,
                cropCode = Catalog.Crop.VINEYARD,
                zoneCode = Catalog.Zone.COLCHAGUA,
                hectares = 5.0,
                startEpochDay = 20_000L,
                endEpochDay = 20_002L
            )
        )
        val dao = database.interestDao()

        dao.replaceActiveInterest(Interest(needId = needId, pilotId = firstPilotId, createdEpochDay = 20_000L))
        var request = dao.observeRequestsForPilot(firstPilotId).first().single()
        assertEquals("Agricultor", request.farmerAlias)
        assertEquals(needId, request.needId)

        dao.replaceActiveInterest(Interest(needId = needId, pilotId = secondPilotId, createdEpochDay = 20_001L))
        assertEquals(emptyList<PilotRequestSummary>(), dao.observeRequestsForPilot(firstPilotId).first())
        assertEquals(1, dao.observeRequestsForPilot(secondPilotId).first().size)

        dao.replaceActiveInterest(Interest(needId = needId, pilotId = firstPilotId, createdEpochDay = 20_002L))
        request = dao.observeRequestsForPilot(firstPilotId).first().single()
        assertEquals(InterestStatus.ACTIVE.value, dao.getLatestByNeed(needId)?.status)
        assertEquals("Agricultor", request.farmerAlias)

        val completedInterest = dao.getLatestByNeed(needId)!!
        dao.updateStatus(completedInterest.id, InterestStatus.COMPLETED.value, 20_003L)
        database.ratingDao().insert(
            Rating(
                interestId = completedInterest.id,
                score = 5,
                comment = "Buen servicio",
                createdEpochDay = 20_003L
            )
        )

        val overview = database.needDao().observeOverviewsByFarmer(farmerId).first().single()
        assertEquals(InterestStatus.COMPLETED.value, overview.interestStatus)
        assertEquals("Piloto Uno", overview.pilotAlias)
        assertEquals(5, overview.ratingScore)
        assertEquals("Buen servicio", overview.ratingComment)
    }
}
