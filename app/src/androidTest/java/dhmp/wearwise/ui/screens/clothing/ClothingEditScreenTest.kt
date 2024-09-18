package dhmp.wearwise.ui.screens.clothing

import android.content.Context
import android.net.Uri
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasContentDescriptionExactly
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import dhmp.wearwise.data.DefaultAppContainer
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.model.Occasion
import dhmp.wearwise.ui.UITest
import dhmp.wearwise.ui.screens.common.TestTag
import dhmp.wearwise.ui.screens.fakeImage
import dhmp.wearwise.ui.theme.WearWiseTheme
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import kotlin.time.Duration.Companion.minutes

class ClothingEditScreenTest : UITest()  {
    private lateinit var mockedGarmentRepo: GarmentsRepository
    private lateinit var context: Context
    private lateinit var model: ClothingViewModel
    private lateinit var appContainer: DefaultAppContainer
    private lateinit var testImagePath: Uri
    private val garmentId: Long = 8080
    private val brands = listOf("Brand1", "Brand2", "Brand3")

    @Before
    fun setup() = runBlocking{
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        model = ClothingViewModel(mockedGarmentRepo)
        appContainer = DefaultAppContainer(context)

        val testImage = fakeImage(context, "testImage.png")
        testImagePath = testImage.toUri()
    }


