package dhmp.wearwise.ui.screens.clothing

import dhmp.wearwise.model.GarmentType

data class ClothingUIState (
    val filterInclude: List<GarmentType> = listOf()
)