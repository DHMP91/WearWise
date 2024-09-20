package dhmp.wearwise.ui.screens.clothing
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
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
import dhmp.wearwise.ui.screens.common.TestTag
import dhmp.wearwise.ui.screens.common.categoryIcon
import java.util.Locale

private val TAG = "ClothingScreen"
enum class Filter {
    BRAND,
    COLOR,
    CATEGORY,
}

@Composable
fun ClothingScreen(
    onEdit: (Long) -> Unit,
    onNewClothing: () -> Unit,
    onOutfits: (Long) -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
) {
    LaunchedEffect(null) {
        clothingViewModel.collectBrands()
    }
    val garments: LazyPagingItems<Garment> = clothingViewModel.garments.collectAsLazyPagingItems()
    val menuState by clothingViewModel.uiMenuState.collectAsState()
    val garmentCount by clothingViewModel.getGarmentsCount(
        menuState.filterExcludeCategories,
        menuState.filterExcludeColors,
        menuState.filterExcludeBrands
    ).collectAsState(initial = 0)
    val brands by clothingViewModel.brands.collectAsState()

    val filterAddFunc = { item: Any, type: Filter ->
        when(type) {
            Filter.BRAND -> clothingViewModel.addBrandToFilter(item as String)
            Filter.COLOR -> clothingViewModel.addColorToFilter(item as String)
            Filter.CATEGORY -> clothingViewModel.addCategoryToFilter(item as Category)
        }
    }

    val filterRemoveFunc = { item: Any, type: Filter ->
        when(type) {
            Filter.BRAND -> clothingViewModel.removeBrandFromFilter(item as String)
            Filter.COLOR -> clothingViewModel.removeColorFromFilter(item as String)
            Filter.CATEGORY -> clothingViewModel.removeCategoryFromFilter(item as Category)
        }
    }

    val filterAddAllFunc = { item: Any, type: Filter ->
        when(type) {
            Filter.BRAND -> clothingViewModel.addBrandToFilter(item as List<String>)
            Filter.COLOR -> clothingViewModel.addColorToFilter(item as List<String>)
            Filter.CATEGORY-> clothingViewModel.addCategoryToFilter(item as List<Category>)
        }
    }

    val filterRemoveAllFunc = { item: Any, type: Filter ->
        when(type) {
            Filter.BRAND -> clothingViewModel.removeBrandFromFilter(item as List<String>)
            Filter.COLOR -> clothingViewModel.removeColorFromFilter(item as List<String>)
            Filter.CATEGORY -> clothingViewModel.removeCategoryFromFilter(item as List<Category>)
        }
    }

    Surface (
        color = MaterialTheme.colorScheme.background
    ){
        Box(modifier = Modifier.fillMaxSize()){

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TestTag.CLOTHING_LIST)
            ) {
                item {
                    Header(
                        menuState = menuState,
                        garmentCount,
                        onShowMenu = { clothingViewModel.showMenu(!menuState.showMenu) }
                    )
                    TopBarSlideOutMenu(
                        menuState = menuState,
                        brands = brands,
                        filterAddFunc,
                        filterRemoveFunc,
                        filterAddAllFunc,
                        filterRemoveAllFunc,
                    )
                }
                items(garments.itemCount) { index ->
                    garments[index]?.let {
                        val thumbnail by clothingViewModel.getGarmentThumbnail(it).collectAsState(initial = "")
                        GarmentCard(
                            it,
                            thumbnail,
                            onEdit,
                            onOutfits,
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { onNewClothing() },
                containerColor = colorResource(R.color.accent),
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Align to bottom end of the Box
                    .padding(16.dp)
                    .testTag(TestTag.NEW_CLOTHING_BUTTON)
            ) {
                Icon(Icons.Filled.Add, "Add Garment")
            }
        }
    }
}

@Composable
fun Header(
    menuState: ClothingMenuUIState,
    garmentCount: Int,
    onShowMenu: () -> Unit
){
    var rowModifier = Modifier.fillMaxWidth()
    rowModifier = if(!menuState.showMenu){
        rowModifier
            .padding(dimensionResource(id = R.dimen.screen_title_padding))
    }else{
        rowModifier
            .padding(
                top = dimensionResource(id = R.dimen.screen_title_padding),
                start = dimensionResource(id = R.dimen.screen_title_padding),
                end = dimensionResource(id = R.dimen.screen_title_padding),
            )
            .background(MaterialTheme.colorScheme.background)
    }

    Row(
        modifier = rowModifier
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
                modifier = Modifier
                    .clickable { onShowMenu() }
                    .testTag(TestTag.MAIN_MENU)
            )
        }
    }

}

