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
import androidx.compose.material3.BottomAppBarDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dhmp.wearwise.R
import dhmp.wearwise.model.Outfit
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.clothing.ClothingViewModel
import dhmp.wearwise.ui.screens.common.categoryIcon

@Composable
fun OutfitScreen(
    onEdit: (Long) -> Unit,
    onTakePicture: (Long) -> Unit,
    onNewOutfit: () -> Unit,
    model: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
) {
    val outfits = model.outfits.collectAsLazyPagingItems()
    val lineColor = MaterialTheme.colorScheme.onBackground

    Surface {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                item {
                    Text(
                        text = "Outfits",
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(dimensionResource(id = R.dimen.screen_title_padding))
                            .drawBehind {
                                val strokeWidth = 2.dp.toPx()
                                val y = size.height - strokeWidth / 2
                                drawLine(
                                    color = lineColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth
                                )
                            }
                            .padding(bottom = 10.dp)
                    )

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
fun NewOutfit(onNewOutfit: () -> Unit, model: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)){
    FloatingActionButton(
        onClick = {
            onNewOutfit()
        },
        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),

    ) {
        Icon(Icons.Filled.Add, "Add Garment")
    }
}

@Composable
fun OutfitCard(
    outfit: Outfit,
    onTakePicture: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    model: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory),
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
){
    clothingViewModel.collectCategories()
    val categories by remember { clothingViewModel.categories }.collectAsState()
    val garments by remember(outfit) { model.getGarments(outfit) }.collectAsState(initial = null)

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


            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 10.dp)
                        .weight(1.5f),
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(outfit.image)
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
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(item.image).build(),
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