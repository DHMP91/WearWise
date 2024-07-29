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
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import dhmp.wearwise.model.Outfit
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.clothing.ClothingViewModel
import dhmp.wearwise.ui.screens.common.ScreenTitle
import dhmp.wearwise.ui.screens.common.categoryIcon

@Composable
fun OutfitScreen(
    onEdit: (Long) -> Unit,
    onTakePicture: (Long) -> Unit,
    onNewOutfit: () -> Unit,
    model: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
) {
    val outfits = model.outfits.collectAsLazyPagingItems()
    OutfitList(outfits, onEdit, onTakePicture, onNewOutfit)
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
    OutfitList(outfits, onEdit, onTakePicture, onNewOutfit, title = "Outfits with clothing item #$garmentId")
}

@Composable
fun NewOutfit(onNewOutfit: () -> Unit){
    FloatingActionButton(
        onClick = {
            onNewOutfit()
        },
        containerColor = colorResource(R.color.accent),
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),

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
    title: String = "All Outfits"
){

    Surface {
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
                        OutfitCard(it, onTakePicture, onEdit)
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
    onTakePicture: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory),
    model: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory),
){
    val categories = Categories
    val garments by remember(outfit) { model.getGarments(outfit) }.collectAsState(initial = null)
    val thumbnail by model.getOutfitThumbnail(outfit).collectAsState(initial = "")
    val row = garments ?: listOf()
    Card(
        modifier = Modifier
            .heightIn(max = 250.dp)
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onEdit(outfit.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background)
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
                    )
                }

            }


            Divider(
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
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
                                    val garmentThumbnail by clothingViewModel.getGarmentThumbnail(item).collectAsState(initial = "")
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
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(0.5f)
                                            .sizeIn(maxHeight = dimensionResource(R.dimen.icon_max_height))
                                            .align(Alignment.CenterHorizontally)
                                    ) {
                                        Icon(
                                            painter = painterResource(
                                                categoryIcon(
                                                    item,
                                                    categories
                                                )
                                            ),
                                            contentDescription = "Clothing Type",
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