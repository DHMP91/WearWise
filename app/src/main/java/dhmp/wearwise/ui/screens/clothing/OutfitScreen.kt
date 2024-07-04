package dhmp.wearwise.ui.screens.clothing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import dhmp.wearwise.model.Outfit
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.common.categoryIcon

@Composable
fun OutfitScreen(
    onEdit: (Long) -> Unit,
    model: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
) {
    Surface {
        val outfits = model.outfits.collectAsLazyPagingItems()

        LazyColumn {
            items(outfits.itemCount){index ->
                outfits[index]?.let {
                    OutfitCard(it)
                }
            }
        }

    }
}

@Composable
fun OutfitCard(
    outfit: Outfit,
    model: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory),
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
){
    clothingViewModel.collectCategories()
    val categories by remember { clothingViewModel.categories }.collectAsState()
    val garments by remember(outfit) { model.getGarments(outfit) }.collectAsState(initial = null)

//    val categoryMap = mutableMapOf()
//
//    garments?.forEach{ garment ->
//        val icon = categoryIcon(garment, categories)
//        garment.categoryId?.let {
//            categoryMap[it] = garment
//        }
//    }

    val rowSize = 4
    val rows = garments?.chunked(rowSize) ?: listOf()
    Card(
        modifier = Modifier
            .heightIn(max = 250.dp)
            .fillMaxWidth()
            .padding(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background)
    ) {
        Row {
            Image(
                imageVector = Icons.Rounded.Face,
                contentDescription = "2",
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.5f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(2f)
            ){
                rows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(5.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (item in row) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                    .padding(5.dp),
                            ) {
                                Image(
                                    imageVector = Icons.Rounded.Face,
                                    contentDescription = "2",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(3f)
                                        .background(color = Color.Green)
                                )

                                Icon(
                                    painter = painterResource(categoryIcon(item, categories)),
                                    contentDescription = "Clothing Type",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .align(Alignment.CenterHorizontally)
                                        .padding(2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}