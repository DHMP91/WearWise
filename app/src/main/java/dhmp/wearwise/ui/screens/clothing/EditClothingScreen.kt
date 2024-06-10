package dhmp.wearwise.ui.screens.clothing

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dhmp.wearwise.ui.AppViewModelProvider


@Composable
fun EditClothingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    garmentId: Long?,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory),
    context: Context = LocalContext.current
){
    if(garmentId == null){
        onFinish()
    }else {
        clothingViewModel.getGarmentById(garmentId)
    }
    val uiState by clothingViewModel.uiEditState.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
    ) {
        Row{
            AsyncImage(
                model = ImageRequest.Builder(context).data(uiState.editGarment.image).build(),
                contentDescription = "icon",
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 10.dp, 10.dp, 10.dp)
            )
        }
        Row {
            Column {
                RemoveBackground(garmentId!!, uiState.editGarment.image)
                DeleteGarment(onFinish, garmentId!!)
            }
            Column {

            }
        }
    }
}

@Composable
fun RemoveBackground(garmentId: Long, image: String?, clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val isProcessing by clothingViewModel.isProcessingBackground.collectAsState()
    if(!isProcessing){
        Button(
            onClick = {
                image.let{
                    Uri.parse(it).path?.let { p -> clothingViewModel.removeBackGround(garmentId, p) }
                }
            }
        ) {
            Text(
                text = "Blurr",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }else {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}


@Composable
fun DeleteGarment(onFinish: ()-> Unit, garmentId: Long, clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val isProcessing by clothingViewModel.isProcessingBackground.collectAsState()
    if(!isProcessing){
        Button(
            onClick = {
                clothingViewModel.deleteGarment(garmentId)
                onFinish()
            }
        ) {
            Text(
                text = "Delete",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun ClothingInfo(){
    Text(
        text = "TODO add info",
        style = MaterialTheme.typography.headlineSmall
    )
}