package dhmp.wearwise.ui.screens.common

import dhmp.wearwise.R
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment


fun categoryIcon(garment: Garment, categories: List<Category>, ): Int {
    val icon = garment.categoryId?.let {
        val name = categories.find { c -> c.id == it }?.name?.lowercase()
        when (name) {
//          b66"OTHER",
            "accessories" -> R.drawable.accessory
            "intimates" -> R.drawable.intimates
            "outerwear" -> R.drawable.outer_wear
            "footwear" -> R.drawable.shoe_icon
            "onepiece" -> R.drawable.dress_icon
            "hats" -> R.drawable.hats_icon
            "tops" -> R.drawable.shirt_icon
            "bottoms" -> R.drawable.pants_icon
            else -> R.drawable.question_mark
        }
    } ?: R.drawable.question_mark
    return icon
}