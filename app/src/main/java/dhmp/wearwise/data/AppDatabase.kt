
package dhmp.wearwise.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import dhmp.wearwise.R
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.CategoryDao
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.GarmentDao
import dhmp.wearwise.model.MLLabel
import dhmp.wearwise.model.MLMetaData


@Database(
    entities = [Garment::class, Category::class, MLMetaData::class, MLLabel::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun garmentDao(): GarmentDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "${context.getString(R.string.app_name)}.db")
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            PrePopulateDB(db).populateCategory()
                        }
                    })
                    .build().also {
                        Instance = it
                    }
            }
        }
    }
}


class PrePopulateDB(private val db: SupportSQLiteDatabase){
    fun populateCategory() {
        val categories = listOf(
            Category(name = "HATS"),
            Category(name = "TOPS"),
            Category(name = "BOTTOMS"),
            Category(name = "ONEPIECE"),
            Category(name = "OUTERWEAR"),
            Category(name = "INTIMATES"),
            Category(name = "FOOTWEAR"),
            Category(name = "ACCESSORIES"),
            Category(name = "OTHER"),
        )
        categories.forEach {
            db.execSQL("INSERT INTO Categories (name) VALUES ('${it.name}')")
        }
    }
}

class Converters {
    @TypeConverter
    fun listToJson(value: List<MLLabel>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<MLLabel>::class.java).toList()
}

