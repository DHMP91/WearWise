package dhmp.wearwise.ui.screens.user

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasContentDescriptionExactly
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.OutfitsRepository
import dhmp.wearwise.data.UserConfigRepository
import dhmp.wearwise.model.AISource
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.CategoryCount
import dhmp.wearwise.model.ColorCount
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.model.Occasion
import dhmp.wearwise.model.OccasionCount
import dhmp.wearwise.model.Season
import dhmp.wearwise.model.SeasonCount
import dhmp.wearwise.model.UserConfig
import dhmp.wearwise.ui.UITest
import dhmp.wearwise.ui.screens.common.TestTag
import dhmp.wearwise.ui.screens.common.WearWiseBottomAppBar
import dhmp.wearwise.ui.theme.WearWiseTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.time.Duration.Companion.minutes

class UserScreenTest : UITest() {
    private lateinit var mockedGarmentRepo: GarmentsRepository
    private lateinit var mockedOutfitRepo: OutfitsRepository
    private lateinit var mockedUserConfigRepo: UserConfigRepository
    private lateinit var context: Context
    private lateinit var model: UserViewModel
    private lateinit var testDispatcher: CoroutineDispatcher
    private val defaultTimeout = 5000L
    private val fakeUserConfig = UserConfig(120, AISource.GOOGLE, "model y", "apiapiapiapiapi")


    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        mockedOutfitRepo = Mockito.mock(OutfitsRepository::class.java)
        mockedUserConfigRepo = Mockito.mock(UserConfigRepository::class.java)
        testDispatcher = StandardTestDispatcher()
        baseMock()
        model = UserViewModel(mockedGarmentRepo, mockedOutfitRepo, mockedUserConfigRepo)
    }

    @Test
    fun userConfig() = runTest {
        launchUserScreen()

        //Open User Config
        composeTestRule.onNode(hasContentDescriptionExactly("User Settings")).performClick()
        composeTestRule.onNode(hasTestTag(TestTag.AI_API_KEY_INPUT)).performClick()

        //Validate fields value is displayed from data source
        val aiSourceText = composeTestRule
            .onNode(hasTestTag("${TestTag.DROPDOWN_MENU_PREFIX}AI Source"))
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.EditableText)?.text
        val aiModelText = composeTestRule
            .onNode(hasTestTag("${TestTag.DROPDOWN_MENU_PREFIX}AI Model"))
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.EditableText)?.text
        val aiAPIText = composeTestRule
            .onNode(hasTestTag(TestTag.AI_API_KEY_INPUT))
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.EditableText)?.text
        Assert.assertEquals(fakeUserConfig.aiSource?.name, aiSourceText)
        Assert.assertEquals(fakeUserConfig.aiModelName, aiModelText)
        Assert.assertEquals(fakeUserConfig.aiApiKey, aiAPIText)
    }

    @Test
    fun userConfigSaved() = runTest (timeout = 5.minutes){
        baseMock()
        val fakeUserConfig = UserConfig(120, AISource.OPENAI, "model y", "apiapiapiapiapi")
        Mockito.`when`(mockedUserConfigRepo.getUserConfigStream()).thenAnswer {
            flow { emit(fakeUserConfig) }
        }
        model = UserViewModel(mockedGarmentRepo, mockedOutfitRepo, mockedUserConfigRepo)
        launchUserScreen()

        //Open User Config
        composeTestRule.onNode(hasContentDescriptionExactly("User Settings")).performClick()

        //Select Google Source
        composeTestRule
            .onNode(hasTestTag("${TestTag.DROPDOWN_MENU_PREFIX}AI Source")).performClick()
        composeTestRule
            .onNode(hasText(AISource.GOOGLE.name)).performClick()

        // Select Google AI Model
        val aiModel = model.getAIModels(AISource.GOOGLE)!!.first()
        composeTestRule
            .onNode(hasTestTag("${TestTag.DROPDOWN_MENU_PREFIX}AI Model"))
            .performClick()
        composeTestRule
            .onNode(hasText(aiModel)).performClick()

        //Input API Key
        val apiKey = "test test sss11111"
        composeTestRule
            .onNode(hasTestTag(TestTag.AI_API_KEY_INPUT)).performTextClearance()
        composeTestRule
            .onNode(hasTestTag(TestTag.AI_API_KEY_INPUT)).performTextInput(apiKey)

        //Click Save
        composeTestRule
            .onNode(hasTestTag(TestTag.CONFIG_SAVE_BUTTON)).performClick()

        composeTestRule.waitForIdle()

        //Validate
        verify(mockedUserConfigRepo, times(1)).updateUserConfig(
            fakeUserConfig.copy(aiSource = AISource.GOOGLE, aiModelName = aiModel, aiApiKey = apiKey)
        )
    }

    private fun launchUserScreen(){
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
                        UserScreen(model)
                    }
                }
            }
        }
    }

    private fun baseMock(){

        Mockito.`when`(mockedUserConfigRepo.getUserConfigStream()).thenAnswer {
            flow { emit(fakeUserConfig) }
        }

        Mockito.`when`(mockedGarmentRepo.getGarmentsCount(listOf(), listOf(), listOf())).thenAnswer {
            flow { emit(0) }
        }

        Mockito.`when`(mockedOutfitRepo.getOutfitsCount()).thenAnswer {
            flow { emit(0) }
        }

        Mockito.`when`(mockedGarmentRepo.getColorCount()).thenAnswer {
            flow { emit(
                listOf(
                    ColorCount(GarmentColorNames.first().name, 20,),
                    ColorCount(null, 1)
                )
            ) }
        }

        Mockito.`when`(mockedGarmentRepo.getCategoryCount()).thenAnswer {
            flow { emit(
                listOf(
                    CategoryCount(Category.categories().first().id, 20,),
                    CategoryCount(null, 1)
                )
            ) }
        }

        Mockito.`when`(mockedGarmentRepo.getOccasionCount()).thenAnswer {
            flow { emit(
                listOf(
                    OccasionCount(Occasion.entries.first(), 20),
                    OccasionCount(null, 1)
                )
            )}
        }

        Mockito.`when`(mockedOutfitRepo.getSeasonCount()).thenAnswer {
            flow { emit(
                listOf(
                    SeasonCount(Season.entries.first(), 20),
                    SeasonCount(null, 1)
                )
            )}
        }
    }

}