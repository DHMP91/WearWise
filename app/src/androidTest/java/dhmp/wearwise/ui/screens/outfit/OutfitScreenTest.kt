package dhmp.wearwise.ui.screens.outfit

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.performClick
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import dhmp.wearwise.App
import dhmp.wearwise.R
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.OutfitsRepository
import dhmp.wearwise.data.UserConfigRepository
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.Outfit
import dhmp.wearwise.ui.UITest
import dhmp.wearwise.ui.screens.FakePagingSource
import dhmp.wearwise.ui.screens.clothing.ClothingViewModel
import dhmp.wearwise.ui.screens.common.TestTag
import dhmp.wearwise.ui.screens.common.WearWiseBottomAppBar
import dhmp.wearwise.ui.screens.fakeImage
import dhmp.wearwise.ui.theme.WearWiseTheme
import dhmp.wearwise.ui.verifyScreenTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.verify

class OutfitScreenTest: UITest() {
    private lateinit var mockedGarmentRepo: GarmentsRepository
    private lateinit var mockedOutfitRepo: OutfitsRepository
    private lateinit var mockedUserConfigRepo: UserConfigRepository
    private lateinit var context: Context
    private lateinit var model: OutfitViewModel
    private lateinit var clothingViewModel: ClothingViewModel

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        mockedOutfitRepo = Mockito.mock(OutfitsRepository::class.java)
        mockedUserConfigRepo = Mockito.mock(UserConfigRepository::class.java)
        model = OutfitViewModel(mockedGarmentRepo, mockedOutfitRepo)
        clothingViewModel =  ClothingViewModel(mockedGarmentRepo, mockedUserConfigRepo)
    }

    @Test
    fun outfitList() {
        val file = fakeImage(context, "test_outfitbuilder.png")
        val icons = listOf(
            R.drawable.hats_icon, // 0
            R.drawable.shirt_icon, // 1
            R.drawable.pants_icon, // 2
            R.drawable.dress_icon, // 3
        )

        //Fake garments
        val imageString = file.toUri().toString()
        val garmentOne = Garment(
            id = 1,
            image = imageString,
            categoryId = Category.categories()[0].id
        )

        val garmentTwo = Garment(
            id = 2,
            image = imageString,
            categoryId = Category.categories()[1].id
        )

        val garmentThree = Garment(
            id = 3,
            image = imageString,
            categoryId = Category.categories()[2].id
        )

        val garmentFour = Garment(
            id = 4,
            image = imageString,
            categoryId = Category.categories()[3].id
        )

        val garments = listOf(
            garmentOne,
            garmentTwo,
            garmentThree,
            garmentFour
        )

        //Fake Outfits
        val outfitNoImage = Outfit(
            id = 1,
            garmentsId = listOf(1, 2)
        )
        val outFitImageAndGarments = Outfit(
            id = 2,
            garmentsId = listOf(3, 4),
            image = imageString
        )

        val outFitImageOnly = Outfit(
            id = 3,
            garmentsId = listOf(1, 4),
            image = imageString
        )

        val emptyOutfit = Outfit(
            id = 4
        )

        val outfits: List<Outfit> = listOf(
            emptyOutfit,
            outfitNoImage,
            outFitImageAndGarments,
            outFitImageOnly,
        )



        runBlocking {
            Mockito.`when`(mockedOutfitRepo.getAllOutfitsPaged()).thenAnswer{
                FakePagingSource(outfits)
            }

            Mockito.`when`(mockedOutfitRepo.getOutfitStream(emptyOutfit.id)).thenAnswer{
                flow {
                    emit(emptyOutfit)
                }
            }
            Mockito.`when`(mockedOutfitRepo.deleteOutfit(emptyOutfit)).thenAnswer{}

            Mockito.`when`(mockedOutfitRepo.getOutfitThumbnail(any())).thenAnswer {
                ""
            }
            Mockito.`when`(mockedGarmentRepo.getGarmentThumbnail(any())).thenAnswer {
                imageString
            }

            for(g in garments){
                Mockito.`when`(mockedGarmentRepo.getGarmentStream(g.id)).thenAnswer {
                    flow {
                        emit(g)
                    }
                }
            }


            composeTestRule.setContent {
                WearWiseTheme {
                    Scaffold(
                        bottomBar = {
                            WearWiseBottomAppBar(
                                navOutfit = {},
                                navClothing = {},
                                navUser = {},
                                route = null
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            OutfitScreen(
                                onEdit = { _ -> },
                                onTakePicture = { _ -> },
                                onNewOutfit = {},
                                model = model,
                                clothingViewModel = clothingViewModel
                            )
                        }
                    }
                }
            }

            verifyScreenTitle("Outfits")
            composeTestRule.waitForIdle()
            verify(mockedOutfitRepo, atLeastOnce()).deleteOutfit(
                emptyOutfit
            )

            // Validate outfit displayed count
            val outfitsCount = composeTestRule.onAllNodes(hasTestTag(TestTag.OUTFIT_CARD)).fetchSemanticsNodes().size
            val outfitsThumbnailCount = composeTestRule.onAllNodes(hasTestTag(TestTag.OUTFIT_THUMBNAIL), useUnmergedTree = true).fetchSemanticsNodes().size
            Assert.assertEquals(outfits.size - 1, outfitsCount)
            Assert.assertEquals(outfits.size - 2, outfitsThumbnailCount)

            // Validate proper garment category icon is displayed
            for(icon in icons){
                val foundCount = composeTestRule.onAllNodes(
                    hasTestTag("${TestTag.CLOTHING_LIST_CATEGORY_PREFIX}${icon}"),
                    useUnmergedTree = true
                ).fetchSemanticsNodes().size
                Assert.assertTrue(foundCount > 0)
            }

            //Validate thumbnails are present
            val garmentThumbnailCount = composeTestRule.onAllNodes(hasTestTag(TestTag.OUTFIT_GARMENT_THUMBNAIL), useUnmergedTree = true).fetchSemanticsNodes().size
            var garmentUsedCount = 0
            outfits.map {
                garmentUsedCount += it.garmentsId.size
            }
            Assert.assertEquals(garmentUsedCount, garmentThumbnailCount)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun newOutfit_noGarments(){
        composeTestRule.setContent {
            WearWiseTheme {
                App()
            }
        }

        composeTestRule.onNode(hasTestTag(TestTag.BOTTOMBAR_OUTFIT)).performClick()
        composeTestRule.waitUntil {
            composeTestRule
                .onNode(hasText("Outfits")).isDisplayed()
        }
        composeTestRule.waitForIdle()

        runBlocking {
            delay(2000)
        }
        val initialOutfitCount = composeTestRule.onAllNodes(hasTestTag(TestTag.OUTFIT_CARD)).fetchSemanticsNodes().size
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTag.NEW_OUTFIT_BUTTON))
        composeTestRule.onNode(hasTestTag(TestTag.NEW_OUTFIT_BUTTON)).performClick()
        composeTestRule.waitForIdle()

        //Validate new outfit screen
        verifyScreenTitle("Outfit #0")
        val clickToTakePictureMatcher = hasText("Click to take a picture of your outfit")
        composeTestRule.waitUntilAtLeastOneExists(clickToTakePictureMatcher)
        composeTestRule.onNode(clickToTakePictureMatcher).performClick()

        //Select camera option on outfit image
        composeTestRule.waitUntilDoesNotExist(clickToTakePictureMatcher, timeoutMillis = 5000)
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTag.USE_CAMERA_SELECTION), timeoutMillis = 5000)
        composeTestRule.onNode(hasTestTag(TestTag.USE_CAMERA_SELECTION)).performClick()
        composeTestRule.waitUntilDoesNotExist(hasTestTag(TestTag.USE_CAMERA_SELECTION))
        composeTestRule.waitForIdle()

        //Capture outfit image on camera screen
        while(
            composeTestRule.onAllNodes(hasTestTag(TestTag.CAMERA_TAKE_ICON), useUnmergedTree = true)
                .fetchSemanticsNodes().isEmpty()
        ){
            runBlocking {
                delay(2000)
            }
        }
        composeTestRule.onNode(hasTestTag(TestTag.CAMERA_TAKE_ICON), useUnmergedTree = true).performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTag.SCREEN_TITLE), 10000)
        composeTestRule.onNode(clickToTakePictureMatcher).assertDoesNotExist()
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasTestTag(TestTag.BOTTOMBAR_OUTFIT)).performClick()
        composeTestRule.waitUntil {
            composeTestRule
                .onAllNodes(hasTestTag(TestTag.OUTFIT_CARD))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        //Validate new outfit is created
        composeTestRule.waitUntil(5_000) {
            val afterOutfitCount = composeTestRule.onAllNodes(hasTestTag(TestTag.OUTFIT_CARD)).fetchSemanticsNodes().size
            initialOutfitCount + 1 >= afterOutfitCount
        }
    }
}