package dhmp.wearwise.data

import android.content.Context

interface AppContainer {
    val garmentsRespository: GarmentsRepository
    val outfitsRepository: OutfitsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val garmentsRespository: GarmentsRepository by lazy {
        DefaultGarmentsRepository(AppDatabase.getDatabase(context).garmentDao())
    }

    override val outfitsRepository: OutfitsRepository by lazy {
        DefaultOutfitRepository(AppDatabase.getDatabase(context).outfitDao())
    }

}
