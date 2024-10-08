package dhmp.wearwise.data

import androidx.paging.PagingSource
import dhmp.wearwise.model.Outfit
import dhmp.wearwise.model.OutfitDao
import dhmp.wearwise.model.Season
import dhmp.wearwise.model.SeasonCount
import kotlinx.coroutines.flow.Flow


interface OutfitsRepository {
    /**
     * Paginated method to get all the items from the the given data source.
     */
    fun getAllOutfitsPaged(): PagingSource<Int, Outfit>

    /**
     * Paginated method to get all outfits from given list of outfit ids
     */
    fun getOutfitsByListOfIdsPaged(ids: List<Long>): PagingSource<Int, Outfit>

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

    suspend fun getOutfitThumbnail(outfit: Outfit): String?

    fun getOutfitsCount(excludedSeasons: List<Season> = listOf()): Flow<Int>

    fun getSeasonCount(): Flow<List<SeasonCount>>
}


class DefaultOutfitRepository(private val itemDao: OutfitDao) : OutfitsRepository {
    private val tag: String = "Default Outfit Repository"

    override fun getAllOutfitsPaged(): PagingSource<Int, Outfit> = itemDao.getAllOutfitsPaged()

    override fun getOutfitsByListOfIdsPaged(ids: List<Long>): PagingSource<Int, Outfit> = itemDao.getOutfitsByListOfIdsPaged(ids)

    override fun getAllOutfitsStream(): Flow<List<Outfit>> = itemDao.getAllOutfits()

    override fun getOutfitStream(id: Long): Flow<Outfit?> = itemDao.getOutfit(id)

    override suspend fun insertOutfit(item: Outfit): Long {
        return itemDao.insert(item)
    }

    override suspend fun deleteOutfit(item: Outfit) = itemDao.delete(item)

    override suspend fun updateOutfit(item: Outfit) = itemDao.update(item)

    override suspend fun getOutfitThumbnail(outfit: Outfit): String? {
        outfit.image?.let {
            return getThumbnail(it)
        }
        return null
    }

    override fun getOutfitsCount(excludedSeasons: List<Season>): Flow<Int> {
        return itemDao.getOutfitsCount(excludedSeasons)
    }

    override fun getSeasonCount(): Flow<List<SeasonCount>> {
        return itemDao.getSeasonCount()
    }

}
