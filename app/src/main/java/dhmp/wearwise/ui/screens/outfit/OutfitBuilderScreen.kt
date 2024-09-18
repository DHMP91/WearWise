package dhmp.wearwise.ui.screens.outfit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dhmp.wearwise.R
import dhmp.wearwise.model.Categories
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.model.Season
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.clothing.ClothingViewModel
import dhmp.wearwise.ui.screens.common.Collapsible
import dhmp.wearwise.ui.screens.common.DropdownMenu
import dhmp.wearwise.ui.screens.common.ImageScreen
import dhmp.wearwise.ui.screens.common.ScreenTitle
import dhmp.wearwise.ui.screens.common.TestTag
import dhmp.wearwise.ui.screens.common.categoryIcon
import dhmp.wearwise.ui.screens.common.fieldBorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch


@Composable
fun EditOutfitScreen(
    id: Long,
    onTakePicture: (Long) -> Unit,
    onClickPicture: (String) -> Unit,
    onCrop: (String) -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory),
) {
    LaunchedEffect(id) {
        outfitViewModel.getOutfit(id)
    }
    BuilderColumn(onFinish, onTakePicture, onClickPicture, onCrop, clothingViewModel, outfitViewModel)

    var backPressHandled by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    BackHandler(enabled = !backPressHandled) {
        backPressHandled = true
        coroutineScope.launch(Dispatchers.Main) {
            awaitFrame()
            onBack()
            backPressHandled = false
        }
    }
}

@Composable
fun NewOutfitScreen(
    onTakePicture: (Long) -> Unit,
    onClickPicture: (String) -> Unit,
    onFinish: () -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory),
) {
    LaunchedEffect(null) {
        outfitViewModel.newOutfit()
    }
    BuilderColumn(onFinish, onTakePicture, onClickPicture, {}, clothingViewModel, outfitViewModel)

}

@Composable
fun OutfitPictureScreen(
    outfitId: Long,
    onFinish: (Long) -> Unit,
    model: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory),
) {
    LaunchedEffect(outfitId){
        model.getOutfit(outfitId)
    }
    val outfitUri by model.outfitUri.collectAsState()
    if (outfitUri != null){
        onFinish(outfitId)
    }
    Surface {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            ImageScreen(model::saveImage, outfitId, onBack = { onFinish(outfitId) })
        }
    }
}

@Composable
fun OutfitBuilderHeader(
    onFinish: () -> Unit,
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Title(outfitViewModel)
        DeleteOutfit(onFinish = onFinish, outfitViewModel)
    }
}


@Composable
fun Title(
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){
    val outfit by outfitViewModel.outfit.collectAsState()
    Box(
        contentAlignment = Alignment.CenterStart,
    ) {
        ScreenTitle("Outfit #${outfit?.id}")
    }
}


@Composable
fun BuilderColumn(
    onFinish: () -> Unit,
    onTakePicture: (Long) -> Unit,
    onClickPicture: (String) -> Unit,
    onCrop: (String) -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {

        Row(
            modifier = Modifier
                .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp)
                .fillMaxSize()
                .weight(2f)
        ) {
            OutfitCard(
                onFinish = onFinish,
                onTakePicture = onTakePicture,
                onClickPicture = onClickPicture,
                onCrop = onCrop,
                clothingViewModel = clothingViewModel,
                outfitViewModel = outfitViewModel
            )
        }

        Row(
            modifier = Modifier
                .padding(
                    start = dimensionResource(id = R.dimen.screen_title_padding),
                    end = dimensionResource(id = R.dimen.screen_title_padding)
                )
                .weight(0.2f)
        ) {
            SaveOutfit(onFinish, outfitViewModel)
        }

    }
}

