package dhmp.wearwise.ui.screens.clothing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dhmp.wearwise.R
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.common.categoryIcon
import kotlinx.coroutines.launch
import java.util.Locale

private val TAG = "ClothingScreen"

@Composable
fun ClothingScreen(
    onEdit: (Long) -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
) {
    Surface {
        Tabs(onEdit)
    }
}

@Composable
fun Tabs(
    onEdit: (Long) -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
){
    var showListView by remember { mutableStateOf(true) }
    var showBuildView by remember { mutableStateOf(false) }
    val tabColor = MaterialTheme.colorScheme.onBackground
    val bottomBorderModifier = { showLine: Boolean ->
        Modifier
            .drawBehind {
                if (showLine) {
                    val strokeWidth = 2.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = tabColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
            }
            .padding(bottom = 10.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, top = 20.dp, end = 20.dp, bottom = 20.dp)
        ){
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        showListView = true
                        showBuildView = false
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "List Clothings",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    maxLines = 1,
                    modifier = bottomBorderModifier(showListView)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        showListView = false
                        showBuildView = true
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            )  {
                Text(
                    text = "Build Outfit",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    maxLines = 1,
                    modifier = bottomBorderModifier(showBuildView)
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
        Row {
            if (showListView) {
                clothingViewModel.collectCategories()
                GarmentList(onEdit)
            } else {
                BuilderView(onEdit)
            }
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
            Icon( painter= painterResource(icon), contentDescription = "Clothing Type")
        }
    }
}

@Composable
fun BuilderView(
    onEdit: (Long) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
){
    val categories by clothingViewModel.categories.collectAsState()
    clothingViewModel.collectCategories()


    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(contentPadding)
            .background(MaterialTheme.colorScheme.background),
    ) {
        categories.forEach { category ->
                item {
                    ClothBuilderRow(
                        category,
                        onEdit
                    )
                }
        }

        item {
            ClothBuilderRow(
                null,
                onEdit,
            )
        }

        item{
            Button(
                onClick = {
                    clothingViewModel.buildOutfit()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.AddCircle, contentDescription = "Create Outfit")
                Text(text="Save Outfit")
            }
        }
    }
}

@Composable
fun ClothBuilderRow(
    category: Category?,
    onEdit: (Long) -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
){
    val itemWidthDp = 150.dp // Assume each item is 150.dp wide, adjust as needed
    val itemHeightMax = 100.dp
    val rowPadding = 20.dp
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val startPadding = max((screenWidthDp - itemWidthDp) / 4, 0.dp)

    val categoryToModelVarMap = clothingViewModel.outfitPiecesIdVariables()
    val garments = clothingViewModel.getGarmentsByCategory(category?.id).collectAsLazyPagingItems()
    var headerText = "Unidentified (Not selectable)"
    var updateSelectedIndex = { _ : Int -> }
    var getSelectedIndex = { -1 }

    if(category != null) {
        headerText = category.name
        val getterSetter = categoryToModelVarMap.find {
            it.name.lowercase().contains(category.name.lowercase())
        }
        if(getterSetter != null) {
            headerText = category?.name ?: headerText
            updateSelectedIndex = { index: Int ->
                if (index >= 0) {
                    garments[index]?.let {
                        getterSetter.set(it.id)
                    }
                }
            }
            getSelectedIndex = {
                var itemIndex = -1
                for (index in 0 until garments.itemCount) {
                    val item = garments[index]
                    if (item?.id == getterSetter.get()) {
                        itemIndex = index
                    }
                }
                itemIndex
            }
        }
    }

    //Selected item state
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val offset = -1
    Collapsible("${headerText} (${garments.itemCount})"){
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
                .padding(rowPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(startPadding)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .width(startPadding - rowPadding)
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .width(itemWidthDp)
                        .heightIn(max = itemHeightMax)
                ){
                    Card(
                        modifier = Modifier
                            .heightIn(max = 150.dp)
                            .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background)
                    ) {
                        Image(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = "First Item",
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
            }
            items(garments.itemCount) { index ->
                val isSelected = index == getSelectedIndex()
                Box(
                    modifier = Modifier
                        .width(itemWidthDp)
                        .heightIn(max = itemHeightMax)
                        .graphicsLayer {
                            if (isSelected) {
                                scaleX = 1.1f
                                scaleY = 1.1f
                            }
                        }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Color.Gray else Color.Transparent
                        )
                ) {
                    garments[index]?.let {
                        ClothBuilderItem(it, onEdit)
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .width(startPadding - rowPadding)
                )
            }
        }

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .collect { firstVisibleItemIndex ->
                    updateSelectedIndex(firstVisibleItemIndex  + offset)
                }
        }

        LaunchedEffect(getSelectedIndex()) {
            coroutineScope.launch {
                val index =  if (getSelectedIndex() <= 0) 0 else getSelectedIndex()
                listState.animateScrollToItem(index)
            }
        }
    }
}


@Composable
fun ClothBuilderItem(garment: Garment, onEdit: (Long) -> Unit){
    Card(
        modifier = Modifier
            .heightIn(max = 150.dp)
            .fillMaxWidth()
            .clickable { onEdit(garment.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(garment.image).build(),
            contentDescription = "GarmentImage",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}


@Composable
fun ClothingScreenTopAppBar(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(2f)
                .padding(start = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onBackground)) {
                Text(text = stringResource(id = R.string.inventory), color = MaterialTheme.colorScheme.onBackground)
            }
        }
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(onClick = { clothingViewModel.showMenu = !clothingViewModel.showMenu }) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
            }
            ClothingMainMenu()
            ClothingBrandSelectionMenu()
        }
    }
}





@Composable
fun Collapsible(headerText: String, content: @Composable () -> Unit){
    //Dropdown state
    var expandedState by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable {
                    expandedState = !expandedState
                }
                .padding(10.dp)
        ) {
            Text(
                modifier = Modifier
                    .weight(6f)
                    .padding(start = 20.dp),
                text = headerText,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon (
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Drop-Down Arrow",
                modifier = Modifier
                    .weight(1f)
                    .alpha(0.2f)
                    .rotate(rotationState),
            )

        }
        if (expandedState) {
            content()
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