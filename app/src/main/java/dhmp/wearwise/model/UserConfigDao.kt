package dhmp.wearwise.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface UserConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: UserConfig)

    @Query("SELECT * from UserConfig WHERE id = 1")
    fun getUserConfig(): Flow<UserConfig?>
}