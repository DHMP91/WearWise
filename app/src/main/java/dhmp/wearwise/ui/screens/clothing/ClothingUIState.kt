package dhmp.wearwise.ui.screens.clothing

import dhmp.wearwise.model.Category

data class ClothingMenuUIState (
    val filterExcludeCategories: List<Category> = listOf(),
    val filterExcludeBrands: List<String> = listOf(),
    val filterExcludeColors: List<String> = listOf(),
    val showMenu: Boolean = false,
    val showBrandFilterMenu: Boolean = false,
    val showColorFilterMenu: Boolean = false,
    val showCategoryFilterMenu: Boolean = false
)