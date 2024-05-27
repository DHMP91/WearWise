package dhmp.wearwise.ui.screens.clothing
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.garments


@Composable
fun ClothingScreen(clothingViewModel: ClothingViewModel = viewModel()) {
    val clothingUiState by clothingViewModel.uiState.collectAsState()

    Scaffold (
        bottomBar = { WearWiseBottomAppBar() }
    ) {
        GarmentList(garments, contentPadding = it)
    }
}



@Composable
fun GarmentList(garments: List<Garment>, contentPadding: PaddingValues = PaddingValues(0.dp),){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(contentPadding)
    ) {
        items(garments.size) { index ->
            GarmentCard(garments[index])
        }
    }
}

@Composable
fun GarmentCard(garment: Garment, modifier: Modifier = Modifier){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
    ) {
        Column {
            Text(
                text = garment.name,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun WearWiseBottomAppBar() {
    BottomAppBar {

    }
}