    @Test
    fun garmentInfo() = runTest(timeout = 5.minutes){

        val category = Category.categories().first()
        val subCategory = category.subCategories?.first()
        val occasion = Occasion.LOUNGE
        val outfits = listOf<Long>(1, 2, 3)
        val color = "Red"

        val fakedGarment = Garment(
            id = garmentId,
            image = testImagePath.toString(),
            color = color,
            outfitsId = outfits,
            categoryId = category.id,
            subCategoryId = subCategory?.id,
            occasion = occasion
        )

        baseMock(fakedGarment)
        composeTestRule.setContent {
            WearWiseTheme {
                EditClothingScreen(
                    onFinish = {},
                    onOutfits = { _ -> },
                    onClickPicture = { _ -> },
                    onCrop =  { _ -> },
                    onBack = { },
                    garmentId = garmentId,
                    clothingViewModel = model
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(5000L) {
            composeTestRule.onNode(hasText(category.name)).isDisplayed()
        }
        Assert.assertTrue(composeTestRule.onNode(hasContentDescriptionExactly("GarmentImage")).isDisplayed())
        Assert.assertTrue(composeTestRule.onNode(hasText(subCategory!!.name)).isDisplayed())
        Assert.assertTrue(composeTestRule.onNode(hasText(color)).isDisplayed())
        Assert.assertTrue(composeTestRule.onNode(hasText(occasion.name)).isDisplayed())

        val firstOutfitCountNode = composeTestRule.onAllNodes(hasTestTag(TestTag.OUTFIT_COUNT), useUnmergedTree = true).fetchSemanticsNodes()[0]
        var text: String? = null
        for(node in firstOutfitCountNode.children){
            if(node.config.contains(SemanticsProperties.Text)){
                text = node.config.getOrNull(SemanticsProperties.Text)?.joinToString()
            }
        }
        Assert.assertTrue(text!!.toInt() == outfits.size)
    }

    @Test
    fun garmentFields() = runTest(timeout = 5.minutes){
        val categories = Category.categories()
        val category = categories.first()
        val subCategories =  category.subCategories!!
        val subCategory = subCategories.first()
        val occasion = Occasion.LOUNGE
        val outfits = listOf<Long>(1, 2, 3)
        val colors = GarmentColorNames.map { it.name }
        val color = colors.first()

        val fakedGarment = Garment(
            id = garmentId,
            image = testImagePath.toString(),
        )

        baseMock(fakedGarment)
        composeTestRule.setContent {
            WearWiseTheme {
                EditClothingScreen(
                    onFinish = {},
                    onOutfits = { _ -> },
                    onClickPicture = { _ -> },
                    onCrop =  { _ -> },
                    onBack = { },
                    garmentId = garmentId,
                    clothingViewModel = model
                )
            }
        }

        composeTestRule.waitForIdle()
        val verifyLabels = listOf("Category", "Color", "Brand", "Occasion")
        for(label in verifyLabels){
            Assert.assertTrue(composeTestRule.onNode(hasText(label)).isDisplayed())
        }

        //Categpry
        composeTestRule.onNode(hasTestTag("${TestTag.DROPDOWN_MENU_PREFIX}Category")).performClick()
        for(c in categories){
            if(!composeTestRule.onNode(hasText(c.name)).isDisplayed()){
                composeTestRule.onNode(hasText(c.name)).performScrollTo()
            }
            Assert.assertTrue(composeTestRule.onNode(hasText(c.name)).isDisplayed())
        }
        composeTestRule.onNode(hasText(category.name)).performScrollTo()
        composeTestRule.onNode(hasText(category.name)).performClick()
        composeTestRule.onNode(hasText(category.name)).isDisplayed()

        //Subcategory
        composeTestRule.onNode(hasTestTag("${TestTag.DROPDOWN_MENU_PREFIX}SubCategory")).performClick()
        for(c in subCategories){
            if(!composeTestRule.onNode(hasText(c.name)).isDisplayed()){
                composeTestRule.onNode(hasText(c.name)).performScrollTo()
            }
            Assert.assertTrue(composeTestRule.onNode(hasText(c.name)).isDisplayed())
        }
        composeTestRule.onNode(hasText(subCategory.name)).performScrollTo()
        composeTestRule.onNode(hasText(subCategory.name)).performClick()
        composeTestRule.onNode(hasText(subCategory.name)).isDisplayed()

        //Occasion
        composeTestRule.onNode(hasTestTag("${TestTag.DROPDOWN_MENU_PREFIX}Occasion")).performClick()
        for(x in Occasion.entries){
            if(!composeTestRule.onNode(hasText(x.name)).isDisplayed()){
                composeTestRule.onNode(hasText(x.name)).performScrollTo()
            }
            Assert.assertTrue(composeTestRule.onNode(hasText(x.name)).isDisplayed())
        }
        composeTestRule.onNode(hasText(occasion.name)).performClick()
        composeTestRule.onNode(hasText(occasion.name)).isDisplayed()

        //Color
        composeTestRule.onNode(hasTestTag("${TestTag.DROPDOWN_MENU_PREFIX}Color")).performClick()
        for(x in colors){
            if(!composeTestRule.onNode(hasText(x)).isDisplayed()){
                composeTestRule.onNode(hasText(x)).performScrollTo()
            }
            Assert.assertTrue(composeTestRule.onNode(hasText(x)).isDisplayed())
        }
        composeTestRule.onNode(hasText(color)).performClick()
        composeTestRule.onNode(hasText(color)).isDisplayed()

        //Brand
        composeTestRule.onNode(hasTestTag("${TestTag.DROPDOWN_MENU_PREFIX}Brand")).performClick()
        for(x in brands){
            if(!composeTestRule.onNode(hasText(x)).isDisplayed()){
                composeTestRule.onNode(hasText(x)).performScrollTo()
            }
            Assert.assertTrue(composeTestRule.onNode(hasText(x)).isDisplayed())
        }
        composeTestRule.onNode(hasTestTag("${TestTag.DROPDOWN_MENU_PREFIX}Brand")).performTextInput("AnythingBrand")
        composeTestRule.onNode(hasText("AnythingBrand")).isDisplayed()

    }



    private fun baseMock(fakedGarment: Garment) = runTest{
        Mockito.`when`(mockedGarmentRepo.getBrands()).thenAnswer {
            flow {
                emit(brands)
            }
        }

        Mockito.`when`(mockedGarmentRepo.getGarmentStream(garmentId)).thenAnswer{
            flow {
                emit(
                    fakedGarment
                )
            }
        }
    }
}