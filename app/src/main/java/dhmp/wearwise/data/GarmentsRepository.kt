package dhmp.wearwise.data

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.paging.PagingSource
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.GarmentDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File


interface GarmentsRepository {
    /**
     * Paginated method to get all the items from the the given data source.
     */
    fun getAllGarmentsPaged(): PagingSource<Int, Garment>
    /**
     * Paginated method to get all the items of type category.
     */
    fun getGarmentsByCategoryPaged(categoryId: Int?): PagingSource<Int, Garment>

    /**
     * Paginated method to get the items that isn't in the excluded filter.
     */
    fun getFilteredGarments(excludedCategories: List<Category> = listOf(), excludedColors: List<String> = listOf(), excludedBrands: List<String> = listOf()): PagingSource<Int, Garment>

    /**
     * Retrieve all the items from the the given data source.
     */
    fun getAllGarmentsStream(): Flow<List<Garment>>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    fun getGarmentStream(id: Long): Flow<Garment?>

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

    suspend fun getBrands(): Flow<List<String>>

    suspend fun saveImageToStorage(appDir: File, image: Bitmap) : Uri

    suspend fun replaceImageInStorage(file: File, image: Bitmap)

    suspend fun getGarmentThumbnail(garment: Garment): String?

    fun getGarmentsCount(excludedCategories: List<Category>, excludedColors: List<String>, excludedBrands: List<String>): Flow<Int>
}

class DefaultGarmentsRepository(private val itemDao: GarmentDao) : GarmentsRepository {
    private val tag: String = "Default Garments Repository"

    override fun getAllGarmentsPaged() = itemDao.getAllGarmentsPaged()

    override fun getGarmentsByCategoryPaged(categoryId: Int?): PagingSource<Int, Garment>{
        return if (categoryId != null)
            itemDao.getGarmentsByCategoryPaged(categoryId)
        else
            itemDao.getUncategorizedGarmentsPaged()
    }

    override fun getFilteredGarments(excludedCategories: List<Category>, excludedColors: List<String>, excludedBrands: List<String>): PagingSource<Int, Garment>{
        val excludedCategoryIds = excludedCategories.map { it.id }
        return itemDao.getFilteredGarmentsPaged(excludedCategoryIds, excludedColors, excludedBrands)
    }

    override fun getAllGarmentsStream(): Flow<List<Garment>> = itemDao.getAllGarments()

    override fun getGarmentStream(id: Long): Flow<Garment?> = itemDao.getGarment(id)

    override suspend fun insertGarment(item: Garment): Long {
        return itemDao.insert(item)
    }

    override suspend fun deleteGarment(item: Garment) = itemDao.delete(item)

    override suspend fun updateGarment(item: Garment) = itemDao.update(item)

    override suspend fun saveImageToStorage(appDir: File, image: Bitmap): Uri {
        val now = System.currentTimeMillis()
        val newFile = File(appDir, "GarmentImages").let {
            it.mkdirs()
            File(it, "Garment_$now.png")
        }
        withContext(Dispatchers.IO) {
            if (!newFile.createNewFile()) {
                Log.e(tag, "Error creating new file to store image")
            } else {
                val outputStream = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                newFile.writeBytes(outputStream.toByteArray())
                outputStream.close()

            }
        }
        return newFile.toUri()
    }

    override suspend fun replaceImageInStorage(file: File, image: Bitmap) {
        withContext(Dispatchers.IO) {
            val outputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            file.writeBytes(outputStream.toByteArray())
            outputStream.close()
        }
    }

    override suspend fun getBrands() = itemDao.getBrands()

    override suspend fun getGarmentThumbnail(garment: Garment): String? {
        garment.image?.let {
            return getThumbnail(it)
        }
        return null
    }

    override fun getGarmentsCount(excludedCategories: List<Category>, excludedColors: List<String>, excludedBrands: List<String>): Flow<Int> {
        val excludedCategoryIds = excludedCategories.map { it.id }
        return itemDao.getGarmentsCount(excludedCategoryIds, excludedColors, excludedBrands)
    }
}
