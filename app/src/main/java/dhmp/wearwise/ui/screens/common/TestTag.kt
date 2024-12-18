package dhmp.wearwise.ui.screens.common
import java.util.UUID


object TestTag {

    //Common
    val SCREEN_TITLE = randomUUIDStr()
    val DROPDOWN_MENU_PREFIX = "DROPDOWN_MENU_"
    val SAVE_BUTTON_ENABLED = randomUUIDStr()
    val SAVE_BUTTON_DISABLED = randomUUIDStr()
    val RESULT_COUNT = randomUUIDStr()


    //BottomBar
    val BOTTOMBAR_CLOTHING = randomUUIDStr()
    val BOTTOMBAR_OUTFIT = randomUUIDStr()
    val BOTTOMBAR_USER = randomUUIDStr()


    //Clothing
    val NEW_CLOTHING_BUTTON = randomUUIDStr()
    val CLOTHING_ITEM = randomUUIDStr()
    val CLOTHING_LIST = randomUUIDStr()
    const val CLOTHING_LIST_CATEGORY_PREFIX = "CLOTHING_CATEGORY_ICON_"
    val OUTFIT_COUNT = randomUUIDStr()
    val CLOTHING_BRAND_CARD_FIELD = randomUUIDStr()
    val FILTER_PREFIX = "FILTER_"
    val FILTER_ROW_PREFIX = "FILTER_ROW_"
    val MAIN_MENU = randomUUIDStr()

    //Outfit
    val NEW_OUTFIT_BUTTON = randomUUIDStr()
    val OUTFIT_CARD = randomUUIDStr()
    val OUTFIT_THUMBNAIL = randomUUIDStr()
    val OUTFIT_GARMENT_THUMBNAIL = randomUUIDStr()

    //OutfitBuilder
    val SELECTED_GARMENT = randomUUIDStr()
    val CATEGORIZED_GARMENT_PREFIX = "CATEGORIZED_GARMENT_PREFIX_"

    //Camera
    val CAMERA_TAKE_ICON = randomUUIDStr()
    val USE_CAMERA_SELECTION = randomUUIDStr()
    val USE_GALLERY_SELECTION = randomUUIDStr()

    //User Screen
    val AI_API_KEY_INPUT = randomUUIDStr()
    val CONFIG_SAVE_BUTTON = randomUUIDStr()

    private fun randomUUIDStr(): String {
        return UUID.randomUUID().toString()
    }
}