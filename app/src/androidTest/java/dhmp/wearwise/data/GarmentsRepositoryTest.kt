package dhmp.wearwise.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asSnapshot
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.model.Occasion
import dhmp.wearwise.ui.screens.fakeImage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class GarmentsRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var context: Context
    private lateinit var appContainer: DefaultAppContainer
    private val firstCategoryId = Category.categories()[0].id
    private val secondCategoryId = Category.categories()[1].id
    private val thirdCategoryId = Category.categories()[3].id
    private val fourthCategoryId = Category.categories()[4].id

    private val firstColor = GarmentColorNames[0].name
    private val secondColor = GarmentColorNames[1].name
    private val thirdColor = GarmentColorNames[3].name
    private val fourthColor = GarmentColorNames[4].name

    @Before
    fun setupDatabase() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        appContainer = DefaultAppContainer(context)
        runBlocking {
            appContainer.garmentsRespository.insertGarment(Garment())
        }
    }

    @Test
    fun getAllGarmentsPaged() = runTest {
        //Scenario: test paging libary implementation with android room database
        val pageSize = 3
        val inject = 15
        val pager =  Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = {  appContainer.garmentsRespository.getAllGarmentsPaged() }
        ).flow

        var itemsSnapshot: List<Garment> = pager.asSnapshot()
        Assert.assertTrue(itemsSnapshot.isNotEmpty())

        repeat(inject) {
            appContainer.garmentsRespository.insertGarment(Garment())
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
            scrollTo(index = inject - 1)
        }
        Assert.assertTrue(itemsSnapshot.size >= inject)

        val pager2 =  Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {  appContainer.garmentsRespository.getAllGarmentsPaged() }
        ).flow

        itemsSnapshot = pager2.asSnapshot {
            scrollTo(index = 1)
        }
        Assert.assertTrue(itemsSnapshot.size >= inject)
    }

    @Test
    fun getGarmentsByCategoryPaged_ValidCategoryId() = runTest {
        val pager =  Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                appContainer.garmentsRespository.getGarmentsByCategoryPaged(firstCategoryId)
            }
        ).flow
        val inject = 2
        repeat(inject) {
            appContainer.garmentsRespository.insertGarment(
                Garment(
                    categoryId = firstCategoryId
                ))
        }
        val itemsSnapshot = pager.asSnapshot {
            scrollTo(index = 1)
        }
        Assert.assertTrue(itemsSnapshot.size == inject)
    }

    @Test
    fun getGarmentsByCategoryPaged_NullCategoryId() = runTest {
        val pager =  Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                appContainer.garmentsRespository.getGarmentsByCategoryPaged(null)
            }
        ).flow

        val itemsSnapshot = pager.asSnapshot {
            scrollTo(index = 1)
        }
        Assert.assertTrue(itemsSnapshot.filter{ it.categoryId == null }.isNotEmpty())
    }

    @Test
    fun getGarmentsByCategoryPaged_InvalidCategoryId() = runTest {
        val pager =  Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                appContainer.garmentsRespository.getGarmentsByCategoryPaged(999999)
            }
        ).flow

        val itemsSnapshot = pager.asSnapshot {
            scrollTo(index = 1)
        }
        Assert.assertTrue(itemsSnapshot.isEmpty())
    }


    @Test
    fun getFilteredGarments_Category() = runTest {
        val categoryBeforeCount = appContainer.garmentsRespository.getCategoryCount().first()
        val pager =  Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                appContainer.garmentsRespository.getFilteredGarments(
                    Category.categories().filter { it.id != secondCategoryId && it.id != thirdCategoryId }
                )
            }
        ).flow

        repeat(2) {
            appContainer.garmentsRespository.insertGarment(
                Garment(
                    categoryId = secondCategoryId
                ))
        }

        repeat(3) {
            appContainer.garmentsRespository.insertGarment(
                Garment(
                    categoryId = thirdCategoryId
                ))
        }

        val itemsSnapshot = pager.asSnapshot {
            scrollTo(index = 1)
        }

        var beforeSum = 0
        beforeSum += categoryBeforeCount.find { it.categoryId == secondCategoryId }?.count ?: 0
        beforeSum += categoryBeforeCount.find { it.categoryId == thirdCategoryId }?.count ?: 0
        Assert.assertTrue(itemsSnapshot.filter { it.categoryId != null }.size == 5 + beforeSum)
    }

    @Test
    fun getFilteredGarments_Color() = runTest {
        val pager =  Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                appContainer.garmentsRespository.getFilteredGarments(
                    excludedColors = GarmentColorNames.filter { it.name != firstColor }.map { it.name }
                )
            }
        ).flow

        repeat(3) {
            appContainer.garmentsRespository.insertGarment(
                Garment(
                    color = firstColor
                ))
        }


        repeat(2) {
            appContainer.garmentsRespository.insertGarment(
                Garment(
                    color = GarmentColorNames.last().name
                ))
        }

        val itemsSnapshot = pager.asSnapshot {
            scrollTo(index = 1)
        }
        Assert.assertTrue(itemsSnapshot.filter { it.color != null }.size == 3)
    }

    @Test
    fun getFilteredGarments_Brand() = runTest {
        val filteredString = "Gibberish"
        val pager =  Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                appContainer.garmentsRespository.getFilteredGarments(
                    excludedBrands = listOf(filteredString.lowercase())
                )
            }
        ).flow

        val notFilteredGarment = Garment()
        notFilteredGarment.brand = "Acorn"
        repeat(3) {
            appContainer.garmentsRespository.insertGarment(notFilteredGarment)
        }

        val filteredGarment = Garment()
        filteredGarment.brand = filteredString
        repeat(2) {
            appContainer.garmentsRespository.insertGarment(filteredGarment)
        }

        val itemsSnapshot = pager.asSnapshot {
            scrollTo(index = 1)
        }
        Assert.assertTrue(itemsSnapshot.filter { !it.brand.isNullOrEmpty() }.size == 3)
    }

    @Test
    fun getFilteredGarments_All() = runTest {
        val filteredString = "Gibberish2"
        val pager =  Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                appContainer.garmentsRespository.getFilteredGarments(
                    Category.categories().filter { it.id != fourthCategoryId },
                    GarmentColorNames.filter { it.name != secondColor }.map { it.name },
                    listOf(filteredString.lowercase())
                )
            }
        ).flow

        repeat(1) {
            appContainer.garmentsRespository.insertGarment(
                Garment(
                    categoryId = fourthCategoryId
                ))
        }

        repeat(2) {
            appContainer.garmentsRespository.insertGarment(
                Garment(
                    color = secondColor
                ))
        }


        repeat(3) {
            appContainer.garmentsRespository.insertGarment(
                Garment(
                    color = GarmentColorNames.last().name
                ))
        }

        val notFilteredGarment = Garment()
        notFilteredGarment.brand = "Acorn2"
        repeat(4) {
            appContainer.garmentsRespository.insertGarment(notFilteredGarment)
        }

        val filteredGarment = Garment()
        filteredGarment.brand = filteredString
        repeat(5) {
            appContainer.garmentsRespository.insertGarment(filteredGarment)
        }

        val itemsSnapshot = pager.asSnapshot {
            scrollTo(index = 50)
        }

        val matches = itemsSnapshot.filter {
            it.brand == "acorn2" || it.color == secondColor || it.categoryId == fourthCategoryId
        }
        Assert.assertTrue(
            matches.size == 7
        )
    }


    @Test
    fun deleteGarment() = runTest {
        val id = appContainer.garmentsRespository.insertGarment(Garment())
        var garment = appContainer.garmentsRespository.getGarmentStream(id).firstOrNull()
        appContainer.garmentsRespository.deleteGarment(garment!!)
        garment = appContainer.garmentsRespository.getGarmentStream(id).firstOrNull()
        Assert.assertTrue(garment == null)
    }


    @Test
    fun updateGarment() = runTest {
        val id = appContainer.garmentsRespository.insertGarment(Garment())
        val garment = appContainer.garmentsRespository.getGarmentStream(id).firstOrNull()

        garment!!.image = "image"
        garment.brand = "brand"
        garment.name = "name"
        garment.categoryId = 1
        garment.subCategoryId = 1000
        garment.outfitsId = listOf(1, 2, 3)
        garment.occasion = Occasion.FORMAL
        garment.color = "blue"
        garment.imageOfSubject = "imageOfSubject"
        appContainer.garmentsRespository.updateGarment(garment)
        val updatedGarment = appContainer.garmentsRespository.getGarmentStream(id).firstOrNull()
        appContainer.garmentsRespository.deleteGarment(updatedGarment!!)
        Assert.assertTrue(garment == updatedGarment)
    }

    @Test
    fun saveImageToStorage() = runTest {
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        val width = 1000
        val height = 1000
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        val appDir = File(context.filesDir.toURI())
        val newFileUri = appContainer.garmentsRespository.saveImageToStorage(appDir, bitmap)

        Assert.assertTrue(
            newFileUri.path.toString().contains("${context.filesDir.toPath()}/GarmentImages")
        )

        Assert.assertTrue(
            File(newFileUri.path!!).exists()
        )

        Assert.assertTrue(
            File(newFileUri.path!!).length() > 0
        )

    }


    @Test
    fun replaceImageInStorage() = runTest {
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        val width = 1000
        val height = 1000
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val replaceBitMap = Bitmap.createBitmap(width/2, height/2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        val file = File(context.filesDir, "replaceImageInStorage.png")
        if(file.exists()){
            file.delete()
        }
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 20, outputStream)
        }
        val originalSize = file.length()
        val modDate = file.lastModified()
        appContainer.garmentsRespository.replaceImageInStorage(file, replaceBitMap)

        Assert.assertTrue(
            file.lastModified() > modDate
        )

        Assert.assertTrue(
            file.length() < originalSize
        )
    }


    @Test
    fun getBrands() = runTest {
        val ids = mutableListOf<Long>()
        val brands = mutableListOf<String>()
        repeat(3) {
            val brand =  "getBrands${it}"
            brands.add(brand)
            val g = Garment()
            g.brand = brand
            ids.add(appContainer.garmentsRespository.insertGarment(g))
        }

        val dbBrands = appContainer.garmentsRespository.getBrands().firstOrNull()
        val notFound = mutableListOf<String>()
        brands.forEach{
            if(!dbBrands!!.contains(it.lowercase())){
                notFound.add(it)
            }
        }
        Assert.assertTrue(notFound.isEmpty())
    }

    @Test
    fun getThumbnail_hasImage() = runTest {
        val file = fakeImage(context, "replaceImageInStorage.png")
        val g = Garment(image = file.toURI().toString())
        val thumbnail = appContainer.garmentsRespository.getGarmentThumbnail(g)

        Assert.assertTrue(
            thumbnail!!.contains("thumbnail")
        )
        Assert.assertTrue(
            File(thumbnail).exists()
        )
    }

    @Test
    fun getThumbnail_noImage() = runTest {
        val g = Garment()
        val thumbnail = appContainer.garmentsRespository.getGarmentThumbnail(g)
        Assert.assertTrue(
            thumbnail == null
        )
    }


    @Test
    fun getGarmentsCount() = runTest {
        val count = appContainer.garmentsRespository.getGarmentsCount(listOf(), listOf(), listOf()).firstOrNull()
        Assert.assertTrue(
            count!! > 0
        )
    }
    @Test
    fun getColorCount() = runTest {
        val colorBeforeCount = appContainer.garmentsRespository.getColorCount().first()
        val injectColors: Map<String, Int> = mutableMapOf(
            GarmentColorNames[1].name to 12,
            GarmentColorNames[5].name to 6,
            GarmentColorNames[9].name to 11,
            GarmentColorNames[7].name to 2,
        )
        injectColors.forEach { (colorName, count) ->
            repeat(count) {
                appContainer.garmentsRespository.insertGarment(Garment(color = colorName))
            }
        }

        val noColor = 3
        repeat(noColor) {
            appContainer.garmentsRespository.insertGarment(Garment())
        }

        val colorCount = appContainer.garmentsRespository.getColorCount().firstOrNull()
        val nullCount = colorCount?.find { it.color == null}?.count
        val colorCountMap = mutableMapOf<String, Int>()
        colorCount?.map {
            if(it.color != null){
                colorCountMap[it.color!!] = it.count
            }
        }
        Assert.assertNotNull(colorCount)
        Assert.assertNotNull(nullCount)
        Assert.assertTrue(nullCount!! >= noColor)


        injectColors.forEach { (color, injectCount) ->
            val beforeCount = colorBeforeCount.find { it.color == color }?.count
            val expected = (beforeCount ?: 0) + injectCount
            val afterCount = colorCount.find {  it.color == color }?.count ?: 0
            Assert.assertEquals(expected, afterCount)
        }
    }

    @Test
    fun getCategoryCount() = runTest {
        val categoryBeforeCount = appContainer.garmentsRespository.getCategoryCount().first()
        val inject: Map<Int, Int> = mutableMapOf(
            Category.categories()[1].id to 12,
            Category.categories()[2].id to 6,
            Category.categories()[6].id to 11,
            Category.categories()[8].id to 2,
        )
        inject.forEach { (id, count) ->
            repeat(count) {
                appContainer.garmentsRespository.insertGarment(Garment(categoryId = id))
            }
        }

        val categoryCount = appContainer.garmentsRespository.getCategoryCount().firstOrNull()
        val nullCountBefore = categoryCount?.find { it.categoryId == null }?.count
        val categoryCountMap = mutableMapOf<Int, Int>()
        categoryCount?.map {
            if(it.categoryId != null){
                categoryCountMap[it.categoryId!!] = it.count
            }
        }

        val none = 3
        repeat(none) {
            appContainer.garmentsRespository.insertGarment(Garment())
        }
        val categoryCountAfter = appContainer.garmentsRespository.getCategoryCount().firstOrNull()
        val nullCountAfter = categoryCountAfter?.find { it.categoryId == null }?.count

        Assert.assertNotNull(categoryCount)
        Assert.assertNotNull(nullCountAfter)
        Assert.assertTrue((nullCountAfter!! - nullCountBefore!!) == none)

        inject.forEach { (id, injectCount) ->
            val beforeCount = categoryBeforeCount.find { it.categoryId == id }?.count
            val expected = (beforeCount ?: 0) + injectCount
            val afterCount = categoryCountAfter.find {  it.categoryId == id }?.count ?: 0
            Assert.assertEquals(expected, afterCount)
        }
    }

    @Test
    fun getOccasionCount() = runTest {
        val occasionBeforeCount = appContainer.garmentsRespository.getOccasionCount().first()
        val inject: Map<Occasion, Int> = mutableMapOf(
            Occasion.entries[1] to 12,
            Occasion.entries[2] to 6,
            Occasion.entries[3] to 11,
            Occasion.entries[4] to 2,
        )
        inject.forEach { (occasion, count) ->
            repeat(count) {
                appContainer.garmentsRespository.insertGarment(Garment(occasion = occasion))
            }
        }

        val occasionCount = appContainer.garmentsRespository.getOccasionCount().firstOrNull()
        val nullCountBefore = occasionCount?.find { it.occasion == null }?.count
        val occasionCountMap = mutableMapOf<Occasion, Int>()
        occasionCount?.map {
            if(it.occasion != null){
                occasionCountMap[it.occasion!!] = it.count
            }
        }

        val none = 3
        repeat(none) {
            appContainer.garmentsRespository.insertGarment(Garment())
        }
        val occasionCountAfter = appContainer.garmentsRespository.getOccasionCount().firstOrNull()
        val nullCountAfter = occasionCountAfter?.find { it.occasion == null }?.count

        Assert.assertNotNull(occasionCount)
        Assert.assertNotNull(nullCountAfter)
        Assert.assertTrue((nullCountAfter!! - nullCountBefore!!) == none)

        inject.forEach { (occasion, injectCount) ->
            val beforeCount = occasionBeforeCount.find { it.occasion == occasion }?.count
            val expected = (beforeCount ?: 0) + injectCount
            val afterCount = occasionCountAfter.find {  it.occasion == occasion }?.count ?: 0
            Assert.assertEquals(expected, afterCount)
        }
    }

    @After
    fun closeDatabase() {
        database.close()
    }
}