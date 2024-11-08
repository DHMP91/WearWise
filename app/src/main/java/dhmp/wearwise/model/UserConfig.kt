package dhmp.wearwise.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AISource {
    GOOGLE,
    OPENAI
}

@Entity(tableName = "UserConfig")
data class UserConfig(
    @PrimaryKey
    val id: Int = 1,
    @ColumnInfo(name = "AISource") var aiSource: AISource,
    @ColumnInfo(name = "AIModelName") var aiModelName: String,
    @ColumnInfo(name = "AIApiKey") var aiApiKey: String
)