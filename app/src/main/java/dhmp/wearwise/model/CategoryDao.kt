package dhmp.wearwise.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Category): Long

    @Update
    suspend fun update(item: Category)

    @Delete
    suspend fun delete(item: Category)

    @Query("SELECT * from Categories WHERE id = :id")
    fun getCategory(id: Long): Flow<Category>

    @Query("SELECT * from Categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>
}