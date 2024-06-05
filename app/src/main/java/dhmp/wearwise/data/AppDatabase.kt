
package dhmp.wearwise.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.GarmentDao
import dhmp.wearwise.model.MLLabel
import dhmp.wearwise.model.MLMetaData


@Database(entities = [Garment::class, Category::class, MLMetaData::class, MLLabel::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun garmentDao(): GarmentDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "wearwise_database")
                    .build().also { Instance = it }
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun listToJson(value: List<MLLabel>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<MLLabel>::class.java).toList()
}

