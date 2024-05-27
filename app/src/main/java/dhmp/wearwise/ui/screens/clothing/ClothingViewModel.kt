package dhmp.wearwise.ui.screens.clothing

import androidx.lifecycle.ViewModel
import dhmp.wearwise.model.garmentTypes
import dhmp.wearwise.ui.screens.clothing.ClothingUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class ClothingViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(ClothingUIState())
    val uiState: StateFlow<ClothingUIState> = _uiState.asStateFlow()

    init {
        reset()
    }

    fun reset() {
        _uiState.value = ClothingUIState(filterInclude = garmentTypes)
    }


}