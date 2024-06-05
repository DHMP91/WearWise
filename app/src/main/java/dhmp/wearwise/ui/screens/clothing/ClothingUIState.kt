package dhmp.wearwise.ui.screens.clothing

import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment

data class ClothingUIState (
    val filterExcludeType: List<Category> = listOf(),
    val filterExcludeBrand: List<String> = listOf(),
    val garments: List<Garment> = listOf(),
    val newItemId: Long = 0L
)