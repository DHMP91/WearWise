package dhmp.wearwise.model

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface UserConfigDao {
    @Update
    suspend fun update(item: UserConfig)

    @Query("SELECT * from UserConfig WHERE id = 1")
    fun getUserConfig(): Flow<UserConfig>
}