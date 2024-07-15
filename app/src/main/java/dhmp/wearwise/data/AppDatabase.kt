
package dhmp.wearwise.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dhmp.wearwise.R
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.CategoryDao
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.GarmentDao
import dhmp.wearwise.model.MLLabel
import dhmp.wearwise.model.MLMetaData
import dhmp.wearwise.model.Outfit
import dhmp.wearwise.model.OutfitDao


@Database(
    entities = [Garment::class, Category::class, Outfit::class, MLMetaData::class, MLLabel::class],
    version = 7,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 6, to = 7)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun garmentDao(): GarmentDao
    abstract fun categoryDao(): CategoryDao
    abstract fun outfitDao(): OutfitDao

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
                    .addMigrations(
                        MIGRATION_3_4,
                        MIGRATION_5_6
                    )
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

    @TypeConverter
    fun longListToString(value: List<Long>): String = Gson().toJson(value)

    @TypeConverter
    fun stringToLongList(data: String): List<Long> {
        return if (data.isEmpty()) {
            emptyList()
        } else {
            data.let {
                val listType = object : TypeToken<List<Long>>() {}.type
                Gson().fromJson(it, listType)
            }
        }
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create the new table if it doesn't exist
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `Outfits` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT,
                `image` TEXT,
                `season` TEXT NOT NULL DEFAULT '',
                `garmentsId` TEXT NOT NULL
            )
        """)
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create the new table if it doesn't exist
        db.execSQL("ALTER TABLE Garments ADD COLUMN color TEXT")
    }
}