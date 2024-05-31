package dhmp.wearwise.ui.screens.clothing

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.model.Garment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClothingViewModel(private val garmentRepository: GarmentsRepository): ViewModel() {

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

    /**
     * This function is used to get all the garments from the database
     * - viewModelScope.launch is used to launch a coroutine within the viewModel lifecycle.
     * - repository.getAllGarmentsStream() is used to get all the garments from the database.
     * - flowOn(Dispatchers.IO) is used to change the dispatcher of the flow to IO, which is optimal for IO operations, and does not block the main thread.
     * - each time the flow emits a new value, the collect function will be called with the list of books.
     */
    fun collectGarments() {
        viewModelScope.launch {
            garmentRepository.getAllGarmentsStream().flowOn(Dispatchers.IO).collect { c ->
                _uiState.update { currentState ->
                    currentState.copy(
                        garments = c
                    )
                }
            }
            garmentRepository.insertGarment(Garment())
        }
    }


}