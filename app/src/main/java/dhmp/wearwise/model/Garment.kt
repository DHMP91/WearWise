package dhmp.wearwise.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Garments",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("categoryId")
        ),
//        ForeignKey(
//            entity = MLMetaData::class,
//            parentColumns = arrayOf("id"),
//            childColumns = arrayOf("mlMetaDataId")
//        )
    ]
)
data class Garment (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String? = null,
    @ColumnInfo(index = true)
    var categoryId: Int? = null,
//    val subCategoryId: int? = null,
    var occasion: Occasion? = null,
    var brand: String? = null,
    var image: String? = null,
    var imageOfSubject: String? = null,
//    var color: String? = null
//    @ColumnInfo(index = true)
//    val mlMetaDataId: Int? = 0
)

@Entity(tableName="MLMetaData")
class MLMetaData(
    @PrimaryKey
    val id: Long = 0,
    var labels: List<MLLabel> = listOf()
)
@Entity(tableName="MLLabels")
class MLLabel(
    @PrimaryKey
    val id: Long = 0,
    val name: String = "",
    val confidence: Double = 0.00
)

@Entity(
    tableName = "Categories",
    indices = [
        Index("name", unique = true)
    ])
class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)

@Entity(tableName = "SubCategories")
class SubCategory(
    val id: Long = 0,
    val name: String,
    val category: Category,
)


enum class Occasion {
    FORMAL,
    CASUAL,
}


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

