package dhmp.wearwise.ui

import androidx.compose.ui.test.hasTestTag
import dhmp.wearwise.ui.screens.common.TestTag
import org.junit.Assert


fun UITest.verifyScreenTitle(title: String){
    verifyScreenTitle(expectedTitle = title)
}

fun UITest.verifyScreenTitle(title: Regex){
    verifyScreenTitle(regexTitle = title)
}

private fun UITest.verifyScreenTitle(expectedTitle: String? = null, regexTitle: Regex? = null){
    val text = getText(composeTestRule.onNode(hasTestTag(TestTag.SCREEN_TITLE)))
    var matches = false
    text?.let{
        expectedTitle?.let { title ->
            matches = title == it
        }
        regexTitle?.let { r ->
            matches = r.matches(it)
        }
    }
    Assert.assertTrue(matches)
}