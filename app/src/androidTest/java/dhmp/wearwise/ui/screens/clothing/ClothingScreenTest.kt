package dhmp.wearwise.ui.screens.clothing

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import dhmp.wearwise.App
import dhmp.wearwise.ui.UITest
import dhmp.wearwise.ui.screens.common.TestTag
import dhmp.wearwise.ui.theme.WearWiseTheme
import dhmp.wearwise.ui.verifyScreenTitle
import org.junit.Test


class ClothingScreenUITest: UITest() {
    private val clothingRegexTitle = Regex("Clothing \\(\\d+\\)")

    @Test
    fun landingScreen() {
        composeTestRule.setContent {
            WearWiseTheme {
                App()
            }
        }
        verifyScreenTitle(clothingRegexTitle)
    }


    @OptIn(ExperimentalTestApi::class)
    @Test
    fun newClothing(){
        composeTestRule.setContent {
            WearWiseTheme {
                App()
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTag.NEW_CLOTHING_BUTTON))
        composeTestRule.onNode(hasTestTag(TestTag.NEW_CLOTHING_BUTTON)).performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTag.USE_CAMERA_SELECTION))
        composeTestRule.onNode(hasTestTag(TestTag.USE_CAMERA_SELECTION)).performClick()

        composeTestRule.waitUntilDoesNotExist(hasTestTag(TestTag.USE_CAMERA_SELECTION))
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasTestTag(TestTag.CAMERA_TAKE_ICON), useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTag.SCREEN_TITLE), 5000)
        composeTestRule.waitForIdle()
        verifyScreenTitle( Regex("Clothing Item #\\d+"))

        composeTestRule.onNode(hasTestTag(TestTag.BOTTOMBAR_CLOTHING)).performClick()
        composeTestRule.waitForIdle()
        verifyScreenTitle(clothingRegexTitle)


    }

}