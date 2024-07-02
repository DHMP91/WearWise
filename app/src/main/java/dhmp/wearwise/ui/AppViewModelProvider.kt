package dhmp.wearwise.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dhmp.wearwise.WearWiseApplication
import dhmp.wearwise.ui.screens.clothing.ClothingViewModel
import dhmp.wearwise.ui.screens.clothing.OutfitViewModel

object AppViewModelProvider {
    val ClothingFactory = viewModelFactory {
        initializer {
            ClothingViewModel(
                wearWiseApplication().container.garmentsRespository,
                wearWiseApplication().container.categoriesRepository,
                wearWiseApplication().container.outfitsRepository
            )
        }
    }

    val OutFitFactory = viewModelFactory {
            initializer {
                OutfitViewModel(
                    wearWiseApplication().container.garmentsRespository,
                    wearWiseApplication().container.outfitsRepository
                )
            }
        }
}

fun CreationExtras.wearWiseApplication(): WearWiseApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as WearWiseApplication)
