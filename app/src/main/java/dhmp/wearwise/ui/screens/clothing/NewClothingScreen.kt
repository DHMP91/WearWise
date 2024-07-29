package dhmp.wearwise.ui.screens.clothing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.common.ImageScreen

private val TAG = "NewClothingScreen"
@Composable
fun NewClothingScreen(
    onFinish: (Long) -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
) {
    val uiState by clothingViewModel.uiState.collectAsState()
    if (uiState.newItemId != 0L ){
        onFinish(uiState.newItemId)
    }
    Surface {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ){
            ImageScreen(clothingViewModel::saveImage)
        }
    }
}
