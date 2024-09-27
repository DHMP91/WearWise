package dhmp.wearwise.ui.screens.clothing

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.platform.app.InstrumentationRegistry
import dhmp.wearwise.App
import dhmp.wearwise.R
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.ui.UITest
import dhmp.wearwise.ui.screens.FakePagingSource
import dhmp.wearwise.ui.screens.common.TestTag
import dhmp.wearwise.ui.screens.common.WearWiseBottomAppBar
import dhmp.wearwise.ui.theme.WearWiseTheme
import dhmp.wearwise.ui.verifyScreenTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import kotlin.time.Duration.Companion.minutes


class ClothingScreenUITest: UITest() {
    private lateinit var mockedGarmentRepo: GarmentsRepository
    private lateinit var context: Context
    private lateinit var model: ClothingViewModel
    private val clothingTitle = "Clothing"
    private val brands = listOf("Brand1", "Brand2", "Brand3")

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        model = ClothingViewModel(mockedGarmentRepo)
    }


    @Test
    fun landingScreen() {
        composeTestRule.setContent {
            WearWiseTheme {
                App()
            }
        }
        verifyScreenTitle(clothingTitle)
    }


    @Test
    fun clothingFilterByCategory_toggleCategoryAll() = runTest(timeout = 5.minutes) {
        baseMock()
        Mockito.`when`(mockedGarmentRepo.getFilteredGarments(
            excludedCategories = Category.categories(),
            excludedColors = listOf(),
            excludedBrands = listOf()
        )
        ).thenAnswer {
            FakePagingSource(listOf<Garment>(
                Garment(
                    id = 87L,
                    categoryId = 99,
                ),
                Garment(
                    id = 89L,
                    categoryId = 99,
                ),
                Garment(
                    id = 99L,
                    categoryId = 99,
                )
            ))
        }

        launchClothingScreen()

        //Verify: Uncheck all category
        val title = context.getString(R.string.filter_by_category)
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.onNode(hasText(context.getString(R.string.filter_by_category))).performClick()
        composeTestRule.onNode(hasText("All").and(hasTestTag("${TestTag.FILTER_PREFIX}${title}"))).performClick()
        composeTestRule.waitForIdle()
        Assert.assertTrue(itemCount() == 3)

        composeTestRule.onNode(hasText("All").and(hasTestTag("${TestTag.FILTER_PREFIX}${title}"))).performClick()
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        Assert.assertTrue(itemCount() != 3 && itemCount() > 0)
        composeTestRule.waitForIdle()

    }


    @Test
    fun clothingFilterByCategory_OneCategoryChecked() = runTest(timeout = 5.minutes) {
        baseMock()
        val categories = Category.categories()
        val checkCategoryIndex = categories.size/2
        val checkCategory = categories[checkCategoryIndex]

        Mockito.`when`(mockedGarmentRepo.getFilteredGarments(
            excludedCategories = categories,
            excludedColors = listOf(),
            excludedBrands = listOf()
        )
        ).thenAnswer {
            FakePagingSource(listOf<Garment>(
                Garment(
                    id = 87L,
                    categoryId = 99,
                )
            ))
        }

        Mockito.`when`(mockedGarmentRepo.getFilteredGarments(
            excludedCategories = categories.minus(checkCategory),
            excludedColors = listOf(),
            excludedBrands = listOf()
        )
        ).thenAnswer {
            FakePagingSource(listOf<Garment>(
                Garment(
                    id = 87L,
                    categoryId = 99,
                ),
                Garment(
                    id = 89L,
                    categoryId = 99,
                )
            ))
        }
        launchClothingScreen()

        //Verify: Check one category
        val title = context.getString(R.string.filter_by_category)
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.onNode(hasText(context.getString(R.string.filter_by_category))).performClick()
        composeTestRule.onNode(hasText("All").and(hasTestTag("${TestTag.FILTER_PREFIX}${title}"))).performClick()
        composeTestRule.onNode(hasTestTag("${TestTag.FILTER_ROW_PREFIX}${title}")).performScrollToIndex(checkCategoryIndex)
        composeTestRule.onNode(hasText(checkCategory.name)).performClick()
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.waitForIdle()
        Assert.assertTrue(itemCount() == 2)
    }

    @Test
    fun clothingFilterByCategory_OneCategoryUnchecked() = runTest(timeout = 5.minutes) {
        baseMock()
        val categories = Category.categories()
        val unCheckCategoryIndex = categories.size/2
        val unCheckCategory = categories[unCheckCategoryIndex]
        val listOfGarments = mutableListOf<Garment>()
        repeat(5) {
            val garment = Garment(
                id = it.toLong(),
                categoryId = it + 1,
            )
            listOfGarments.add(garment)
        }

        Mockito.`when`(mockedGarmentRepo.getFilteredGarments(
            excludedCategories = listOf(unCheckCategory),
            excludedColors = listOf(),
            excludedBrands = listOf()
        )
        ).thenAnswer { FakePagingSource(listOfGarments) }

        launchClothingScreen()

        //Verify: Check one category
        val title = context.getString(R.string.filter_by_category)
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.onNode(hasText(context.getString(R.string.filter_by_category))).performClick()
        composeTestRule.onNode(hasTestTag("${TestTag.FILTER_ROW_PREFIX}${title}")).performScrollToIndex(unCheckCategoryIndex)
        composeTestRule.onNode(hasText(unCheckCategory.name)).performClick()
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.waitForIdle()
        Assert.assertTrue(itemCount() == 5)
    }


    @Test
    fun clothingFilterByCategory_toggleAllColor() = runTest(timeout = 5.minutes) {
        baseMock()
        val listOfGarments = mutableListOf<Garment>()
        repeat(4) {
            val garment = Garment(
                id = it.toLong(),
                categoryId = it + 1,
            )
            listOfGarments.add(garment)
        }

        Mockito.`when`(mockedGarmentRepo.getFilteredGarments(
            excludedCategories = listOf(),
            excludedColors = GarmentColorNames.map { it.name },
            excludedBrands = listOf()
        )
        ).thenAnswer {
            FakePagingSource(listOfGarments)
        }

        launchClothingScreen()

        //Verify: Check all color
        val title = context.getString(R.string.filter_by_color)
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.onNode(hasText(context.getString(R.string.filter_by_color))).performClick()
        composeTestRule.onNode(hasText("All").and(hasTestTag("${TestTag.FILTER_PREFIX}${title}"))).performClick()
        composeTestRule.onNode(hasText("All").and(hasTestTag("${TestTag.FILTER_PREFIX}${title}"))).performClick()
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.waitForIdle()
        Assert.assertTrue(itemCount() != 4)
    }

    @Test
    fun clothingFilterByCategory_UncheckOneColor() = runTest(timeout = 5.minutes) {
        baseMock()
        Mockito.`when`(mockedGarmentRepo.getFilteredGarments(
            excludedCategories = listOf(),
            excludedColors = listOf("Blue"),
            excludedBrands = listOf()
        )
        ).thenAnswer {
            FakePagingSource(
                listOf(
                    Garment(
                        id = 87L,
                        categoryId = 99,
                    )
                )
            )
        }
        launchClothingScreen()

        //Verify: Uncheck one color
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.onNode(hasText(context.getString(R.string.filter_by_color))).performClick()
        composeTestRule.onNode(hasText("Blue")).performClick()
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.waitForIdle()
        Assert.assertTrue(itemCount() == 1)
    }


    @Test
    fun clothingFilterByCategory_toggleBrandAll() = runTest(timeout = 5.minutes) {
        baseMock()
        Mockito.`when`(mockedGarmentRepo.getFilteredGarments(
            excludedCategories = listOf(),
            excludedColors = listOf(),
            excludedBrands = brands
        )
        ).thenAnswer {
            FakePagingSource(listOf<Garment>(
                Garment(
                    id = 87L,
                    categoryId = 99,
                ),
                Garment(
                    id = 89L,
                    categoryId = 99,
                ),
                Garment(
                    id = 99L,
                    categoryId = 99,
                )
            ))
        }

        launchClothingScreen()

        //Verify: Uncheck all category
        val title = context.getString(R.string.filter_by_brand)
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.onNode(hasText(context.getString(R.string.filter_by_brand))).performClick()
        composeTestRule.onNode(hasText("All").and(hasTestTag("${TestTag.FILTER_PREFIX}${title}"))).performClick()
        composeTestRule.waitForIdle()
        Assert.assertTrue(itemCount() == 3)

        composeTestRule.onNode(hasText("All").and(hasTestTag("${TestTag.FILTER_PREFIX}${title}"))).performClick()
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        Assert.assertTrue(itemCount() != 3 && itemCount() > 0)
        composeTestRule.waitForIdle()

    }


    @Test
    fun clothingFilterByCategory_OneBrandChecked() = runTest(timeout = 5.minutes) {
        baseMock()
        val checkedBrand = brands.last()
        Mockito.`when`(mockedGarmentRepo.getFilteredGarments(
            excludedCategories = listOf(),
            excludedColors = listOf(),
            excludedBrands = brands
        )
        ).thenAnswer {
            FakePagingSource(listOf<Garment>(
                Garment(
                    id = 87L,
                    categoryId = 99,
                )
            ))
        }


        Mockito.`when`(mockedGarmentRepo.getFilteredGarments(
            excludedCategories = listOf(),
            excludedColors = listOf(),
            excludedBrands = brands.minus(checkedBrand)
        )
        ).thenAnswer {
            FakePagingSource(listOf<Garment>(
                Garment(
                    id = 87L,
                    categoryId = 99,
                ),
                Garment(
                    id = 89L,
                    categoryId = 99,
                )
            ))
        }
        launchClothingScreen()

        //Verify: Check one category
        val title = context.getString(R.string.filter_by_brand)
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.onNode(hasText(context.getString(R.string.filter_by_brand))).performClick()
        composeTestRule.onNode(hasText("All").and(hasTestTag("${TestTag.FILTER_PREFIX}${title}"))).performClick()
        composeTestRule.onNode(hasText(checkedBrand)).performClick()
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.waitForIdle()
        Assert.assertTrue(itemCount() == 2)
    }

    @Test
    fun clothingFilterByCategory_OneBrandUnchecked() = runTest(timeout = 5.minutes) {
        baseMock()
        val uncheckBrand = brands.first()
        val listOfGarments = mutableListOf<Garment>()
        repeat(5) {
            val garment = Garment(
                id = it.toLong(),
                categoryId = it + 1,
            )
            listOfGarments.add(garment)
        }

        Mockito.`when`(mockedGarmentRepo.getFilteredGarments(
            excludedCategories = listOf(),
            excludedColors = listOf(),
            excludedBrands = listOf(uncheckBrand)
        )
        ).thenAnswer { FakePagingSource(listOfGarments) }

        launchClothingScreen()

        //Verify: Check one category
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.onNode(hasText(context.getString(R.string.filter_by_brand))).performClick()
        composeTestRule.onNode(hasText(uncheckBrand)).performClick()
        composeTestRule.onNode(hasTestTag(TestTag.MAIN_MENU)).performClick()
        composeTestRule.waitForIdle()
        Assert.assertTrue(itemCount() == 5)
    }


    @Test
    fun clothingList() = runTest(timeout = 5.minutes) {
        val fakeGarments = mutableListOf<Garment>()
        val garmentCreationCount = 9
        val categoryIcons = listOf(
            R.drawable.accessory,
            R.drawable.intimates,
            R.drawable.outer_wear,
            R.drawable.shoe_icon,
            R.drawable.dress_icon,
            R.drawable.hats_icon,
            R.drawable.shirt_icon,
            R.drawable.pants_icon
        )
        val outfitIds = listOf<Long>(1, 2, 3)
        val brand = "SomeFancyBrand"
        repeat(garmentCreationCount){
            val garment = Garment(
                id = it.toLong(),
                categoryId = it + 1,
                outfitsId = outfitIds,
            )
            garment.brand = brand
            fakeGarments.add(garment)
        }

        baseMock(garments = fakeGarments)

        launchClothingScreen()

        // Verify: title clothing count
        composeTestRule.onNode(hasText("999 Results")).assertIsDisplayed()

        // Verify lazy loading list initial amount
        var clothingCards =  composeTestRule.onAllNodes(hasTestTag(TestTag.CLOTHING_ITEM)).fetchSemanticsNodes()
        var displayedClothingItemCount = clothingCards.size
        Assert.assertTrue(displayedClothingItemCount < garmentCreationCount && displayedClothingItemCount >= garmentCreationCount/2)

        // Verify: ability to scroll to last item
        val garmentDisplayed = {
            composeTestRule.onNode(hasText("#0")).isDisplayed()
        }
        Assert.assertFalse(garmentDisplayed())
        var maxScrollAttempt = 3
        while(!garmentDisplayed() && maxScrollAttempt > 0) {
            maxScrollAttempt -= 1
            clothingCards = composeTestRule.onAllNodes(hasTestTag(TestTag.CLOTHING_ITEM)).fetchSemanticsNodes()
            displayedClothingItemCount = clothingCards.size
            composeTestRule.onNode(hasTestTag(TestTag.CLOTHING_LIST)).performScrollToIndex(displayedClothingItemCount - 1)
        }
        Assert.assertTrue(garmentDisplayed())

        // Verify: category icons are displayed
        composeTestRule.onNode(hasTestTag(TestTag.CLOTHING_LIST)).performScrollToIndex(0)
        val foundIcons = mutableSetOf<Int>()
        maxScrollAttempt = 3
        while(foundIcons.size != categoryIcons.size && maxScrollAttempt > 0){
            maxScrollAttempt -= 1
            for(icon in categoryIcons){
                if(composeTestRule.onNode(hasTestTag("${TestTag.CLOTHING_LIST_CATEGORY_PREFIX}${icon}"), useUnmergedTree = true).isDisplayed()) {
                    foundIcons.add(icon)
                }
            }
            clothingCards = composeTestRule.onAllNodes(hasTestTag(TestTag.CLOTHING_ITEM)).fetchSemanticsNodes()
            displayedClothingItemCount = clothingCards.size
            composeTestRule.onNode(hasTestTag(TestTag.CLOTHING_LIST)).performScrollToIndex(displayedClothingItemCount - 1)
        }

        Assert.assertTrue(foundIcons.size == categoryIcons.size)


        // Verify: outfit count
        val firstOutfitCountNode = composeTestRule.onAllNodes(hasTestTag(TestTag.OUTFIT_COUNT), useUnmergedTree = true).fetchSemanticsNodes()[0]
        var text: String? = null
        for(node in firstOutfitCountNode.children){
            if(node.config.contains(SemanticsProperties.Text)){
                text = node.config.getOrNull(SemanticsProperties.Text)?.joinToString()
            }
        }
        Assert.assertNotNull(text)
        Assert.assertTrue(text!!.toInt() == outfitIds.size)

        // Verify: outfit count
        val brandFields = composeTestRule.onAllNodes(hasTestTag(TestTag.CLOTHING_BRAND_CARD_FIELD), useUnmergedTree = true).fetchSemanticsNodes()
        Assert.assertTrue(brandFields.size > (garmentCreationCount / 2))
        Assert.assertTrue(getText(brandFields[0])!!.lowercase() == brand.lowercase())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun newClothing() = runTest(timeout = 5.minutes){
        composeTestRule.setContent {
            WearWiseTheme {
                App()
            }
        }

        val resultCount = getText(composeTestRule.onNode(hasTestTag(TestTag.RESULT_COUNT)))
        val number = resultCount?.let {  extractNumber(it) }
        Assert.assertNotNull(number)
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTag.NEW_CLOTHING_BUTTON))
        composeTestRule.onNode(hasTestTag(TestTag.NEW_CLOTHING_BUTTON)).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTag.USE_CAMERA_SELECTION))
        composeTestRule.onNode(hasTestTag(TestTag.USE_CAMERA_SELECTION)).performClick()
        composeTestRule.waitUntilDoesNotExist(hasTestTag(TestTag.USE_CAMERA_SELECTION))
        composeTestRule.waitForIdle()


        while(
            composeTestRule.onAllNodes(hasTestTag(TestTag.CAMERA_TAKE_ICON), useUnmergedTree = true)
                .fetchSemanticsNodes().isEmpty()
        ){
            runBlocking {
                delay(1000)
            }
        }
        composeTestRule.onNode(hasTestTag(TestTag.CAMERA_TAKE_ICON), useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTag.SCREEN_TITLE), 10000)
        composeTestRule.waitForIdle()
        verifyScreenTitle(Regex("Clothing #\\d+"))

        composeTestRule.onNode(hasTestTag(TestTag.BOTTOMBAR_CLOTHING)).performClick()
        composeTestRule.waitForIdle()
        verifyScreenTitle(clothingTitle)

        val resultCountAfter = getText(composeTestRule.onNode(hasTestTag(TestTag.RESULT_COUNT)))
        val numberAfter = resultCountAfter?.let {  extractNumber(it) }
        Assert.assertNotNull(numberAfter)
        Assert.assertTrue(numberAfter!!.toInt() == number!!.toInt() + 1)


        composeTestRule.onAllNodes(hasTestTag(TestTag.CLOTHING_ITEM)).onFirst().performClick()
        val editClothingTitle = getText(composeTestRule.onNode(hasTestTag(TestTag.SCREEN_TITLE)))
        Assert.assertNotNull(editClothingTitle)
        Assert.assertTrue(editClothingTitle!!.contains(clothingTitle))
    }


    private fun extractNumber(input: String): Int? {
        val regex = "(\\d+) Results".toRegex()
        val matchResult = regex.find(input)
        return matchResult?.groupValues?.get(1)?.toInt()
    }


    private fun itemCount(): Int {
        val clothingCards = composeTestRule.onAllNodes(hasTestTag(TestTag.CLOTHING_ITEM)).fetchSemanticsNodes()
        val displayedClothingItemCount = clothingCards.size
        return displayedClothingItemCount
    }

    private fun launchClothingScreen(){
        composeTestRule.setContent {
            WearWiseTheme {
                Scaffold(
                    bottomBar = {
                        WearWiseBottomAppBar(
                            navOutfit = {},
                            navClothing = {},
                            navNewClothing = {},
                            navShop = {},
                            navUser = {},
                            null
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ClothingScreen(
                            onEdit = {},
                            onNewClothing = {},
                            onOutfits = {},
                            clothingViewModel = model
                        )
                    }
                }
            }
        }
    }

    private suspend fun baseMock(garments: List<Garment>? = null){
        var fakeGarments = garments
        if(fakeGarments == null){
            fakeGarments = mutableListOf()
            repeat(9) {
                val garment = Garment(
                    id = it.toLong(),
                    categoryId = it + 1,
                )
                fakeGarments.add(garment)
            }
        }


        Mockito.`when`(
            mockedGarmentRepo.getFilteredGarments(
                listOf(),
                listOf(),
                listOf()
            )
        ).thenAnswer {
            FakePagingSource(fakeGarments.reversed())
        }

        Mockito.`when`(mockedGarmentRepo.getGarmentsCount(any(), any(), any())).thenAnswer {
            flow { emit(999) }
        }

        Mockito.`when`(mockedGarmentRepo.getGarmentThumbnail(any())).thenAnswer {
            ""
        }


        Mockito.`when`(mockedGarmentRepo.getBrands()).thenAnswer {
            flow {
                emit(brands)
            }
        }
    }

}