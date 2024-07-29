package dhmp.wearwise.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


enum class Season {
    WINTER,
    SPRING,
    SUMMER,
    FALL,
    ANY
}

@Entity(tableName = "Outfits")
data class Outfit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String? = null,
    var image: String? = null,
    var garmentsId: List<Long> = listOf(),
    @ColumnInfo(defaultValue = "")
    val season: Season = Season.ANY
)