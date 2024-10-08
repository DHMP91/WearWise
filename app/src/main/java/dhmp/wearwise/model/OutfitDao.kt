package dhmp.wearwise.model

import androidx.paging.PagingSource
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
    fun getOutfitOnce(id: Long): Outfit

    @Query("SELECT * from Outfits WHERE id = :id")
    fun getOutfit(id: Long): Flow<Outfit>

    @Query("SELECT * from Outfits")
    fun getAllOutfits(): Flow<List<Outfit>>

    @Query("SELECT * from Outfits ORDER BY id DESC")
    fun getAllOutfitsPaged(): PagingSource<Int, Outfit>

    @Query("SELECT * from Outfits WHERE id IN (:ids) ORDER BY id DESC")
    fun getOutfitsByListOfIdsPaged(ids: List<Long>): PagingSource<Int, Outfit>

    @Query("SELECT COUNT(*) from Outfits " +
            "WHERE (Outfits.season NOT IN (:excludeSeason))")
    fun getOutfitsCount(excludeSeason: List<Season>): Flow<Int>

    @Query("SELECT Outfits.season AS season, COUNT(Outfits.id) AS count " +
            "FROM Outfits " +
            "GROUP BY Outfits.season")
    fun getSeasonCount(): Flow<List<SeasonCount>>
}