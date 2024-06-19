package dhmp.wearwise.data

import dhmp.wearwise.model.Outfit
import dhmp.wearwise.model.OutfitDao
import kotlinx.coroutines.flow.Flow


interface OutfitsRepository {
    /**
     * Retrieve all the items from the the given data source.
     */
    fun getAllOutfitsStream(): Flow<List<Outfit>>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    fun getOutfitStream(id: Long): Flow<Outfit?>

    /**
     * Insert item in the data source
     */
    suspend fun insertOutfit(item: Outfit): Long

    /**
     * Delete item from the data source
     */
    suspend fun deleteOutfit(item: Outfit)

    /**
     * Update item in the data source
     */
    suspend fun updateOutfit(item: Outfit)
}


class DefaultOutfitRepository(private val itemDao: OutfitDao) : OutfitsRepository {
    private val tag: String = "Default Outfit Repository"
    override fun getAllOutfitsStream(): Flow<List<Outfit>> = itemDao.getAllOutfits()

    override fun getOutfitStream(id: Long): Flow<Outfit?> = itemDao.getOutfit(id)

    override suspend fun insertOutfit(item: Outfit): Long {
        return itemDao.insert(item)
    }

    override suspend fun deleteOutfit(item: Outfit) = itemDao.delete(item)

    override suspend fun updateOutfit(item: Outfit) = itemDao.update(item)
}
