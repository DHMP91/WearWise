package dhmp.wearwise.ui.screens.common
import java.util.UUID


object TestTag {

    //Common
    val SCREEN_TITLE = randomUUIDStr()


    //BottomBar TestTags
    val BOTTOMBAR_CLOTHING = randomUUIDStr()
    val BOTTOMBAR_OUTFIT = randomUUIDStr()


    //Clothing TestTags
    val NEW_CLOTHING_BUTTON = randomUUIDStr()
    val CLOTHING_ITEM = randomUUIDStr()
    val CLOTHING_LIST = randomUUIDStr()
    const val CLOTHING_LIST_CATEGORY_PREFIX = "CLOTHING_CATEGORY_ICON_"
    val OUTFIT_COUNT = randomUUIDStr()
    val CLOTHING_BRAND_CARD_FIELD = randomUUIDStr()



    //Camera TestTags
    val CAMERA_TAKE_ICON = randomUUIDStr()
    val USE_CAMERA_SELECTION = randomUUIDStr()
    val USE_GALLERY_SELECTION = randomUUIDStr()

    private fun randomUUIDStr(): String {
        return UUID.randomUUID().toString()
    }
}