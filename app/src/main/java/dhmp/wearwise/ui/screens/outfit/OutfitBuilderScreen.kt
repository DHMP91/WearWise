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
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.clothing.ClothingViewModel
import dhmp.wearwise.ui.screens.common.CameraScreen
import dhmp.wearwise.ui.screens.common.Collapsible
import dhmp.wearwise.ui.screens.common.ScreenTitle
import dhmp.wearwise.ui.screens.common.categoryIcon
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch


@Composable
fun EditOutfitScreen(
    id: Long,
    onTakePicture: (Long) -> Unit,
    onFinish: () -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory),
    onBack: () -> Unit,
) {
    LaunchedEffect(id) {
        outfitViewModel.getOutfit(id)
        clothingViewModel.collectCategories()
    }
    BuilderColumn(onFinish, onTakePicture, clothingViewModel, outfitViewModel)

    var backPressHandled by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    BackHandler(enabled = !backPressHandled) {
        println("back pressed")
        backPressHandled = true
        coroutineScope.launch {
            awaitFrame()
            onBack()
            backPressHandled = false
        }
    }
}

@Composable
fun NewOutfitScreen(
    onTakePicture: (Long) -> Unit,
    onFinish: () -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory),
) {
    LaunchedEffect(null) {
        outfitViewModel.newOutfit()
        clothingViewModel.collectCategories()
    }
    BuilderColumn(onFinish, onTakePicture, clothingViewModel, outfitViewModel)
}

@Composable
fun OutfitPictureScreen(
    outfitId: Long,
    onFinish: (Long) -> Unit,
    model: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory),
) {
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
            CameraScreen(model::saveImage, outfitId)
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
            .padding(dimensionResource(id = R.dimen.screen_title_padding))
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
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){
    Column {

        OutfitBuilderHeader(onFinish, outfitViewModel)


        Row(modifier = Modifier
            .weight(1.5f)
        ) {
            OutfitImage(onTakePicture, outfitViewModel)
        }

        Row(modifier = Modifier
            .weight(1f)
        ) {
            SelectedGarments(clothingViewModel, outfitViewModel)
        }

        Row {
            SaveOutfit(onFinish, outfitViewModel)
        }

        Row(
            modifier = Modifier
                .weight(2f)
        ) {
            CategorizedGarments(
                onFinish,
                clothingViewModel,
                outfitViewModel
            )
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
            "Delete",
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
    val outfit by outfitViewModel.outfit.collectAsState()
    val buttonColor =
        if(savedOutfitFlagState) ButtonDefaults.buttonColors(Color.Gray)
        else ButtonDefaults.buttonColors(Color(0xFF77DD77))
    val coroutineScope = rememberCoroutineScope()
    Button(
        onClick = {
            coroutineScope.launch {
                outfitViewModel.saveOutfit()
                onFinish()
            }
        },
        colors = buttonColor,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(text = "Save Outfit")
    }
}

@Composable
fun OutfitImage(
    onTakePicture: (Long) -> Unit,
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
                        .padding(end = 10.dp)
                        .padding(10.dp)
                        .clickable {
                            coroutineScope.launch {
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
                        tint = Color.White,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(it.image)
                            .build(),
                        contentDescription = "2",
                        contentScale = ContentScale.FillHeight,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedGarments(
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){

    val outfit by outfitViewModel.outfit.collectAsState()
    outfit?.let {
        val selectedItems by outfitViewModel.getGarments(it).collectAsState(
            initial = null
        )
        val categories by clothingViewModel.categories.collectAsState()

        Card(
            modifier = Modifier
                .fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background),
        ) {
            if (selectedItems.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                )
                {
                    Text(
                        text = "Click on image(s) below to start an building your outfit",
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
                                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
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
    onFinish: () -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    outfitViewModel: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
){
    val categories by clothingViewModel.categories.collectAsState()
    LazyColumn(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
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
    val rowPadding = 20.dp
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val startPadding = max((screenWidthDp - itemWidthDp) / 4, 0.dp)

    val garments = clothingViewModel.getGarmentsByCategory(category?.id).collectAsLazyPagingItems()
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
                .padding(rowPadding)
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
            },
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

