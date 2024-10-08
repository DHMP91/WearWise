package dhmp.wearwise.ui.screens.outfit

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dhmp.wearwise.R
import dhmp.wearwise.model.Categories
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.Outfit
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.clothing.ClothingViewModel
import dhmp.wearwise.ui.screens.common.ScreenTitle
import dhmp.wearwise.ui.screens.common.TestTag
import dhmp.wearwise.ui.screens.common.categoryIcon
import kotlinx.coroutines.flow.Flow

@Composable
fun OutfitScreen(
    onEdit: (Long) -> Unit,
    onTakePicture: (Long) -> Unit,
    onNewOutfit: () -> Unit,
    model: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory),
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
) {
    val outfits = model.outfits.collectAsLazyPagingItems()
    OutfitList(
        outfits,
        onEdit,
        onTakePicture,
        onNewOutfit,
        onDelete = { id: Long -> model.deleteOutfit(id) },
        onGetGarments = { outfit: Outfit -> model.getGarments(outfit) },
        onGetOutfitThumbnail = { outfit: Outfit -> model.getOutfitThumbnail(outfit)},
        onGetGarmentThumbnail = { garment: Garment -> clothingViewModel.getGarmentThumbnail(garment)},
    )
}


@Composable
fun OutfitsByIdsScreen(
    garmentId: Long,
    onEdit: (Long) -> Unit,
    onTakePicture: (Long) -> Unit,
    onNewOutfit: () -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    model: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
) {
    LaunchedEffect (garmentId) {
        clothingViewModel.getGarmentById(garmentId)
    }
    val uiState by clothingViewModel.uiEditState.collectAsState()
    val outfits = model.getOutfitsByListOfId(uiState.editGarment.outfitsId).collectAsLazyPagingItems()
    OutfitList(
        outfits,
        onEdit,
        onTakePicture,
        onNewOutfit,
        onDelete = { id: Long -> model.deleteOutfit(id) },
        onGetGarments = { outfit: Outfit -> model.getGarments(outfit) },
        onGetOutfitThumbnail = { outfit: Outfit -> model.getOutfitThumbnail(outfit)},
        onGetGarmentThumbnail = { garment: Garment -> clothingViewModel.getGarmentThumbnail(garment)},
        title = "Outfits with clothing item #$garmentId"
    )
}

@Composable
fun NewOutfit(onNewOutfit: () -> Unit){
    FloatingActionButton(
        onClick = {
            onNewOutfit()
        },
        containerColor = colorResource(R.color.accent),
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(dimensionResource(id = R.dimen.default_elevation)),
        modifier = Modifier.testTag(TestTag.NEW_OUTFIT_BUTTON)
    ) {
        Icon(Icons.Filled.Add, "Add Garment")
    }
}


@Composable
fun OutfitList(
    outfits: LazyPagingItems<Outfit>,
    onEdit: (Long) -> Unit,
    onTakePicture: (Long) -> Unit,
    onNewOutfit: () -> Unit,
    onDelete: (Long) -> Unit,
    onGetGarments: (Outfit) -> Flow<List<Garment>>,
    onGetOutfitThumbnail: (Outfit) -> Flow<String>,
    onGetGarmentThumbnail: (Garment) -> Flow<String>,
    title: String = "Outfits"
){
    Surface (
        color = MaterialTheme.colorScheme.background
    ){
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(id = R.dimen.screen_title_padding))
                    ) {
                        ScreenTitle(title)
                    }
                }
                items(outfits.itemCount) { index ->
                    outfits[index]?.let {
                        if(it.image == null && it.garmentsId.isEmpty()) {
                            onDelete(it.id)
                        } else {
                            val garments by remember(it) { onGetGarments(it) }.collectAsState(initial = null)
                            val thumbnail by onGetOutfitThumbnail(it).collectAsState(initial = "")
                            OutfitCard(it, garments, thumbnail, onTakePicture, onEdit, onGetGarmentThumbnail =  onGetGarmentThumbnail)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Align to bottom end of the Box
                    .padding(16.dp)
            ){
                NewOutfit(onNewOutfit)
            }
        }
    }
}

@Composable
fun OutfitCard(
    outfit: Outfit,
    garments: List<Garment>?,
    thumbnail: String,
    onTakePicture: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onGetGarmentThumbnail: (Garment) -> Flow<String>,
){
    val categories = Categories
    val row = garments ?: listOf()
    Card(
        modifier = Modifier
            .heightIn(max = 250.dp)
            .fillMaxWidth()
            .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp)
            .clickable { onEdit(outfit.id) }
            .testTag(TestTag.OUTFIT_CARD),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row (
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
        ){

            if (outfit.image == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 10.dp)
                        .weight(1.5f)
                        .clickable { onTakePicture(outfit.id) },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter= painterResource(R.drawable.shutter_icon),
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

            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 10.dp)
                        .weight(1.5f),
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(thumbnail)
                            .build(),
                        contentDescription = "2",
                        contentScale = ContentScale.FillHeight,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .testTag(TestTag.OUTFIT_THUMBNAIL)
                    )
                }

            }


            HorizontalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = Color.Gray
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 5.dp)
                    .weight(2f)
            ){
//                rows.forEach { row ->
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        for (item in row) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .widthIn(min = 80.dp, max = 100.dp)
                                ) {
                                    val garmentThumbnail by onGetGarmentThumbnail(item).collectAsState(initial = "")
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(garmentThumbnail).build(),
                                        contentDescription = "2",
                                        contentScale = ContentScale.FillHeight,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(3f)
                                            .padding(10.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .widthIn(max = 60.dp)
                                            .testTag(TestTag.OUTFIT_GARMENT_THUMBNAIL)
                                    )

                                    val icon =  categoryIcon(item, categories)
                                    Box(
                                        modifier = Modifier
                                            .weight(0.5f)
                                            .align(Alignment.CenterHorizontally)
                                            .testTag("${TestTag.CLOTHING_LIST_CATEGORY_PREFIX}${icon}")
                                    ) {

                                        Icon(
                                            painter = painterResource(icon),
                                            contentDescription = "Clothing Type",
                                            modifier = Modifier
                                                .sizeIn(maxHeight = dimensionResource(R.dimen.outfit_list_icon_height))
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}