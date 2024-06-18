package dhmp.wearwise.data

import dhmp.wearwise.model.Category
import dhmp.wearwise.model.CategoryDao
import kotlinx.coroutines.flow.Flow


interface CategoriesRepository {
    /**
     * Retrieve all the items from the the given data source.
     */
    fun getAllCategoriesStream(): Flow<List<Category>>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    fun getCategoryStream(id: Long): Flow<Category?>

    /**
     * Insert item in the data source
     */
    suspend fun insertCategory(item: Category): Long

    /**
     * Delete item from the data source
     */
    suspend fun deleteCategory(item: Category)

    /**
     * Update item in the data source
     */
    suspend fun updateCategory(item: Category)
}


class DefaultCategoryRepository(private val itemDao: CategoryDao) : CategoriesRepository {
    private val tag: String = "Default Category Repository"
    override fun getAllCategoriesStream(): Flow<List<Category>> = itemDao.getAllCategories()

    override fun getCategoryStream(id: Long): Flow<Category?> = itemDao.getCategory(id)

    override suspend fun insertCategory(item: Category): Long {
        return itemDao.insert(item)
    }

    override suspend fun deleteCategory(item: Category) = itemDao.delete(item)

    override suspend fun updateCategory(item: Category) = itemDao.update(item)
}
