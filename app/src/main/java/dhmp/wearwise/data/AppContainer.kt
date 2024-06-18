package dhmp.wearwise.data

import android.content.Context

interface AppContainer {
    val garmentsRespository: GarmentsRepository
    val categoriesRepository: CategoriesRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val garmentsRespository: GarmentsRepository by lazy {
        DefaultGarmentsRepository(AppDatabase.getDatabase(context).garmentDao())
    }

    override val categoriesRepository: CategoriesRepository by lazy {
        DefaultCategoryRepository(AppDatabase.getDatabase(context).categoryDao())
    }

}
