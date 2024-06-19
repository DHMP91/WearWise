package dhmp.wearwise.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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