package dhmp.wearwise.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule

open class UITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    fun getText(node: SemanticsNodeInteraction): String? {
        return node.fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)?.joinToString()
    }
}