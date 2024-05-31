package dhmp.wearwise.data

import android.content.Context

interface AppContainer {
    val garmentsRespository: GarmentsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val garmentsRespository: GarmentsRepository by lazy {
        DefaultGarmentsRepository(AppDatabase.getDatabase(context).garmentDao())
    }

}