@Composable
fun OutfitCard(
    onFinish: () -> Unit,
    onTakePicture: (Long) -> Unit,
    onClickPicture: (String) -> Unit,
    onCrop: (String) -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){
    Card(
        modifier = Modifier
            .fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = dimensionResource(id = R.dimen.screen_title_padding),
                    top = dimensionResource(id = R.dimen.screen_title_padding),
                    end = dimensionResource(id = R.dimen.screen_title_padding)
                )
        ) {
            OutfitBuilderHeader(onFinish, outfitViewModel)
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()
                .weight(2.5f)
        ) {
            OutfitImage(onTakePicture, onClickPicture, onCrop, outfitViewModel)
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
                .padding(start = 10.dp, top = 10.dp, end = 10.dp)
        ) {
            SelectedGarments(outfitViewModel)
        }


        Row(
            modifier = Modifier
                .weight(2.2f)
                .wrapContentHeight()
                .padding(start = 10.dp, top = 10.dp, end = 10.dp)
        ) {
            OutlinedCard (
                colors = CardDefaults.cardColors(Color.White),
                border = fieldBorder()
            ){
                CategorizedGarments(
                    clothingViewModel,
                    outfitViewModel
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, top = 2.dp, end = 10.dp)
        ) {
            SeasonField(outfitViewModel)
        }

    }
}

@Composable
fun DeleteOutfit(
    onFinish: () -> Unit,
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){
    Box (
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            "Delete Outfit",
            modifier = Modifier
                .clickable {
                    outfitViewModel.deleteOutfit()
                    onFinish()
                }
        )
    }

}

@Composable
fun SaveOutfit(
    onFinish: () -> Unit,
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){
    val savedOutfitFlagState by outfitViewModel.savedOutfitFlag.collectAsState()
    val buttonColor =
        if(savedOutfitFlagState) ButtonDefaults.buttonColors(Color.Gray)
        else ButtonDefaults.buttonColors(colorResource(R.color.accent))
    val coroutineScope = rememberCoroutineScope()
    Button(
        onClick = {
            if (!savedOutfitFlagState){
                coroutineScope.launch(Dispatchers.Main) {
                    outfitViewModel.saveOutfit()
                    onFinish()
                }
            }
        },
        colors = buttonColor,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Save",
            color = if(savedOutfitFlagState) Color.White else Color.Black
        )
    }
}

