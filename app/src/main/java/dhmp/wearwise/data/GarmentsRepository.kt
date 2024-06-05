package dhmp.wearwise.data

import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.GarmentDao
import kotlinx.coroutines.flow.Flow


interface GarmentsRepository {
    /**
     * Retrieve all the items from the the given data source.
     */
    fun getAllGarmentsStream(): Flow<List<Garment>>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    fun getGarmentStream(id: Int): Flow<Garment?>

    /**
     * Insert item in the data source
     */
    suspend fun insertGarment(item: Garment): Long

    /**
     * Delete item from the data source
     */
    suspend fun deleteGarment(item: Garment)

    /**
     * Update item in the data source
     */
    suspend fun updateGarment(item: Garment)
}

class DefaultGarmentsRepository(private val itemDao: GarmentDao) : GarmentsRepository {
    override fun getAllGarmentsStream(): Flow<List<Garment>> = itemDao.getAllGarments()

    override fun getGarmentStream(id: Int): Flow<Garment?> = itemDao.getGarment(id)

    override suspend fun insertGarment(item: Garment): Long {
        return itemDao.insert(item)
    }

    override suspend fun deleteGarment(item: Garment) = itemDao.delete(item)

    override suspend fun updateGarment(item: Garment) = itemDao.update(item)
}
