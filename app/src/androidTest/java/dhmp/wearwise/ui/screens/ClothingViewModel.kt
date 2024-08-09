package dhmp.wearwise.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.testing.asSnapshot
import androidx.test.platform.app.InstrumentationRegistry
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.ui.screens.clothing.ClothingViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
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


class ClothingViewModelTest {
    private lateinit var mockedGarmentRepo: GarmentsRepository
    private lateinit var context: Context
    lateinit var model: ClothingViewModel
    @Captor
    private lateinit var updateGarmentCaptor: ArgumentCaptor<Garment>
    private val garmentAmount = 20

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        model = ClothingViewModel(mockedGarmentRepo)
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

        val modelGarment = withTimeoutOrNull(15000) {
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
        val fakeGarment = Garment(
            id = 10L,
            color = "SomeColor"
        )
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
        var fakeGarments = mutableListOf<Garment>()
        repeat(garmentAmount){
            fakeGarments = fakeGarments.plus(Garment(id = it.toLong(), categoryId = 99)).toMutableList()
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

//    @Test
//    fun saveChanges(){
//        throw NotImplementedError()
//    }
//
//    @Test
//    fun analyzeGarment(){
//        throw NotImplementedError()
//    }
//
//    @Test
//    fun removeBackGround(){
//        throw NotImplementedError()
//    }
//
//
//    @Test
//    fun deleteGarment(){
//        throw NotImplementedError()
//    }
//
//    @Test
//    fun collectBrands(){
//        throw NotImplementedError()
//    }
}

private class FakePagingSource(
    private val garments: List<Garment>
) : PagingSource<Int, Garment>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Garment> {
        return LoadResult.Page(
            data = garments,
            prevKey = null,
            nextKey = null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Garment>) = null
}


private fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
