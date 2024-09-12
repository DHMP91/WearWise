package dhmp.wearwise.ui

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule

open class UITest {
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.INTERNET
    )

    @get:Rule
    val composeTestRule = createComposeRule()


    fun getText(node: SemanticsNodeInteraction): String? {
        return getText(node.fetchSemanticsNode())
    }

    fun getText(node: SemanticsNode): String? {
        return node.config.getOrNull(SemanticsProperties.Text)?.joinToString()
    }
}