package dhmp.wearwise.ui.screens.common

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import dhmp.wearwise.App
import dhmp.wearwise.ui.UITest
import dhmp.wearwise.ui.theme.WearWiseTheme
import dhmp.wearwise.ui.verifyScreenTitle
import org.junit.Test

class WearWiseBottomAppBarUITest: UITest() {

    @Test
    fun bottomNavigation() {
        composeTestRule.setContent {
            WearWiseTheme {
                App()
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasTestTag(TestTag.BOTTOMBAR_OUTFIT)).performClick()
        composeTestRule.waitForIdle()
        verifyScreenTitle("Outfits")
        composeTestRule.onNode(hasTestTag(TestTag.BOTTOMBAR_CLOTHING)).performClick()
        composeTestRule.waitForIdle()
        verifyScreenTitle("Clothing")
    }
}