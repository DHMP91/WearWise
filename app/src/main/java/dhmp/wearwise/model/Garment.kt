package dhmp.wearwise.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "Garments",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("categoryId")
        )
    ]
)
data class Garment (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String? = null,
    @ColumnInfo(index = true)
    val categoryId: Int? = null,
//    val subCategoryId: int? = null,
    val occasion: Occasion? = null,
    val brand: String? = null,
    val image: Byte? = null,
)

@Entity(tableName = "Categories")
class Category(
    @PrimaryKey
    val id: Int = 0,
    val name: String
)

@Entity(tableName = "SubCategories")
class SubCategory(
    val id: Int = 0,
    val name: String,
    val category: Category,
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
        Garment(0, categoryId = garmentCategory[0].id),
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

