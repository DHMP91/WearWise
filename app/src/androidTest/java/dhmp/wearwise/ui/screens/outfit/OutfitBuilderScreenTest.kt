package dhmp.wearwise.ui.screens.outfit

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasContentDescriptionExactly
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.OutfitsRepository
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
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.io.File
import kotlin.time.Duration.Companion.minutes

class OutfitBuilderScreenTest: UITest() {
    private lateinit var mockedGarmentRepo: GarmentsRepository
    private lateinit var mockedOutfitRepo: OutfitsRepository
    private lateinit var context: Context
    private lateinit var clothingModel: ClothingViewModel
    private lateinit var model: OutfitViewModel
    private lateinit var fakeOutfitImage: File
    private lateinit var fakeImage: File
    private lateinit var fakeOutfit: Outfit
    private lateinit var fakeGarment: Garment
    private val outfitId = 9090L
    private val garmentsPerCategory = 5

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        mockedOutfitRepo = Mockito.mock(OutfitsRepository::class.java)
        clothingModel = ClothingViewModel(mockedGarmentRepo)
        model = OutfitViewModel(mockedGarmentRepo, mockedOutfitRepo)
        fakeOutfitImage = fakeImage(context, "test_outfitbuilder.png")
        fakeImage = fakeImage(context, "test_outfitBuilder.png")
        fakeOutfit = Outfit(
            id = outfitId,
            image = fakeImage.toUri().toString(),
            garmentsId = listOf(1,2,3)
        )
        fakeGarment = Garment(
            id = 9090,
            image = fakeImage.toUri().toString(),
        )
    }

    @Test
    fun outfitBuilder_default() = runTest(timeout = 5.minutes){
        base()
        verifyScreenTitle("Outfit #${outfitId}")
        composeTestRule.onNode(hasContentDescriptionExactly("Delete Outfit")).assertIsDisplayed()
        composeTestRule.onNode(hasContentDescriptionExactly("Crop Image")).assertIsDisplayed()
        composeTestRule.onNode(hasContentDescriptionExactly("Retake Image")).assertIsDisplayed()
        composeTestRule.onNode(hasText("Save Outfit")).assertIsDisplayed()
        val selectedGarmentsCount = composeTestRule.onAllNodes(hasTestTag(TestTag.SELECTED_GARMENT)).fetchSemanticsNodes().size
        Assert.assertEquals(fakeOutfit.garmentsId.size, selectedGarmentsCount)

        for(c in Category.categories()) {
            val categorizedGarmentsCount =
                composeTestRule.onAllNodes(hasTestTag("${TestTag.CATEGORIZED_GARMENT_PREFIX}${c.id}")).fetchSemanticsNodes().size
            Assert.assertEquals(0, categorizedGarmentsCount)
        }
    }


    @Test
    fun outfitBuilder_removeSelectedGarments() = runTest(timeout = 5.minutes){
        val mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        val mockedOutfitRepo = Mockito.mock(OutfitsRepository::class.java)
        val clothingModel = ClothingViewModel(mockedGarmentRepo)
        val model = OutfitViewModel(mockedGarmentRepo, mockedOutfitRepo)

        Mockito.`when`(mockedGarmentRepo.updateGarment(any())).thenAnswer{}
        Mockito.`when`(mockedOutfitRepo.updateOutfit(any())).thenAnswer{}

        base(mockedOutfitRepo, mockedGarmentRepo, model, clothingModel)
        composeTestRule.waitUntil{
            composeTestRule.onNode(hasText("Save Outfit")).isDisplayed()
        }
        composeTestRule.waitForIdle()
        val selectedGarmentsCount = {
            composeTestRule.onAllNodes(hasTestTag(TestTag.SELECTED_GARMENT)).fetchSemanticsNodes().size
        }
        while(selectedGarmentsCount() > 0) {
            composeTestRule.onAllNodes(
                hasContentDescription("Remove From Outfit")
                    .and(hasParent(hasTestTag(TestTag.SELECTED_GARMENT)))
            ).onFirst().performClick()
            composeTestRule.waitForIdle()
        }
        Assert.assertEquals(0, selectedGarmentsCount())
        composeTestRule.onNode(hasText("Save Outfit")).performClick()
        composeTestRule.waitForIdle()
        verify(mockedGarmentRepo, times(3)).updateGarment(
            any()
        )

        verify(mockedOutfitRepo, times(1)).updateOutfit(
            any()
        )
    }

    @Test
    fun outfitBuilder_saveNoChanges() = runTest(timeout = 5.minutes){
        val mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        val mockedOutfitRepo = Mockito.mock(OutfitsRepository::class.java)
        val clothingModel = ClothingViewModel(mockedGarmentRepo)
        val model = OutfitViewModel(mockedGarmentRepo, mockedOutfitRepo)

        Mockito.`when`(mockedGarmentRepo.updateGarment(any())).thenAnswer{}
        Mockito.`when`(mockedOutfitRepo.updateOutfit(any())).thenAnswer{}

        base(mockedOutfitRepo, mockedGarmentRepo, model, clothingModel)
        composeTestRule.waitUntil{
            composeTestRule.onNode(hasText("Save Outfit")).isDisplayed()
        }

        composeTestRule.onNode(hasText("Save Outfit")).performClick()
        composeTestRule.waitForIdle()
        verify(mockedGarmentRepo, times(0)).updateGarment(
            any()
        )

        verify(mockedOutfitRepo, times(0)).updateOutfit(
            any()
        )
    }

    @Test
    fun outfitBuilder_addSelectedGarments() = runTest(timeout = 5.minutes){
        val mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        val mockedOutfitRepo = Mockito.mock(OutfitsRepository::class.java)
        val clothingModel = ClothingViewModel(mockedGarmentRepo)
        val model = OutfitViewModel(mockedGarmentRepo, mockedOutfitRepo)

        Mockito.`when`(mockedGarmentRepo.updateGarment(any())).thenAnswer{}
        Mockito.`when`(mockedOutfitRepo.updateOutfit(any())).thenAnswer{}

        base(mockedOutfitRepo, mockedGarmentRepo, model, clothingModel)
        composeTestRule.waitUntil{
            composeTestRule.onNode(hasText("Save Outfit")).isDisplayed()
        }

        //add garments
        val category = Category.categories().first()
        val categoryNode =  composeTestRule.onNode(hasText("${category.name} ($garmentsPerCategory)"))
        categoryNode.performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodes(hasTestTag("${TestTag.CATEGORIZED_GARMENT_PREFIX}${category.id}")).onFirst().performClick()

        delay(1000)
        composeTestRule.onNode(hasText("Save Outfit")).performClick()
        composeTestRule.waitForIdle()

        verify(mockedGarmentRepo, times(4)).updateGarment(
            any()
        )

        verify(mockedOutfitRepo, times(1)).updateOutfit(
            any()
        )
    }

    @Test
    fun outfitBuilder_garmentCategoryScroll() = runTest(timeout = 5.minutes){
        base()
        var lastCategory: String? = null
        for(c in Category.categories()) {
            val categoryNode =  composeTestRule.onNode(hasText("${c.name} ($garmentsPerCategory)"))
            if(lastCategory != null)
            {
                val scrollUpEndPosition =  composeTestRule.onNode(hasText("$lastCategory ($garmentsPerCategory)")).fetchSemanticsNode().boundsInRoot.topCenter
                val scrollUpStartPosition = categoryNode.fetchSemanticsNode().boundsInRoot.bottomCenter
                composeTestRule.onRoot().performTouchInput {
                    swipe(
                        start = Offset(scrollUpStartPosition.x, scrollUpStartPosition.y),
                        end = Offset(scrollUpEndPosition.x, scrollUpEndPosition.y),
                        durationMillis = 1000 // Adjust duration as needed
                    )
                }
            }
            categoryNode.performClick()
            composeTestRule.waitForIdle()
            lastCategory = c.name

            val garments =  {
                composeTestRule.onAllNodes(hasTestTag("${TestTag.CATEGORIZED_GARMENT_PREFIX}${c.id}")).fetchSemanticsNodes()
            }
            var categorizedGarmentsCount = garments().size
            if(categorizedGarmentsCount == 0){
                categoryNode.performClick()
                categorizedGarmentsCount = garments().size
            }
            Assert.assertTrue(categorizedGarmentsCount in 2..4)

            composeTestRule.onAllNodes(hasTestTag("${TestTag.CATEGORIZED_GARMENT_PREFIX}${c.id}")).apply {
                onFirst().performScrollTo()
            }
            val startPosition = garments().last().boundsInRoot.centerRight
            val endPosition = garments().first().boundsInRoot.centerLeft
            composeTestRule.onRoot().performTouchInput {
                swipe(
                    start = Offset(startPosition.x, startPosition.y),
                    end = Offset(endPosition.x, endPosition.y),
                    durationMillis = 1000 // Adjust duration as needed
                )
            }
            composeTestRule.waitForIdle()
            val initialCount = categorizedGarmentsCount
            categorizedGarmentsCount = garments().size
            Assert.assertTrue(initialCount < categorizedGarmentsCount)
            categoryNode.performClick()
        }
    }

    private suspend fun base(
        mockedOutfitRepo: OutfitsRepository = this.mockedOutfitRepo,
        mockedGarmentRepo: GarmentsRepository = this.mockedGarmentRepo,
        outfitViewModel: OutfitViewModel = model,
        clothingViewModel: ClothingViewModel = clothingModel
    ) {
        Mockito.`when`(mockedOutfitRepo.getOutfitStream(fakeOutfit.id)).thenAnswer{
            flow {
                emit(fakeOutfit)
            }
        }

        fakeOutfit.garmentsId.forEach{ id ->
            Mockito.`when`(mockedGarmentRepo.getGarmentStream(id)).thenAnswer{
                flow {
                    emit(
                        Garment(
                            id = id,
                            image = fakeImage.toUri().toString(),
                            outfitsId = listOf(fakeOutfit.id)
                        )
                    )
                }
            }
        }

//        Mockito.`when`(mockedGarmentRepo.getGarmentStream(any())).thenAnswer{
//            flow {
//                emit(fakeGarment)
//            }
//        }

        Mockito.`when`(mockedGarmentRepo.getGarmentsByCategoryPaged(anyOrNull())).thenAnswer{ invocation: InvocationOnMock ->
            val categoryId = invocation.arguments[0] as Int? // Access the argument
            val fakeGarments = mutableListOf<Garment>()
            repeat(garmentsPerCategory){
                val garment = Garment(
                    id = (categoryId?.times(1000) ?: 0) + it.toLong(),
                    categoryId = categoryId,
                    image = fakeImage.toUri().toString()
                )
                Mockito.`when`(mockedGarmentRepo.getGarmentStream(garment.id)).thenAnswer{
                    flow {
                        emit(
                            garment
                        )
                    }
                }
                fakeGarments.add(garment)
            }
            FakePagingSource(
                fakeGarments
            )
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
                    Box(modifier = Modifier.padding(innerPadding)) {
                        EditOutfitScreen(
                            id = outfitId,
                            onTakePicture = { _ -> },
                            onClickPicture = { _ -> },
                            onCrop = { _ -> },
                            onFinish = {},
                            onBack = {},
                            clothingViewModel = clothingViewModel,
                            outfitViewModel = outfitViewModel
                        )
                    }
                }
            }
        }
    }

    // Test outfit image processing

}