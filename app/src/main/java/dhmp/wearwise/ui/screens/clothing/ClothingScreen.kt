package dhmp.wearwise.ui.screens.clothing
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dhmp.wearwise.R
import dhmp.wearwise.model.Garment
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.common.WearWiseBottomAppBar

@Composable
fun ClothingScreen(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val clothingUiState by clothingViewModel.uiState.collectAsState()
    clothingViewModel.collectGarments() //Start the flow for garment list
    Scaffold (
        topBar = { ClothingScreenTopAppBar() }
    ) {
        GarmentList(clothingUiState.garments, contentPadding = it)
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
        Row{
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(garment.image).build(),
                contentDescription = "icon",
                contentScale = ContentScale.Inside,
                modifier = Modifier.fillMaxWidth().padding(10.dp, 10.dp, 10.dp,10.dp)
            )
        }
        Row {
            Text(
                text = garment.id.toString(),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingScreenTopAppBar(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    TopAppBar(
        title = {
            Text(stringResource(id = R.string.inventory))
        },
        actions = {
            IconButton(onClick = { clothingViewModel.showMenu = !clothingViewModel.showMenu }) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
            }
            ClothingMainMenu()
            ClothingBrandSelectionMenu()
        }
    )
}

@Composable
fun ClothingMainMenu(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory)){
    DropdownMenu(
        expanded = clothingViewModel.showMenu,
        onDismissRequest = { clothingViewModel.showMenu= false }
    ) {
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.filter_by_brand))
                }
            },
            onClick = {
                clothingViewModel.showBrandFilterMenu = true
            }
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.filter_by_category))
                }
            },
            onClick = {
                // TODO
            }
        )

    }
}


@Composable
fun ClothingBrandSelectionMenu(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory)){
    DropdownMenu(
        expanded = clothingViewModel.showMenu && clothingViewModel.showBrandFilterMenu,
        onDismissRequest = {
            clothingViewModel.showMenu= false
            clothingViewModel.showBrandFilterMenu = false
        }
    ) {
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = true,  //TODO check selection state
                        onCheckedChange = null // The click on the item handles this
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "This BRAND")
                }
            },
            onClick = { /* todo */ }
        )
    }
}