@Composable
fun GarmentCard(
    garment: Garment,
    thumbnail: String,
    onEdit: (Long) -> Unit,
    onOutfits: (Long) -> Unit,
    modifier: Modifier = Modifier,
){
    Card(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(
            modifier = modifier
                .heightIn(max = dimensionResource(id = R.dimen.garment_item_height))
                .fillMaxWidth()
                .clickable { onEdit(garment.id) }
                .testTag(TestTag.CLOTHING_ITEM)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(thumbnail).build(),
                contentDescription = "GarmentImage",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Top,
            ) {

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
                        color = if (garment.brand != null) MaterialTheme.colorScheme.onBackground else Color.Gray,
                        modifier = Modifier.testTag(TestTag.CLOTHING_BRAND_CARD_FIELD)
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
                        color = if (garment.occasion != null) MaterialTheme.colorScheme.onBackground else Color.Gray
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
                    val color = GarmentColorNames.find { it.name == garment.color }
                    color?.let {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(color.color))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                        )
                    }

                    val categories = Categories
                    val icon = categoryIcon(garment, categories)
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = "Clothing Type",
                        modifier = Modifier
                            .sizeIn(maxHeight = dimensionResource(R.dimen.clothing_list_icon_height))
                            .padding(start = 5.dp, end = 5.dp)
                            .testTag("${TestTag.CLOTHING_LIST_CATEGORY_PREFIX}${icon}")
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.testTag(TestTag.OUTFIT_COUNT)
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
}


@Composable
fun TopBarSlideOutMenu(
    menuState: ClothingMenuUIState,
    brands: List<String>,
    filterAddFunc: (Any, Filter) -> Unit,
    filterRemoveFunc: (Any, Filter) -> Unit,
    filterAddAllFunc: (List<Any>, Filter) -> Unit,
    filterRemoveAllFunc: (List<Any>, Filter) -> Unit,
){
    val density = LocalDensity.current
    AnimatedVisibility(
        menuState.showMenu,
        enter = slideInVertically {
                // Slide in from 40 dp from the top.
                with(density) { -40.dp.roundToPx() }
            } + expandVertically(
                // Expand from the top.
                expandFrom = Alignment.Top
            ) + fadeIn(
                // Fade in with the initial alpha of 0.3f.
                initialAlpha = 0.3f
            ),
        exit = slideOutVertically{
                with(density) { 40.dp.roundToPx() }
            } + shrinkVertically(

            ) + fadeOut( targetAlpha = 0.0f)
    ){

        Column {
            FilterRow(
                title = stringResource(R.string.filter_by_brand),
                items = brands,
                filterExclude = { menuState.filterExcludeBrands },
                addFunc = { item -> filterAddFunc(item, Filter.BRAND) },
                removeFunc = { item -> filterRemoveFunc(item, Filter.BRAND) },
                addAllFunc = { items -> filterAddAllFunc(items, Filter.BRAND) },
                removeAllFunc = { items -> filterRemoveAllFunc(items, Filter.BRAND) }
            )

            val colorNames = GarmentColorNames.map { it.name }
            FilterRow(
                title = stringResource(R.string.filter_by_color),
                items = colorNames,
                filterExclude = { menuState.filterExcludeColors },
                addFunc = { item -> filterAddFunc(item, Filter.COLOR) },
                removeFunc = { item -> filterRemoveFunc(item, Filter.COLOR) },
                addAllFunc = { items -> filterAddAllFunc(items, Filter.COLOR) },
                removeAllFunc = { items -> filterRemoveAllFunc(items, Filter.COLOR) }
            )

            val categories = Category.categories()
            FilterRow(
                title = stringResource(R.string.filter_by_category),
                items = categories,
                filterExclude = { menuState.filterExcludeCategories },
                addFunc = { item -> filterAddFunc(item, Filter.CATEGORY) },
                removeFunc = { item -> filterRemoveFunc(item, Filter.CATEGORY) },
                addAllFunc = { items -> filterAddAllFunc(items, Filter.CATEGORY) },
                removeAllFunc = { items -> filterRemoveAllFunc(items, Filter.CATEGORY) }
            )
        }
    }
}

@Composable
fun <T> FilterRow(
    title: String,
    items: List<T>,
    filterExclude: () -> List<T>,
    addFunc: (T) -> Unit,
    removeFunc: (T) -> Unit,
    addAllFunc: (List<T>) -> Unit,
    removeAllFunc: (List<T>) -> Unit,
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White)
    ){
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 20.dp, top = 5.dp, end = 20.dp, bottom = 5.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 1.dp)
            ) {
                Text(text = title)
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${TestTag.FILTER_ROW_PREFIX}${title}")
            ) {
                item {
                    val allSelected = filterExclude().isEmpty()
                    Box(
                        modifier = Modifier
                            .background(
                                if (allSelected) colorResource(R.color.accent) else Color.Gray,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable {
                                if (!allSelected) {
                                    removeAllFunc(items)
                                } else {
                                    addAllFunc(items)
                                }
                            }
                            .padding(8.dp)
                            .testTag("${TestTag.FILTER_PREFIX}${title}")
                    ) {
                        Text(text = "All")
                    }
                }

                items(items.size) { index ->
                    val item = items[index]
                    val isSelected = !filterExclude().contains(item)
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) colorResource(R.color.accent) else Color.Gray,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable {
                                if (isSelected) {
                                    addFunc(item)
                                } else {
                                    removeFunc(item)
                                }

                            }
                            .padding(8.dp)
                    ) {
                        val displayText = if (item is Category) {
                            item.name
                        } else {
                            item.toString()
                        }
                        Text(text = displayText)
                    }
                }
            }

        }
    }
}