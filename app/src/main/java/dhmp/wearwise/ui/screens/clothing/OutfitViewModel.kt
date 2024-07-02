package dhmp.wearwise.ui.screens.clothing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.OutfitsRepository
import dhmp.wearwise.model.Garment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class OutfitViewModel (
    private val garmentRepository: GarmentsRepository,
    private val outfitsRepository: OutfitsRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow(OutfitUIState())
    val uiState: StateFlow<OutfitUIState> = _uiState.asStateFlow()

    fun getOutfitItems(garmentIds: List<Long>){
        val garments = mutableListOf<Garment>()
        for(id in garmentIds) {
            viewModelScope.launch {
                garmentRepository.getGarmentStream(id).flowOn(Dispatchers.IO).collect { garment ->
                    garment?.let {
                        garments.add(it)
                    }
                }
            }
        }
    }
}