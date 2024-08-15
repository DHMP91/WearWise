package dhmp.wearwise.ui.screens.common
import java.util.UUID
object TestTag {
    val SCREEN_TITLE = randomUUIDStr()
    val BOTTOMBAR_CLOTHING =  randomUUIDStr()
    val BOTTOMBAR_OUTFIT =  randomUUIDStr()

    private fun randomUUIDStr(): String {
        return UUID.randomUUID().toString()
    }
}