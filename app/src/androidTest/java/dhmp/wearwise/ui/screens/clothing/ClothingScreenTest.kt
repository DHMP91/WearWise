package dhmp.wearwise.ui.screens.clothing

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.platform.app.InstrumentationRegistry
import dhmp.wearwise.App
import dhmp.wearwise.R
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.model.Garment
import dhmp.wearwise.ui.UITest
import dhmp.wearwise.ui.screens.FakePagingSource
import dhmp.wearwise.ui.screens.common.TestTag
import dhmp.wearwise.ui.screens.common.WearWiseBottomAppBar
import dhmp.wearwise.ui.theme.WearWiseTheme
import dhmp.wearwise.ui.verifyScreenTitle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import kotlin.time.Duration.Companion.minutes


class ClothingScreenUITest: UITest() {
    private val clothingRegexTitle = Regex("Clothing \\(\\d+\\)")
    private lateinit var mockedGarmentRepo: GarmentsRepository
    private lateinit var context: Context
    private lateinit var model: ClothingViewModel
    private lateinit var testDispatcher: CoroutineDispatcher


    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        testDispatcher = StandardTestDispatcher()
        model = ClothingViewModel(mockedGarmentRepo)
    }


    @Test
    fun landingScreen() {
        composeTestRule.setContent {
            WearWiseTheme {
                App()
            }
        }
        verifyScreenTitle(clothingRegexTitle)
    }


    @Test
    fun clothingList() = runTest(timeout = 5.minutes) {
        val fakeGarments = mutableListOf<Garment>()
        val fakeClothingCounter = 7
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
                categoryId = it+1,
                outfitsId = outfitIds,
            )
            garment.brand = brand
            fakeGarments.add(garment)
        }

        Mockito.`when`(mockedGarmentRepo.getFilteredGarments(any(), any(), any())).thenAnswer{
            FakePagingSource(fakeGarments.reversed())
        }

        Mockito.`when`(mockedGarmentRepo.getGarmentsCount(any(), any(), any())).thenAnswer{
            flow { emit(fakeClothingCounter)}
        }

        Mockito.`when`(mockedGarmentRepo.getGarmentThumbnail(any())).thenAnswer{
             ""
        }


        val brands = listOf("Brand1", "Brand2", "Brand3")
        Mockito.`when`(mockedGarmentRepo.getBrands()).thenAnswer {
            flow {
                emit(brands)
            }
        }

        composeTestRule.setContent {
            WearWiseTheme {
                Scaffold(
                  bottomBar = {
                      WearWiseBottomAppBar(
                          navOutfit = {},
                          navClothing = {},
                          navNewClothing = {},
                          navShop = {},
                          null
                      )
                  }
                ) { innerPadding ->
                    Box(modifier =  Modifier.padding(innerPadding)) {
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

        // Verify: title clothing count
        val clothingTitle = getText(composeTestRule.onNode(hasTestTag(TestTag.SCREEN_TITLE)))
        val number = clothingTitle?.let {  extractNumber(it) }
        Assert.assertNotNull(number)
        Assert.assertTrue(number!!.toInt() == fakeClothingCounter)

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

        val clothingTitle = getText(composeTestRule.onNode(hasTestTag(TestTag.SCREEN_TITLE)))
        val number = clothingTitle?.let {  extractNumber(it) }
        Assert.assertNotNull(number)
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTag.NEW_CLOTHING_BUTTON))
        composeTestRule.onNode(hasTestTag(TestTag.NEW_CLOTHING_BUTTON)).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTag.USE_CAMERA_SELECTION))
        composeTestRule.onNode(hasTestTag(TestTag.USE_CAMERA_SELECTION)).performClick()
        composeTestRule.waitUntilDoesNotExist(hasTestTag(TestTag.USE_CAMERA_SELECTION))
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasTestTag(TestTag.CAMERA_TAKE_ICON), useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTag.SCREEN_TITLE), 10000)
        composeTestRule.waitForIdle()
        verifyScreenTitle( Regex("Clothing Item #\\d+"))

        composeTestRule.onNode(hasTestTag(TestTag.BOTTOMBAR_CLOTHING)).performClick()
        composeTestRule.waitForIdle()
        verifyScreenTitle(clothingRegexTitle)

        val clothingTitleAfter = getText(composeTestRule.onNode(hasTestTag(TestTag.SCREEN_TITLE)))
        val numberAfter = clothingTitleAfter?.let {  extractNumber(it) }
        Assert.assertNotNull(numberAfter)
        Assert.assertTrue(numberAfter!!.toInt() == number!!.toInt() + 1)
    }


    private fun extractNumber(input: String): Int? {
        val regex = "\\((\\d+)\\)".toRegex()
        val matchResult = regex.find(input)
        return matchResult?.groupValues?.get(1)?.toInt()
    }

}