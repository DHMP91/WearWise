package dhmp.wearwise.ui.screens.clothing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dhmp.wearwise.R
import dhmp.wearwise.model.Garment
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.common.categoryIcon
import java.util.Locale

private val TAG = "ClothingScreen"

@Composable
fun ClothingScreen(
    onEdit: (Long) -> Unit,
    onNewClothing: () -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
) {
    LaunchedEffect(null){
        clothingViewModel.collectCategories()
    }
    Surface {
        Box(modifier = Modifier.fillMaxSize()){
            GarmentList(onEdit)
            FloatingActionButton(
                onClick = { onNewClothing() },
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Align to bottom end of the Box
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add Garment")
            }
        }
    }
}

@Composable
fun Header(
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
){
    val tabColor = MaterialTheme.colorScheme.onBackground
    val bottomBorderModifier =
        Modifier
            .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = tabColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
            }
            .padding(bottom = 10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.screen_title_padding))
    ){
        Column(
            modifier = Modifier
                .weight(2f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Clothings",
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                maxLines = 1,
                modifier = bottomBorderModifier
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier.clickable  { clothingViewModel.showMenu = !clothingViewModel.showMenu }
            )
            ClothingMainMenu()
            ClothingBrandSelectionMenu()
        }
    }

}

@Composable
fun GarmentList(
    onEdit: (Long) -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
){
    val garments: LazyPagingItems<Garment> = clothingViewModel.garments.collectAsLazyPagingItems()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.background),
    ) {
        item{
            Header()
        }
        items(garments.itemCount) { index ->
            garments[index]?.let {
                GarmentCard(it, onEdit)
            }
        }
    }
}

@Composable
fun GarmentCard(
    garment: Garment, onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
){
    Row (
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .heightIn(max = 120.dp)
            .fillMaxWidth()
            .clickable { onEdit(garment.id) }
            .padding(10.dp)
    ) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(garment.image).build(),
            contentDescription = "GarmentImage",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .weight(2f)
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
        )

        Column(
            modifier = Modifier
                .weight(3f)
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ){
//            Text(text="${garment.id}")
            Text(garment.brand ?: "Set Brand")
            Text(
                garment.occasion?.name?.lowercase()?.replaceFirstChar {
                    it.titlecase(Locale.getDefault())
                }
                ?: "Set Occasion"
            )
        }

//        Column(
//            modifier = Modifier
//                .weight(1.5f)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.Center
//        ){
//            Text("Category2")
//            Text("Color")
//            Text("Material")
//        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(start = 10.dp, top = 5.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ){
            val categories by clothingViewModel.categories.collectAsState()
            val icon = categoryIcon(garment, categories)
            Icon(
                painter = painterResource(icon),
                contentDescription = "Clothing Type",
                modifier = Modifier
                    .sizeIn(maxHeight = dimensionResource(R.dimen.icon_max_height))
            )
        }
    }
}

@Composable
fun ClothingMainMenu(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)){
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
fun ClothingBrandSelectionMenu(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)){
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