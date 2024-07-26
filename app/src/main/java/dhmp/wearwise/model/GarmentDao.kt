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
interface GarmentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Garment): Long

    @Update
    suspend fun update(item: Garment)

    @Delete
    suspend fun delete(item: Garment)

    @Query("SELECT * from Garments WHERE id = :id")
    fun getGarment(id: Long): Flow<Garment>

    @Query("SELECT * from Garments")
    fun getAllGarments(): Flow<List<Garment>>

    @Query("SELECT DISTINCT Garments.brand from Garments WHERE Garments.brand IS NOT NULL ORDER BY brand ASC")
    fun getBrands(): Flow<List<String>>

    @Query("SELECT DISTINCT Garments.brand from Garments WHERE Garments.brand IS NOT NULL ORDER BY brand ASC")
    fun getGarments(): Flow<List<String>>

    @Query("SELECT * FROM Garments ORDER BY id DESC")
    fun getAllGarmentsPaged(): PagingSource<Int, Garment>

    @Query("SELECT * FROM Garments WHERE Garments.categoryId = :categoryId ORDER BY id DESC")
    fun getGarmentsByCategoryPaged(categoryId: Int?): PagingSource<Int, Garment>

    @Query("SELECT * FROM Garments WHERE Garments.categoryId IS NULL ORDER BY id DESC")
    fun getUncategorizedGarmentsPaged(): PagingSource<Int, Garment>


    @Query("SELECT * FROM Garments " +
            "WHERE (Garments.categoryId NOT IN (:excludedCategoryId) OR Garments.categoryId IS NULL) " +
            "AND (Garments.color NOT IN (:excludedColor) OR Garments.color IS NULL)" +
            "AND (Garments.brand NOT IN (:excludedBrand) OR Garments.brand IS NULL)")
    fun getFilteredGarmentsPaged(excludedCategoryId: List<Int>, excludedColor: List<String>, excludedBrand: List<String>): PagingSource<Int, Garment>

    @Query("SELECT COUNT(*) FROM Garments " +
            "WHERE (Garments.categoryId NOT IN (:excludedCategoryId) OR Garments.categoryId IS NULL) " +
            "AND (Garments.color NOT IN (:excludedColor) OR Garments.color IS NULL)" +
            "AND (Garments.brand NOT IN (:excludedBrand) OR Garments.brand IS NULL)")
    fun getGarmentsCount(excludedCategoryId: List<Int>, excludedColor: List<String>, excludedBrand: List<String>): Flow<Int>
}