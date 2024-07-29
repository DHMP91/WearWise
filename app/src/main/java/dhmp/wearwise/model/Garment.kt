package dhmp.wearwise.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "Garments",
    foreignKeys = [
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
    var categoryId: Int? = null,
    var occasion: Occasion? = null,
    var image: String? = null,
    var imageOfSubject: String? = null,
    var color: String? = null,
    @ColumnInfo(defaultValue = "")
    var outfitsId: List<Long> = listOf(),
    var subCategoryId: Int? = null,
//    @ColumnInfo(index = true)
//    val mlMetaDataId: Int? = 0
)
{
    @ColumnInfo(name = "brand")
    var brand: String? = null
        set(value) {
            field = value?.lowercase()?.trim()
        }
}

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

enum class Occasion {
    CASUAL,
    FORMAL,
    LOUNGE,
    SPORT,
    SEMIFORMAL,
    BUSINESS,
    DRESSYCASUAL
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

