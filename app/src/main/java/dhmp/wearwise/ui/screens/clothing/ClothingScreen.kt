package dhmp.wearwise.ui.screens.clothing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dhmp.wearwise.R
import dhmp.wearwise.model.Garment
import dhmp.wearwise.ui.AppViewModelProvider

@Composable
fun ClothingScreen(
    onEdit: (Long) -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val clothingUiState by clothingViewModel.uiState.collectAsState()
    clothingViewModel.collectGarments() //Start the flow for garment list
    Scaffold (
        topBar = { ClothingScreenTopAppBar() }
    ) {
        GarmentList(clothingUiState.garments, onEdit, contentPadding = it)
    }
}



@Composable
fun GarmentList(garments: List<Garment>, onEdit: (Long) -> Unit, contentPadding: PaddingValues = PaddingValues(0.dp)){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(contentPadding)
            .background(MaterialTheme.colorScheme.background),
    ) {
        items(garments.size) { index ->
            GarmentCard(garments[index], onEdit)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarmentCard(garment: Garment, onEdit: (Long) -> Unit, modifier: Modifier = Modifier){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(15.dp, 5.dp, 15.dp, 5.dp)
            .height(120.dp),
        onClick = {
            onEdit(garment.id)
        }
    ) {
        Row (
            verticalAlignment = Alignment.Top,
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {

            Column(modifier = Modifier
                .weight(2.5f)
                .fillMaxSize()
                .padding(end = 5.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(garment.image).build(),
                    contentDescription = "GarmentImage",
                    contentScale = ContentScale.FillWidth,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ){
                Text("Brand")
                Text("Category")
                Text("Ocassion")
            }

            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ){
                Text("Category2")
                Text("Color")
                Text("Material")
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ){
                //TODO different icon for different clothing type
                Icon(imageVector = Icons.Filled.Face, contentDescription = "Clothing Type")
            }

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
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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