package dhmp.wearwise.ui.screens.clothing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.OutfitsRepository
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.Outfit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private const val ITEMS_PER_PAGE = 3

class OutfitViewModel (
    private val garmentRepository: GarmentsRepository,
    private val outfitsRepository: OutfitsRepository,
): ViewModel() {

    val outfits: Flow<PagingData<Outfit>> =
        Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
            pagingSourceFactory = { outfitsRepository.getAllOutfitsPaged() }
        )
            .flow
            .cachedIn(viewModelScope)

    fun getGarments(outfit: Outfit): Flow<List<Garment>> = flow {
        val garments = mutableListOf<Garment>()
        outfit.garmentsId.forEach { id ->
            val garment = garmentRepository.getGarmentStream(id).flowOn(Dispatchers.IO).first()
            garment?.let {
                garments.add(it)
                emit(garments.toList()) // Emit a new copy of the list each time an item is added
            }
        }
    }.flowOn(Dispatchers.IO)
}