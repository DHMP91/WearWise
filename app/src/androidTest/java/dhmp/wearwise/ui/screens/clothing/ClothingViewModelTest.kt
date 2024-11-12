package dhmp.wearwise.ui.screens.clothing

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.paging.testing.asSnapshot
import androidx.test.platform.app.InstrumentationRegistry
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.UserConfigRepository
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.ui.screens.FakePagingSource
import dhmp.wearwise.ui.screens.capture
import dhmp.wearwise.ui.screens.fakeImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.io.File
import kotlin.time.Duration.Companion.minutes


class ClothingViewModelTest {
    @Captor
    private lateinit var updateGarmentCaptor: ArgumentCaptor<Garment>
    private lateinit var mockedGarmentRepo: GarmentsRepository
    private lateinit var mockedUserConfigRepo: UserConfigRepository
    private lateinit var context: Context
    private lateinit var model: ClothingViewModel
    private lateinit var testDispatcher: CoroutineDispatcher

    private val garmentAmount = 20
    private val defaultTimeout = 5000L //millisecond


    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        mockedUserConfigRepo = Mockito.mock(UserConfigRepository::class.java)
        `when`(mockedUserConfigRepo.getUserConfigStream()).thenAnswer {
            flow { emit(null) }
        }
        testDispatcher = StandardTestDispatcher()
        model = ClothingViewModel(mockedGarmentRepo, mockedUserConfigRepo)
    }

    @Test
    fun getGarmentById() = runBlocking {
        val fakeGarment = Garment(
            id = 10L,
            color = "SomeColor"
        )
        `when`(mockedGarmentRepo.getGarmentStream(3L)).thenReturn(
            flow {  emit(fakeGarment) }
        )
        model.getGarmentById(3L)

        val modelGarment = withTimeoutOrNull(defaultTimeout) {
            model.uiEditState
                .map { it.editGarment }
                .first { it == fakeGarment }
        }
        Assert.assertNotNull(modelGarment)
        Assert.assertEquals(fakeGarment, modelGarment)
    }

    @Test
    fun saveImage() {
        val width = 1000
        val height = 1000
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val appDir = context.filesDir
        val id = 99L
        updateGarmentCaptor = ArgumentCaptor.forClass(Garment::class.java)

        runBlocking {
            `when`(mockedGarmentRepo.insertGarment(any())).thenReturn(id)
            `when`(mockedGarmentRepo.saveImageToStorage(any(), any())).thenReturn(Uri.EMPTY)
            val job = model.saveImage(appDir, bitmap, 0f)
            job.join()

            verify(mockedGarmentRepo, times(1)).updateGarment(capture(updateGarmentCaptor))
        }

        runTest {
            Assert.assertEquals(id, model.newItemId.first())
            val capturedGarment = updateGarmentCaptor.value
            val expectedGarment = Garment(id = id, image = Uri.EMPTY.toString())
            Assert.assertEquals(expectedGarment, capturedGarment)
        }
    }


    @Test
    fun storeChanges() = runBlocking {
        val expectedGarmentStored = Garment(id = 77, image = Uri.EMPTY.toString())
        model.storeChanges(expectedGarmentStored)
        Assert.assertTrue(model.uiEditState.value.changes!! == expectedGarmentStored)
        Assert.assertTrue(model.uiEditState.value.editGarment == Garment())
    }


    @Test
    fun showMenu() = runBlocking {
        model.showMenu(true)
        Assert.assertTrue(model.uiMenuState.value.showMenu)
        model.showMenu(false)
        Assert.assertTrue(!model.uiMenuState.value.showMenu)
    }

    @Test
    fun showBrandFilterMenu() = runBlocking {
        model.showBrandFilterMenu(true)
        Assert.assertTrue(model.uiMenuState.value.showBrandFilterMenu)
        model.showBrandFilterMenu(false)
        Assert.assertTrue(!model.uiMenuState.value.showBrandFilterMenu)
    }


    @Test
    fun showColorFilterMenu() = runBlocking {
        model.showColorFilterMenu(true)
        Assert.assertTrue(model.uiMenuState.value.showColorFilterMenu)
        model.showColorFilterMenu(false)
        Assert.assertTrue(!model.uiMenuState.value.showColorFilterMenu)
    }

    @Test
    fun showCategoryFilterMenu() = runBlocking {
        model.showCategoryFilterMenu(true)
        Assert.assertTrue(model.uiMenuState.value.showCategoryFilterMenu)
        model.showCategoryFilterMenu(false)
        Assert.assertTrue(!model.uiMenuState.value.showCategoryFilterMenu)
    }

    @Test
    fun modBrandFilter() = runBlocking {
        val brandName = "ThisBrand"
        model.addBrandToFilter(brandName)
        Assert.assertTrue(model.uiMenuState.value.filterExcludeBrands == listOf(brandName))
        model.removeBrandFromFilter(brandName)
        Assert.assertTrue(model.uiMenuState.value.filterExcludeBrands == listOf<String>())
    }

    @Test
    fun modCategoryFilter() = runBlocking {
        val c = Category.categories().first()
        model.addCategoryToFilter(c)
        Assert.assertTrue(model.uiMenuState.value.filterExcludeCategories == listOf(c))
        model.removeCategoryFromFilter(c)
        Assert.assertTrue(model.uiMenuState.value.filterExcludeCategories == listOf<Category>())
    }

    @Test
    fun modColorFilter() = runBlocking {
        val color = "red"
        model.addColorToFilter(color)
        Assert.assertTrue(model.uiMenuState.value.filterExcludeColors == listOf(color))
        model.removeColorFromFilter(color)
        Assert.assertTrue(model.uiMenuState.value.filterExcludeColors == listOf<Category>())
    }

    @Test
    fun getGarmentsByCategory() = runBlocking {
        val params = listOf(99, 0, 999999, null)
        val fakeGarments = mutableListOf<Garment>()
        repeat(garmentAmount){
            fakeGarments.add(Garment(id = it.toLong(), categoryId = 99))
        }
        for(param in params) {
            `when`(mockedGarmentRepo.getGarmentsByCategoryPaged(param)).thenAnswer{
                FakePagingSource(fakeGarments)
            }
            val data = model.getGarmentsByCategory(param)
            val itemsSnapshot: List<Garment> = data.asSnapshot {
                scrollTo(1)
            }

            Assert.assertTrue(itemsSnapshot.isNotEmpty())
            Assert.assertEquals(
                garmentAmount,
                itemsSnapshot.size
            )
        }
    }

    @Test
    fun getGarmentCount() = runBlocking{
        val params = listOf(
            listOf(listOf(), listOf(), listOf()),
            listOf(listOf(Category.categories().first()), listOf("a"), listOf("b"))
        )
        for(param in params) {
            `when`(mockedGarmentRepo.getGarmentsCount(any(), any(), any())).thenAnswer {
                flow { emit(2) }
            }
            val categories = param[0] as List<Category>
            val colors =  param[1] as List<String>
            val brands =  param[2] as List<String>
            val ret = model.getGarmentsCount(categories, colors, brands)
            Assert.assertEquals(2, ret.first())
        }
    }

    @Test
    fun getGarmentThumbnail() = runBlocking {
        val tests = listOf(
            "",
            null,
            "thumbnailUri",
            "a"
        )

        for(test in tests) {
            val garment = Garment(id = 123, image = "imageUri")
            `when`(mockedGarmentRepo.getGarmentThumbnail(garment)).thenAnswer {
                test
            }

            val ret = model.getGarmentThumbnail(garment).first()
            if(test.isNullOrEmpty()){
                Assert.assertTrue(ret == "imageUri")
            } else {
                Assert.assertTrue(ret == test)
            }
        }
    }

    @Test
    fun saveChanges() = runBlocking {
        val garment = Garment(id = 123, image = "imageUri")
        model.storeChanges(garment)

        withTimeoutOrNull(defaultTimeout) {
            while (model.uiEditState.first().changes == null){
                Log.d("saveChangesTest", "waiting for change state to not be null")
            }
        }
        assert(model.uiEditState.first().changes != null)
        `when`(mockedGarmentRepo.updateGarment(garment)).thenAnswer {}
        model.saveChanges(garment)

        withTimeoutOrNull(defaultTimeout) {
            while (model.uiEditState.first().changes != null){
                Log.d("saveChangesTest", "waiting for change state to be null")
            }
        }
        assert(model.uiEditState.first().changes == null)
    }

    @Test
    fun analyzeGarment() = runTest(timeout = 9.minutes){
        val tests = mapOf(
            0xFFDC143C.toInt() to "Red", // Crimson
            0xFFC0C0C0.toInt() to "White", // Silver
            0xFF00FF00.toInt() to "Green", // Lime
            0xFF000042.toInt() to "Blue", // Navy
            0xFFFFD700.toInt() to "Yellow", // Gold
        )
        for(test in tests) {
            val colorCode = test.key
            val expectedColorMatch = test.value
            val fileName = "test_analyzeGarment_${colorCode}.png"
            val file = fakeImage(context, fileName, fillColor = colorCode)

            val fakeGarment = Garment(id = 9090, imageOfSubject = file.toURI().toString())
            `when`(mockedGarmentRepo.getGarmentStream(any())).thenAnswer {
                flow {
                    emit(fakeGarment)
                }
            }

            model.analyzeGarment(fakeGarment.id)

            while (model.uiEditState.first().changes == null) {
                runBlocking {
                    delay(500)
                    Log.d("analyzeGarment", "waiting for change state to not be null")
                }
            }

            val detectedColor = model.uiEditState.first().changes?.color
            val matches = detectedColor == expectedColorMatch
            Assert.assertTrue(matches)

            model.storeChanges(null)
            while (model.uiEditState.first().changes != null){
                runBlocking {
                    delay(500)
                    Log.d("saveChangesTest", "waiting for change state to be null")
                }
            }

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteGarment() {
        val fakedFiles = mutableListOf<File>()
        val model = ClothingViewModel(mockedGarmentRepo, mockedUserConfigRepo, testDispatcher)
        repeat(5) {
            val fileName = "test_analyzeGarment_deleteGarment_${it}.png"
            val file = fakeImage(context, fileName)
            fakedFiles.add(file)
        }

        val tests = listOf(
            Garment(id = 9091, image = fakedFiles[0].toURI().toString(), imageOfSubject = fakedFiles[1].toUri().toString()),
            Garment(id = 9092, image = null, imageOfSubject = fakedFiles[2].toUri().toString()),
            Garment(id = 9093, image = null, imageOfSubject = null),
            Garment(id = 9094, image = fakedFiles[3].toURI().toString(), imageOfSubject = null),
            Garment(id = 9095, image = "${fakedFiles[4].toURI()}_doesNotExists", imageOfSubject = fakedFiles[4].toUri().toString())
        )

        for(fakeGarment in tests) {
            runTest(testDispatcher){
                `when`(mockedGarmentRepo.getGarmentStream(any())).thenAnswer {
                    flow {
                        emit(fakeGarment)
                    }
                }
                `when`(mockedGarmentRepo.deleteGarment(fakeGarment)).thenAnswer {}

                model.deleteGarment(fakeGarment.id)
                advanceUntilIdle()
                verify(mockedGarmentRepo, times(1)).deleteGarment(fakeGarment)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun collectBrands(){
        val model = ClothingViewModel(mockedGarmentRepo, mockedUserConfigRepo, testDispatcher)
        val brands = listOf("Brand1", "Brand2", "Brand3")
        runTest(testDispatcher) {
            `when`(mockedGarmentRepo.getBrands()).thenAnswer {
                flow {
                    emit(brands)
                }
            }

            model.collectBrands()
            advanceUntilIdle()
            Assert.assertTrue(model.brands.first().containsAll(brands))
        }
    }
}


