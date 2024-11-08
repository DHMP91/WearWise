package dhmp.wearwise.data

import dhmp.wearwise.model.UserConfig
import dhmp.wearwise.model.UserConfigDao
import kotlinx.coroutines.flow.Flow


interface UserConfigRepository {

    /**
     * Retrieve all the items from the the given data source.
     */
    fun getUserConfigStream(): Flow<UserConfig>

    suspend fun updateUserConfig(userConfig: UserConfig)
}


class DefaultUserConfigRepository(private val itemDao: UserConfigDao) : UserConfigRepository {
    override fun getUserConfigStream(): Flow<UserConfig> = itemDao.getUserConfig()

    override suspend fun updateUserConfig(userConfig: UserConfig) {
        val enforcedConfig = userConfig.copy(id = 1)
        itemDao.update(enforcedConfig)
    }
}