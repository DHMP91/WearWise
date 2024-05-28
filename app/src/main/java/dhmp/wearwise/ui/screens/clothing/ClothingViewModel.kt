package dhmp.wearwise.ui.screens.clothing

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class ClothingViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(ClothingUIState())
    val uiState: StateFlow<ClothingUIState> = _uiState.asStateFlow()
    var showMenu by mutableStateOf(false)
    var showBrandFilterMenu by mutableStateOf(false)
    init {
        reset()
    }

    fun reset() {
        _uiState.value = ClothingUIState()
    }

//    fun getBrands(): List<String> {
//
//    }


}