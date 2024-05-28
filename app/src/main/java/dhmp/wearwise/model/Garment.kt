package dhmp.wearwise.model


data class Garment (
    val id: Int = 0,
    val name: String? = null,
    val category: Category? = null,
    val subCategory: SubCategory? = null,
    val occasion: Occasion? = null,
    val brand: String? = null,
    val image: Byte? = null,
)

class Category(
    val id: Int = 0,
    val name: String
)

class SubCategory(
    val id: Int = 0,
    val name: String,
    val category: Category,
)

class TopType(
    val type: String = ""
)

enum class Occasion {
    FORMAL,
    CASUAL,
}


// TO BE MOVED TO DATABASE
val garmentCategory: List<Category> =
    listOf(
        Category(1, "TOPS"),
        Category(2, "BOTTOMS"),
        Category(3, "ONEPIECE"),
        Category(4, "OUTERWEAR"),
        Category(5, "INTIMATES"),
        Category(6, "FOOTWEAR"),
        Category(7, "ACCESSORIES"),
        Category(9, "OTHER")
    )

val garments: List<Garment> =
    listOf(
        Garment(0, category = garmentCategory[0]),
        Garment(1),
        Garment(2),
        Garment(3),
        Garment(4),
        Garment(5)
    )


//val topType: Set<String> =
//    setOf(
//        "TSHIRT",
//        "LONGSLEEVE",
//        "SWEATER",
//        "BLOUSE",
//        "BUTTONUP",
//        "DRESSSHIRT"
//    )


//val BottomType : Set<String> =
//setOf(
//"TSHIRT",
//"LONGSLEEVE",
//"SWEATER",
//"BLOUSE",
//"BUTTONUP",
//"DRESSSHIRT"
//)

