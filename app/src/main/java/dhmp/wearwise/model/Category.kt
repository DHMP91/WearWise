package dhmp.wearwise.model

class Category(
    val id: Int,
    val name: String
)

class SubCategory(
    val id: Int,
    val name: String,
    val categoryId: Int
)



val Categories = listOf(
    Category(id = 1, name = "HATS"),
    Category(id = 2, name = "TOPS"),
    Category(id = 3, name = "BOTTOMS"),
    Category(id = 4, name = "ONEPIECE"),
    Category(id = 5, name = "OUTERWEAR"),
    Category(id = 6, name = "INTIMATES"),
    Category(id = 7, name = "FOOTWEAR"),
    Category(id = 8, name = "ACCESSORIES"),
    Category(id = 9, name = "OTHER"),
)

val SubCategories = {

}