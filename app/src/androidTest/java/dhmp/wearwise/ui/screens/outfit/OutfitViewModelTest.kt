package dhmp.wearwise.ui.screens.outfit

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import androidx.paging.testing.asSnapshot
import androidx.test.platform.app.InstrumentationRegistry
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.OutfitsRepository
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.Outfit
import dhmp.wearwise.model.Season
import dhmp.wearwise.ui.screens.FakePagingSource
import dhmp.wearwise.ui.screens.fakeImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.atMost
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.minutes

class OutfitViewModelTest {
    private lateinit var mockedGarmentRepo: GarmentsRepository
    private lateinit var mockedOutfitRepo: OutfitsRepository
    private lateinit var context: Context
    private lateinit var model: OutfitViewModel
    private lateinit var testDispatcher: CoroutineDispatcher


    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        mockedOutfitRepo = Mockito.mock(OutfitsRepository::class.java)
        testDispatcher = StandardTestDispatcher()
        model = OutfitViewModel(mockedGarmentRepo, mockedOutfitRepo)
    }


    @Test
    fun getOutfitsByListOfId() {
        val outfitIds = listOf<Long>(1, 2, 3)
        val outfits = listOf<Outfit>(
            Outfit(
                id = 1,
                image = "some image uri string",
                garmentsId = listOf(1, 2, 3),
                season = Season.ANY
            ),
            Outfit(
                id = 2,
                image = "some image uri string",
                garmentsId = listOf(1, 2, 3),
                season = Season.ANY
            )
        )
        Mockito.`when`(
            mockedOutfitRepo.getOutfitsByListOfIdsPaged(outfitIds)
        ).thenAnswer {
            FakePagingSource(outfits)
        }

        val data = model.getOutfitsByListOfId(outfitIds)

        runBlocking {
            val itemsSnapshot: List<Outfit> = data.asSnapshot {
                scrollTo(1)
            }
            Assert.assertFalse(itemsSnapshot.isEmpty())
            Assert.assertTrue(itemsSnapshot.first() == outfits.first())
        }
    }

    @Test
    fun getGarments() {

        val fakeGarments = listOf(
            Garment(id = 1, imageOfSubject = ""),
            Garment(id = 2, imageOfSubject = ""),
        )

        val fakeOutfit = Outfit(
            id = 9090,
            garmentsId = fakeGarments.map { it.id }
        )
        for(g in fakeGarments) {
            Mockito.`when`(mockedGarmentRepo.getGarmentStream(g.id)).thenAnswer {
                flow {
                    emit(g)
                }
            }
        }

        runBlocking {
            val flow = model.getGarments(fakeOutfit)
            val data = flow.first()
            Assert.assertTrue(data.isNotEmpty())
            for(g in fakeGarments) {
                val garment = data.find { it.id == g.id  }
                Assert.assertNotNull(garment)
            }
        }
    }

    @Test
    fun getOutfitThumbnail() {
        val fakeOutfit = Outfit(
            id = 9090,
            image = "outfitImage"
        )
        val tests: List<String?> = listOf("", null, "thumbnail")
        runBlocking {
            for(test in tests) {
                Mockito.`when`(mockedOutfitRepo.getOutfitThumbnail(fakeOutfit)).thenAnswer {
                    test
                }
                val flow = model.getOutfitThumbnail(fakeOutfit)
                val data = flow.first()
                if(test.isNullOrEmpty()){
                    Assert.assertTrue(data == fakeOutfit.image)
                }else{
                    Assert.assertTrue(data == test)
                }
            }
        }
    }

    @Test
    fun saveImage() {
        val width = 1000
        val height = 1000
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val appDir = context.filesDir

        val tests =  listOf<Long?>(1, null, 0)
        for(id in tests) {
            val file = File(appDir, "test_saveImage")
            if (file.exists()) {
                file.delete()
            }
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            if(!(id == null || id == 0L)) {
                val outfit = Outfit(
                    id = id,
                    image = file.toUri().toString()
                )
                runBlocking {
                    Mockito.`when`(mockedOutfitRepo.getOutfitStream(id)).thenAnswer {
                        flow {
                            emit(outfit)
                        }
                    }
                    Mockito.`when`(mockedOutfitRepo.updateOutfit(any())).thenReturn(Unit)
                    Mockito.`when`(mockedGarmentRepo.saveImageToStorage(appDir, bitmap)).thenReturn(
                        Uri.EMPTY
                    )

                    val job = model.saveImage(appDir, bitmap, 0F, id)
                    job.join()
                    //verify delete file
                    Assert.assertFalse(file.exists())

                    //verify call outfitsRepository.updateOutfit(it)
                    verify(mockedOutfitRepo, atLeastOnce()).updateOutfit(outfit)
                    verify(mockedOutfitRepo, atMost(2)).updateOutfit(outfit)
                    //verify call garmentRepository.saveImageToStorage
                    verify(mockedOutfitRepo, atLeastOnce()).updateOutfit(outfit)
                    verify(mockedOutfitRepo, atMost(2)).updateOutfit(outfit)
                }
            }else{
                runBlocking {
                    val job = model.saveImage(appDir, bitmap, 0F, id)
                    job.join()
                    Assert.assertTrue(file.exists())
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getOutfit_newOutfit(){
        val id: Long = 9090
        val outfit = Outfit(
            id = id
        )
        val model =  OutfitViewModel(mockedGarmentRepo, mockedOutfitRepo, testDispatcher)
        runTest(testDispatcher, timeout = 1.minutes) {
            Mockito.`when`(mockedOutfitRepo.getOutfitStream(id)).thenAnswer {
                flow {
                    emit(outfit)
                }
            }

            model.getOutfit(id)
            advanceUntilIdle()
            Assert.assertTrue(model.outfit.first()?.id == id)

            model.newOutfit()
            advanceUntilIdle()
            Assert.assertTrue(model.outfit.first()?.id == 0L)

        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun saveOutfit_new(){
        val newId: Long = 9090
        val garmentOne = Garment(
            id = 10
        )
        val garmentTwo = Garment(
            id = 11
        )
        val model =  OutfitViewModel(mockedGarmentRepo, mockedOutfitRepo, testDispatcher)
        runTest(testDispatcher, timeout = 1.minutes) {
            Mockito.`when`(mockedOutfitRepo.insertOutfit(any())).thenAnswer {
                newId
            }
            Mockito.`when`(mockedGarmentRepo.getGarmentStream(garmentOne.id)).thenAnswer {
                flow {
                    emit(garmentOne)
                }
            }
            Mockito.`when`(mockedGarmentRepo.getGarmentStream(garmentTwo.id)).thenAnswer {
                flow {
                    emit(garmentTwo)
                }
            }
            Mockito.`when`(mockedOutfitRepo.getOutfitStream(newId)).thenAnswer {
                flow {
                    emit(
                        Outfit(
                            id = newId,
                            garmentsId = listOf(garmentOne.id, garmentTwo.id)
                        )
                    )
                }
            }

            //Verify saving new outfit
            Assert.assertTrue(model.savedOutfitFlag.value)
            model.newOutfit()
            model.addToOutfit(garmentOne)
            model.addToOutfit(garmentTwo)
            advanceUntilIdle()
            Assert.assertFalse(model.savedOutfitFlag.value)
            Assert.assertTrue(model.outfit.value?.id == 0L)

            model.saveOutfit()
            advanceUntilIdle()
            verify(mockedGarmentRepo, times(1)).updateGarment(
                Garment(
                    id = garmentOne.id,
                    outfitsId = listOf(newId)
                )
            )
            verify(mockedGarmentRepo, times(1)).updateGarment(
                Garment(
                    id = garmentTwo.id,
                    outfitsId = listOf(newId)
                )
            )
            Assert.assertTrue(model.outfit.value?.id == newId)
            Assert.assertTrue(model.savedOutfitFlag.value)
        }

    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun saveOutfit_existing(){
        val newId: Long = 9090
        val garmentOne = Garment(
            id = 10,
            outfitsId = listOf(newId)
        )
        val garmentTwo = Garment(
            id = 11
        )
        val garmentThree = Garment(
            id = 12
        )
        val model =  OutfitViewModel(mockedGarmentRepo, mockedOutfitRepo, testDispatcher)
        runTest(testDispatcher, timeout = 1.minutes) {
            Mockito.`when`(mockedGarmentRepo.getGarmentStream(garmentOne.id)).thenAnswer {
                flow {
                    emit(garmentOne)
                }
            }
            Mockito.`when`(mockedGarmentRepo.getGarmentStream(garmentTwo.id)).thenAnswer {
                flow {
                    emit(garmentTwo)
                }
            }
            Mockito.`when`(mockedGarmentRepo.getGarmentStream(garmentThree.id)).thenAnswer {
                flow {
                    emit(garmentThree)
                }
            }
            Mockito.`when`(mockedOutfitRepo.getOutfitStream(newId)).thenAnswer {
                flow {
                    emit(
                        Outfit(
                            id = newId,
                            garmentsId = listOf(garmentOne.id, garmentTwo.id)
                        )
                    )
                }
            }

            //Verify Save existing outfit
            model.getOutfit(newId)
            advanceUntilIdle()
            model.removeFromOutfit(garmentOne)
            model.addToOutfit(garmentThree)
            advanceUntilIdle()
            Assert.assertFalse(model.savedOutfitFlag.value)
            model.saveOutfit()
            advanceUntilIdle()
            verify(mockedGarmentRepo, times(1)).updateGarment(
                Garment(
                    id = garmentOne.id,
                    outfitsId = listOf()
                )
            )
            verify(mockedGarmentRepo, times(1)).updateGarment(
                Garment(
                    id = garmentThree.id,
                    outfitsId = listOf(newId)
                )
            )
            verify(mockedGarmentRepo, times(1)).updateGarment(
                Garment(
                    id = garmentTwo.id,
                    outfitsId = listOf(newId)
                )
            )
            Assert.assertTrue(model.outfit.value?.id == newId)
            Assert.assertTrue(model.savedOutfitFlag.value)
        }
    }

    @Test
    fun deleteOutfit() {
        val newId: Long = 9090
        val garmentOne = Garment(
            id = 10,
            outfitsId = listOf(newId)
        )
        val garmentTwo = Garment(
            id = 11,
            outfitsId = listOf(newId)
        )

        val file = fakeImage(context, "test_deleteOutfit")
        val model =  OutfitViewModel(mockedGarmentRepo, mockedOutfitRepo, testDispatcher)
        runTest(testDispatcher, timeout = 1.minutes) {
            Mockito.`when`(mockedGarmentRepo.getGarmentStream(garmentOne.id)).thenAnswer {
                flow {
                    emit(
                        Garment(
                            id = garmentOne.id,
                            outfitsId = listOf(newId)
                        )
                    )
                }
            }
            Mockito.`when`(mockedGarmentRepo.getGarmentStream(garmentTwo.id)).thenAnswer {
                flow {
                    emit(garmentTwo)
                }
            }
            Mockito.`when`(mockedOutfitRepo.getOutfitStream(newId)).thenAnswer {
                flow {
                    emit(
                        Outfit(
                            id = newId,
                            garmentsId = listOf(garmentOne.id, garmentTwo.id),
                            image = file.toUri().toString()
                        )
                    )
                }
            }

            //Verify garment is updated and image is delete
            model.deleteOutfit(newId)
            advanceUntilIdle()
            verify(mockedGarmentRepo, times(1)).updateGarment(
                Garment(
                    id = garmentOne.id,
                    outfitsId = listOf()
                )
            )
            verify(mockedGarmentRepo, times(1)).updateGarment(
                Garment(
                    id = garmentTwo.id,
                    outfitsId = listOf()
                )
            )
            Assert.assertFalse(file.exists())

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setSeason() {
        val id: Long = 9090
        val outfit = Outfit(
            id = id
        )
        val model =  OutfitViewModel(mockedGarmentRepo, mockedOutfitRepo, testDispatcher)
        runTest(testDispatcher, timeout = 1.minutes) {
            Mockito.`when`(mockedOutfitRepo.getOutfitStream(id)).thenAnswer {
                flow {
                    emit(outfit)
                }
            }

            model.getOutfit(id)
            model.setSeason(Season.SPRING)

            advanceUntilIdle()
            Assert.assertTrue(model.outfit.first()?.season == Season.SPRING)
            Assert.assertFalse(model.savedOutfitFlag.value)
        }
    }

}