package dhmp.wearwise.ui.screens.clothing

import dhmp.wearwise.model.Garment

data class EditClothingUIState (
    val editGarment: Garment = Garment(),
    val changes: Garment? = null
)