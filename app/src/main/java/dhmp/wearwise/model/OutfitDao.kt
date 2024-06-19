package dhmp.wearwise.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OutfitDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Outfit): Long

    @Update
    suspend fun update(item: Outfit)

    @Delete
    suspend fun delete(item: Outfit)

    @Query("SELECT * from Outfits WHERE id = :id")
    fun getOutfit(id: Long): Flow<Outfit>

    @Query("SELECT * from Outfits")
    fun getAllOutfits(): Flow<List<Outfit>>
}