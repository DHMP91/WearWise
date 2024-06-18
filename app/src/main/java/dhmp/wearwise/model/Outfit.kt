package dhmp.wearwise.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey


enum class Season {
    WINTER,
    SPRING,
    SUMMER,
    FALL,
    ANY
}

data class Outfit (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String? = null,
    @ColumnInfo(index = true)
    var garments: List<Garment> = listOf(),
    var season: Season = Season.ANY
)