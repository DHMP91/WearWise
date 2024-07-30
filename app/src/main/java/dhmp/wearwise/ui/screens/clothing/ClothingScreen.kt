package dhmp.wearwise.ui.screens.clothing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
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
import dhmp.wearwise.model.Categories
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.common.ScreenTitle
import dhmp.wearwise.ui.screens.common.categoryIcon
import java.util.Locale

private val TAG = "ClothingScreen"

@Composable
fun ClothingScreen(
    onEdit: (Long) -> Unit,
    onNewClothing: () -> Unit,
    onOutfits: (Long) -> Unit,
) {
    Surface {
        Box(modifier = Modifier.fillMaxSize()){
            GarmentList(onEdit, onOutfits)
            FloatingActionButton(
                onClick = { onNewClothing() },
                containerColor = colorResource(R.color.accent),
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
    val menuState by clothingViewModel.uiMenuState.collectAsState()
    val garmentCount by clothingViewModel.getGarmentsCount(
        menuState.filterExcludeCategories,
        menuState.filterExcludeColors,
        menuState.filterExcludeBrands
    ).collectAsState(initial = 0)

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
            ScreenTitle("Clothing ($garmentCount)")
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu",
                modifier = Modifier.clickable  { clothingViewModel.showMenu(!menuState.showMenu) }
            )
            ClothingMainMenu()
            ClothingBrandSelectionMenu()
            ClothingCategorySelectionMenu()
            ClothingColorSelectionMenu()
        }
    }

}

@Composable
fun GarmentList(
    onEdit: (Long) -> Unit,
    onOutfits: (Long) -> Unit,
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
                GarmentCard(it, onEdit, onOutfits)
            }
        }
    }
}

@Composable
fun GarmentCard(
    garment: Garment,
    onEdit: (Long) -> Unit,
    onOutfits: (Long) -> Unit,
    modifier: Modifier = Modifier,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
){
    val thumbnail by clothingViewModel.getGarmentThumbnail(garment).collectAsState(initial = "")
    Row (
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .heightIn(max = 130.dp)
            .fillMaxWidth()
            .clickable { onEdit(garment.id) }
            .padding(10.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(thumbnail).build(),
            contentDescription = "GarmentImage",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .weight(2f)
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
        )

        Column (
            modifier = Modifier
                .weight(3f)
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Top,
        ){

            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
//            Text(text="${garment.id}")

                Text(
                    garment.brand?.lowercase()?.replaceFirstChar {
                        it.titlecase(Locale.getDefault())
                    } ?: "Set Brand",
                    color = if(garment.brand != null)  MaterialTheme.colorScheme.onBackground else Color.Gray
                )
                Text(
                    "#${garment.id}",
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    modifier = Modifier
                        .padding(top = 5.dp, bottom = 5.dp)
                )
                Text(
                    garment.occasion?.name?.lowercase()?.replaceFirstChar {
                        it.titlecase(Locale.getDefault())
                    } ?: "Set Occasion",
                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                    color = if(garment.occasion != null)  MaterialTheme.colorScheme.onBackground else Color.Gray
                )
            }


            Row(
                modifier = Modifier
                    .weight(0.75f)
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                val color = GarmentColorNames.find { it.name == garment.color}
                color?.let {
                    Box(
                        modifier = Modifier
                            .size(20.dp)  // Adjust the size as needed
                            .background(Color(color.color))
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.onBackground)
                    )
                }

                val categories = Categories
                val icon = categoryIcon(garment, categories)
                Icon(
                    painter = painterResource(icon),
                    contentDescription = "Clothing Type",
                    modifier = Modifier
                        .sizeIn(maxHeight = dimensionResource(R.dimen.icon_max_height))
                        .padding(start = 5.dp, end = 5.dp)
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outfit),
                        contentDescription = "Outfit Count",
                        modifier = Modifier
                            .sizeIn(maxHeight = dimensionResource(R.dimen.icon_max_height))
                            .clickable {
                                onOutfits(garment.id)
                            }
                    )
                    Text(
                        "${garment.outfitsId.size}"
                    )

                }
            }
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


    }
}

@Composable
fun ClothingMainMenu(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)){
    val menuState by clothingViewModel.uiMenuState.collectAsState()
    DropdownMenu(
        expanded = menuState.showMenu,
        onDismissRequest = { clothingViewModel.showMenu(false) }
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
                clothingViewModel.showBrandFilterMenu(true)
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
                clothingViewModel.showCategoryFilterMenu(true)
            }
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.filter_by_color))
                }
            },
            onClick = {
                clothingViewModel.showColorFilterMenu(true)
            }
        )
    }
}


