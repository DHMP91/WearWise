package dhmp.wearwise.ui.screens.statistics

import androidx.lifecycle.ViewModel
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.OutfitsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class StatsViewModel (
    private val garmentRepository: GarmentsRepository,
    private val outfitsRepository: OutfitsRepository,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO
): ViewModel() {

}