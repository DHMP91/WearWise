package dhmp.wearwise.ui.screens.clothing

import androidx.compose.ui.test.hasTestTag
import dhmp.wearwise.App
import dhmp.wearwise.ui.UITest
import dhmp.wearwise.ui.screens.common.TestTag
import dhmp.wearwise.ui.theme.WearWiseTheme
import dhmp.wearwise.ui.verifyScreenTitle
import org.junit.Assert
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
        val text = getText(composeTestRule.onNode(hasTestTag(TestTag.SCREEN_TITLE)))
        var matches = false
        text?.let{
            matches = Regex("Clothing \\(\\d+\\)").matches(it)
        }
        Assert.assertTrue(matches)

        verifyScreenTitle(clothingRegexTitle)
    }

}