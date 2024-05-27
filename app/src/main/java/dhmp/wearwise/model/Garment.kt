package dhmp.wearwise.model


data class Garment (
    val name: String,
    val type: GarmentType? = null,
    val subType: TopType? = null,
    val occasion: Occasion = Occasion.CASUAL,
    val id: Int = 0
)

class GarmentType(
    val type: String = ""
)

class TopType(
    val type: String = ""
)

enum class Occasion {
    FORMAL,
    CASUAL,
}


// TO BE MOVED TO DATABASE
val garmentTypes: List<GarmentType> =
    listOf(
        GarmentType("TOPS"),
        GarmentType("BOTTOMS"),
        GarmentType("ONEPIECE"),
        GarmentType("OUTERWEAR"),
        GarmentType("INTIMATES"),
        GarmentType("FOOTWEAR"),
        GarmentType("ACCESSORIES"),
        GarmentType("OTHER")
    )

val garments: List<Garment> =
    listOf(
        Garment("Shirt1", type = garmentTypes[0]),
        Garment("Bottom1"),
        Garment("Shirt2"),
        Garment("Bottom2"),
        Garment("Dress1"),
        Garment("Dress2")
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