@Composable
fun OutfitImage(
    onTakePicture: (Long) -> Unit,
    onClickPicture: (String) -> Unit,
    onCrop: (String) -> Unit,
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){
    val outfit by outfitViewModel.outfit.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    outfit?.let {
        when (it.image) {
            null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .clickable {
                            coroutineScope.launch(Dispatchers.Main) {
                                val id = outfitViewModel.saveOutfit() //Save current changes it any
                                id?.let {
                                    onTakePicture(id)
                                }
                            }
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.shutter_icon),
                        contentDescription = "Outfit Picture",
                        modifier = Modifier
                            .padding(5.dp)
                            .size(max(30.dp, 30.dp))
                    )
                    Text(
                        "Click to take a picture of your outfit",
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }

            "PROCESSING" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }

            else -> {
                val selectedItems by outfitViewModel.getGarments(it).collectAsState(
                    initial = null
                )
                Row {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(0.5f),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End,

                    ){
                        selectedItems?.forEach { garment ->
                            garment.color?.let { garmentColor ->
                                val color = GarmentColorNames.find { color -> color.name == garmentColor }
                                color?.let {
                                    Box(
                                        modifier = Modifier
                                            .size(dimensionResource(R.dimen.icon_max_height))  // Adjust the size as needed
                                            .background(Color(color.color))
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(it.image)
                                .build(),
                            contentDescription = "2",
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    it.image?.let { image -> onClickPicture(image) }
                                },
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxSize()
                            .padding(
                                end = dimensionResource(id = R.dimen.screen_title_padding)
                            ),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {

                        it.image?.let {image ->
                            Icon(
                                painter = painterResource(R.drawable.crop_icon),
                                "Crop Image",
                                modifier = Modifier
                                    .clickable {
                                        onCrop(image)
                                    }
                                    .padding(
                                        bottom = dimensionResource(id = R.dimen.screen_title_padding)
                                    )
                                    .sizeIn(maxHeight = dimensionResource(R.dimen.icon_max_height))
                            )
                        }

                        Icon(
                            painter = painterResource(R.drawable.camera_retake),
                            "Retake Image",
                            modifier = Modifier
                                .clickable {
                                    coroutineScope.launch(Dispatchers.Main) {
                                        val id = outfitViewModel.saveOutfit() //Save current changes
                                        id?.let {
                                            onTakePicture(id)
                                        }
                                    }
                                }
                        )

                    }
                }
            }
        }
    }
}

@Composable
fun SelectedGarments(
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){

    val outfit by outfitViewModel.outfit.collectAsState()
    outfit?.let {
        val selectedItems by outfitViewModel.getGarments(it).collectAsState(
            initial = null
        )
        val categories = Categories

        OutlinedCard(
            modifier = Modifier
                .fillMaxSize(),
            colors = CardDefaults.cardColors(Color.White),
            border = fieldBorder()
        ) {
            if (selectedItems.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                )
                {
                    Text(
                        text = "No Items Selected",
                        fontSize = MaterialTheme.typography.titleSmall.fontSize,
                    )

                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                selectedItems?.forEach { item ->
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(min = 80.dp, max = 100.dp)
                                .testTag(TestTag.SELECTED_GARMENT)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(3f)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.image).build(),
                                    contentDescription = "2",
                                    contentScale = ContentScale.FillHeight,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .widthIn(max = 60.dp)
                                )

                                FloatingActionButton(
                                    onClick = { outfitViewModel.removeFromOutfit(item) },
                                    containerColor = colorResource(R.color.accent),
                                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(15.dp)
                                ) {
                                    Icon(Icons.Filled.Clear, "Remove From Outfit")
                                }

                                Icon(
                                    painter = painterResource(categoryIcon(item, categories)),
                                    contentDescription = "Clothing Type",
                                    modifier = Modifier
                                        .size(dimensionResource(R.dimen.icon_badge_size))
                                        .padding(5.dp)
                                        .align(Alignment.TopStart)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun CategorizedGarments(
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){
    val categories = Categories
    LazyColumn {
        item {
            Box(modifier = Modifier.padding(5.dp)){
                //Empty Box
            }
        }

        categories.forEach { category ->
            item {
                ClothBuilderRow(
                    category,
                    clothingViewModel = clothingViewModel,
                    outfitViewModel = outfitViewModel
                )
            }
        }

        item {
            ClothBuilderRow(
                null,
                clothingViewModel = clothingViewModel,
                outfitViewModel = outfitViewModel
            )
        }

        item {
            Box(modifier = Modifier.padding(50.dp)){
                //Empty Box
            }
        }
    }

}


@Composable
fun ClothBuilderRow(
    category: Category?,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){
    val itemWidthDp = 150.dp // Assume each item is 150.dp wide, adjust as needed
    val itemHeightMax = 100.dp
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val startPadding = max((screenWidthDp - itemWidthDp) / 8, 0.dp)
    val garmentsFlow = remember(category?.id) {
        clothingViewModel.getGarmentsByCategory(category?.id)
    }
    val garments = garmentsFlow.collectAsLazyPagingItems()
    var headerText = "Unidentified"
    val onClick = { garment: Garment ->
        outfitViewModel.addToOutfit(garment)
    }

    if(category != null) {
        headerText = category.name
    }

    //Selected item state
    val listState = rememberLazyListState()
    Collapsible("${headerText} (${garments.itemCount})") {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 20.dp, top = 5.dp, end = 20.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(startPadding),
        ) {
            items(garments.itemCount) { index ->
                Box(
                    modifier = Modifier
                        .width(itemWidthDp)
                        .heightIn(max = itemHeightMax)
                ) {
                    garments[index]?.let {
                        ClothBuilderItem(it, onClick)
                    }
                }
            }
        }
    }
}

@Composable
fun ClothBuilderItem(
    garment: Garment,
    onClick: (Garment) -> Unit,
){
    Card(
        modifier = Modifier
            .heightIn(max = 150.dp)
            .fillMaxWidth()
            .clickable {
                onClick(garment)
            }
            .testTag("${TestTag.CATEGORIZED_GARMENT_PREFIX}${garment.categoryId}"),
//        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
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
fun SeasonField(outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory),){
    val outfit by outfitViewModel.outfit.collectAsState()
    val updateSeason = { value: String ->
        outfitViewModel.setSeason(Season.valueOf(value))
    }
    val seasons = Season.entries.map { it.name }
    DropdownMenu("Season", seasons, outfit?.season?.name, updateSeason)
}