@Composable
fun ClothingBrandSelectionMenu(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)){
    LaunchedEffect(null) {
        clothingViewModel.collectBrands()
    }
    val menuState by clothingViewModel.uiMenuState.collectAsState()
    val brands by clothingViewModel.brands.collectAsState()

    DropdownMenu(
        expanded = menuState.showMenu && menuState.showBrandFilterMenu,
        onDismissRequest = {
            clothingViewModel.showMenu(false)
            clothingViewModel.showBrandFilterMenu(false)
        }
    ) {
        DropdownMenuItem(
            text = {
                Icon(Icons.Filled.ArrowBack, "Exit Menu")
            },
            onClick = {
                clothingViewModel.showBrandFilterMenu(false)
            }
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Check All")
                }
            },
            onClick = {
                for(brand in brands){
                    if(menuState.filterExcludeBrands.contains(brand)) {
                        clothingViewModel.removeBrandFromFilter(brand)
                    }
                }
            }
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Uncheck All")
                }
            },
            onClick = {
                for(brand in brands){
                    if(!menuState.filterExcludeBrands.contains(brand)) {
                        clothingViewModel.addBrandToFilter(brand)
                    }
                }
            }
        )
        for(brand in brands) {
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = !menuState.filterExcludeBrands.contains(brand),  //TODO check selection state
                            onCheckedChange = {
                                if(!menuState.filterExcludeBrands.contains(brand)){
                                    clothingViewModel.addBrandToFilter(brand)
                                }else{
                                    clothingViewModel.removeBrandFromFilter(brand)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = brand)
                    }
                },
                onClick = {
                    if(!menuState.filterExcludeBrands.contains(brand)){
                        clothingViewModel.addBrandToFilter(brand)
                    }else{
                        clothingViewModel.removeBrandFromFilter(brand)
                    }
                }
            )
        }
    }
}


@Composable
fun ClothingColorSelectionMenu(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)){
    val menuState by clothingViewModel.uiMenuState.collectAsState()
    val colorNames = GarmentColorNames.map { it.name }

    DropdownMenu(
        expanded = menuState.showMenu && menuState.showColorFilterMenu,
        onDismissRequest = {
            clothingViewModel.showMenu(false)
            clothingViewModel.showColorFilterMenu(false)
        }
    ) {
        DropdownMenuItem(
            text = {
                Icon(Icons.Filled.ArrowBack, "Exit Menu")
            },
            onClick = {
                clothingViewModel.showColorFilterMenu(false)
            }
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Check All")
                }
            },
            onClick = {
                for(c in colorNames){
                    if(menuState.filterExcludeColors.contains(c)) {
                        clothingViewModel.removeColorFromFilter(c)
                    }
                }
            }
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Uncheck All")
                }
            },
            onClick = {
                for(c in colorNames){
                    if(!menuState.filterExcludeColors.contains(c)) {
                        clothingViewModel.addColorToFilter(c)
                    }
                }
            }
        )
        for(color in colorNames) {
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = !menuState.filterExcludeColors.contains(color),  //TODO check selection state
                            onCheckedChange = {
                                if(!menuState.filterExcludeColors.contains(color)){
                                    clothingViewModel.addColorToFilter(color)
                                }else{
                                    clothingViewModel.removeColorFromFilter(color)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = color)
                    }
                },
                onClick = {
                    if(!menuState.filterExcludeBrands.contains(color)){
                        clothingViewModel.addColorToFilter(color)
                    }else{
                        clothingViewModel.removeColorFromFilter(color)
                    }
                }
            )
        }
    }
}



@Composable
fun ClothingCategorySelectionMenu(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)){
    val menuState by clothingViewModel.uiMenuState.collectAsState()
    val categories = Category.categories()

    DropdownMenu(
        expanded = menuState.showMenu && menuState.showCategoryFilterMenu,
        onDismissRequest = {
            clothingViewModel.showMenu(false)
            clothingViewModel.showCategoryFilterMenu(false)
        }
    ) {
        DropdownMenuItem(
            text = {
                Icon(Icons.Filled.ArrowBack, "Exit Menu")
            },
            onClick = {
                clothingViewModel.showCategoryFilterMenu(false)
            }
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Check All")
                }
            },
            onClick = {
                for(c in categories){
                    if(menuState.filterExcludeCategories.contains(c)) {
                        clothingViewModel.removeCategoryFromFilter(c)
                    }
                }
            }
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Uncheck All")
                }
            },
            onClick = {
                for(c in categories){
                    if(!menuState.filterExcludeCategories.contains(c)) {
                        clothingViewModel.addCategoryToFilter(c)
                    }
                }
            }
        )
        for(c in categories) {
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = !menuState.filterExcludeCategories.contains(c),  //TODO check selection state
                            onCheckedChange = {
                                if(!menuState.filterExcludeCategories.contains(c)){
                                    clothingViewModel.addCategoryToFilter(c)
                                }else{
                                    clothingViewModel.removeCategoryFromFilter(c)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = c.name)
                    }
                },
                onClick = {
                    if(!menuState.filterExcludeCategories.contains(c)){
                        clothingViewModel.addCategoryToFilter(c)
                    }else{
                        clothingViewModel.removeCategoryFromFilter(c)
                    }
                }
            )
        }
    }
}