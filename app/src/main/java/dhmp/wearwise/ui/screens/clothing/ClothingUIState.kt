package dhmp.wearwise.ui.screens.clothing

import dhmp.wearwise.model.Category

data class ClothingUIState (
    val filterExcludeType: List<Category> = listOf(),
    val filterExcludeBrand: List<String> = listOf(),
    val newItemId: Long = 0L
)