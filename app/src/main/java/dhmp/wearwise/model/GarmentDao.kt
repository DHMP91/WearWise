package dhmp.wearwise.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GarmentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Garment): Long

    @Update
    suspend fun update(item: Garment)

    @Delete
    suspend fun delete(item: Garment)

    @Query("SELECT * from Garments WHERE id = :id")
    fun getGarment(id: Long): Flow<Garment>

    @Query("SELECT * from Garments ORDER BY name ASC")
    fun getAllGarments(): Flow<List<Garment>>

    @Query("SELECT DISTINCT Garments.brand from Garments WHERE Garments.brand IS NOT NULL ORDER BY brand ASC")
    fun getBrands(): Flow<List<String>>
}