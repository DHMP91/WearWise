package dhmp.wearwise.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asSnapshot
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import dhmp.wearwise.model.Outfit
import dhmp.wearwise.model.Season
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class OutfitRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var context: Context
    private lateinit var appContainer: DefaultAppContainer

    @Before
    fun setupDatabase() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        appContainer = DefaultAppContainer(context)
        runBlocking {
            appContainer.outfitsRepository.insertOutfit(Outfit())
        }
    }

    @Test
    fun getAllOutfitsPaged() = runTest {
        //Scenario: test paging libary implementation with android room database
        val pageSize = 3
        val inject = 15
        val pager =  Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = {  appContainer.outfitsRepository.getAllOutfitsPaged() }
        ).flow
        var itemsSnapshot: List<Outfit> = pager.asSnapshot()
        val initial = itemsSnapshot.size
        val max = inject + initial

        repeat(inject) {
            appContainer.outfitsRepository.insertOutfit(Outfit())
        }

        itemsSnapshot = pager.asSnapshot()
        Assert.assertTrue(itemsSnapshot.size >= pageSize && itemsSnapshot.size <= pageSize*3)

        itemsSnapshot = pager.asSnapshot{
            scrollTo(pageSize + 1)
        }
        Assert.assertTrue(itemsSnapshot.size >= pageSize && itemsSnapshot.size <= pageSize*3)

        itemsSnapshot = pager.asSnapshot {
            scrollTo(index = pageSize*3 + 1)
        }
        Assert.assertTrue(itemsSnapshot.size == inject )


        itemsSnapshot = pager.asSnapshot {
            scrollTo(index = max - 1)
        }
        Assert.assertTrue(itemsSnapshot.size == max)

        val pager2 =  Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {  appContainer.outfitsRepository.getAllOutfitsPaged() }
        ).flow

        itemsSnapshot = pager2.asSnapshot {
            scrollTo(index = 1)
        }
        Assert.assertTrue(itemsSnapshot.size == max)
    }

    @Test
    fun getOutfitsByListOfIdsPaged_ValidId() = runTest {
        val pager =  Pager(
            config = PagingConfig(pageSize = 2, enablePlaceholders = false),
            pagingSourceFactory = {  appContainer.outfitsRepository.getOutfitsByListOfIdsPaged(listOf(1)) }
        ).flow

        val itemsSnapshot: List<Outfit> = pager.asSnapshot {
            scrollTo(1)
        }
        Assert.assertTrue(itemsSnapshot.size == 1)
    }

    @Test
    fun getOutfitsByListOfIdsPaged_NonExistentId() = runTest {
        val pager =  Pager(
            config = PagingConfig(pageSize = 2, enablePlaceholders = false),
            pagingSourceFactory = {  appContainer.outfitsRepository.getOutfitsByListOfIdsPaged(listOf(99991,99992,99993)) }
        ).flow

        val itemsSnapshot: List<Outfit> = pager.asSnapshot()
        Assert.assertTrue(itemsSnapshot.isEmpty())
    }

    @Test
    fun getAllOutfitsStream() = runTest {
        val outfits = appContainer.outfitsRepository.getAllOutfitsStream().firstOrNull()
        Assert.assertTrue(outfits!!.isNotEmpty())
    }

    @Test
    fun getOutfitStream() = runTest {
        val id = appContainer.outfitsRepository.insertOutfit(Outfit())
        val outfit = appContainer.outfitsRepository.getOutfitStream(id).firstOrNull()
        Assert.assertTrue(outfit != null)
    }

    @Test
    fun deleteOutfit() = runTest {
        val id = appContainer.outfitsRepository.insertOutfit(Outfit())
        val outfit = appContainer.outfitsRepository.getOutfitStream(id).firstOrNull()
        appContainer.outfitsRepository.deleteOutfit(outfit!!)
        val deletedOutfit = appContainer.outfitsRepository.getOutfitStream(id).firstOrNull()
        Assert.assertTrue(deletedOutfit == null)
    }

    @Test
    fun updateOutfit() = runTest {
        val id = appContainer.outfitsRepository.insertOutfit(Outfit())
        val outfit = appContainer.outfitsRepository.getOutfitStream(id).first()

        outfit!!.image = "image"
        outfit.garmentsId = listOf(1,2,3,4)
        outfit.name = "outfitName"
        outfit.season = Season.WINTER
        appContainer.outfitsRepository.updateOutfit(outfit)

        val updatedOutfit = appContainer.outfitsRepository.getOutfitStream(id).firstOrNull()
        Assert.assertTrue(updatedOutfit!! == outfit)
    }

    @After
    fun closeDatabase() {
        database.close()
    }
}