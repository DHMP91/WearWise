package dhmp.wearwise.model

class Category(
    val id: Int,
    val name: String,
    val subCategories: Set<Category>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Category) return false

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        return result
    }

    companion object {
        private val hatsSubCategory = setOf(
            Category( id = 1001, name = "Caps"),
            Category( id = 1002, name = "Visor"),
            Category( id = 1003, name = "Beanie"),
        )
        private val hatsCategory = Category(
            id = 1,
            name = "HATS",
            subCategories = hatsSubCategory
        )

        private val topsSubCategory = setOf(
            Category( id = 2001, name = "TShirt"),
            Category( id = 2002, name = "Long Sleeve"),
            Category( id = 2003, name = "Beater"),
            Category( id = 2004, name = "Button Up"),
            Category( id = 2005, name = "Dress Shirt"),
        )
        private val topsCategory = Category(
            id = 2,
            name = "TOPS",
            subCategories = topsSubCategory
        )

        private val bottomSubCategory = setOf(
            Category( id = 3001, name = "Shorts"),
            Category( id = 3002, name = "Denim"),
            Category( id = 3003, name = "Legging"),
            Category( id = 3004, name = "Khakis"),
        )
        private val bottomCategory = Category(
            id = 3,
            name = "BOTTOMS",
            subCategories = bottomSubCategory

        )

        private val onePieceSubCategory = setOf(
            Category( id = 4001, name = "Jumpsuit"),
            Category( id = 4002, name = "Onesie"),
            Category( id = 4003, name = "Dress"),
        )
        private val onePieceCategory = Category(
            id = 4,
            name = "ONEPIECE",
            subCategories = onePieceSubCategory
        )

        private val outerWearSubCategory = setOf(
            Category( id = 5001, name = "Cardigan"),
            Category( id = 5002, name = "Winter Jacket"),
            Category( id = 5003, name = "Rain Coat"),
            Category( id = 5004, name = "Hoodie"),
            Category( id = 5005, name = "Blazer"),
            Category( id = 5006, name = "Sweater"),
        )
        private val outerWearCategory = Category(
            id = 5,
            name = "OUTERWEAR",
            subCategories = outerWearSubCategory
        )

        private val intimatesSubCategory = setOf(
        Category( id = 6001, name = "Underwear"),
        Category( id = 6002, name = "Bra")
        )
        private val intimatesCategory = Category(
            id = 6,
            name = "INTIMATES",
            subCategories = intimatesSubCategory
        )

        private val footwearSubCategory = setOf(
            Category( id = 7001, name = "Sneakers"),
            Category( id = 7002, name = "Dress Shoe"),
            Category( id = 7003, name = "Heels"),
            Category( id = 7004, name = "Flats"),
            Category( id = 7005, name = "Boots"),
            Category( id = 7006, name = "Sandals"),
            Category( id = 7007, name = "Loafers"),
            Category( id = 7008, name = "Slides"),
        )
        private val footwearCategory = Category(
            id = 7,
            name = "FOOTWEAR",
            subCategories = footwearSubCategory
        )

        private val accessorySubCategory = setOf(
            Category( id = 8001, name = "Watch"),
            Category( id = 8002, name = "Earring"),
            Category( id = 8003, name = "Necklace"),
            Category( id = 8004, name = "Glasses"),
            Category( id = 8005, name = "Bracelet"),
            Category( id = 8006, name = "Ring"),
            Category( id = 8007, name = "Gloves")
        )
        private val accessoryCategory = Category(
            id = 8,
            name = "ACCESSORY",
            subCategories = accessorySubCategory

        )
        private val otherCategory = Category(
            id = 9,
            name = "OTHER"
        )

        fun categories(): List<Category> {
            return setOf(
                hatsCategory,
                topsCategory,
                bottomCategory,
                onePieceCategory,
                outerWearCategory,
                intimatesCategory,
                footwearCategory,
                accessoryCategory,
                otherCategory,
            ).toList()
        }

        fun getCategory(id: Int): Category? {
            if(id <= 1000) {
                return categories().find { it.id == id }
            }else{
                for (c in Categories){
                    val subCategory = c.subCategories?.find { it.id == id }
                    if(subCategory != null)
                        return subCategory
                }
            }
            return null
        }

        fun getCategory(name: String): Category?{
            categories().forEach { category ->
                if(category.name == name){
                    return category
                } else {
                    category.subCategories?.let { subCategories ->
                        subCategories.forEach { subCategory ->
                            if(subCategory.name == name) {
                                return subCategory
                            }
                        }
                    }
                }
            }
            return null
        }


    }
}

val Categories = Category.categories()

data class CategoryCount (
    val categoryId: Int,
    val count: Int
) {
    val categoryName: String?
        get() = Category.getCategory(categoryId)?.name
}

