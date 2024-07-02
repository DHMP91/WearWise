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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dhmp.wearwise.R

@Composable
fun OutfitScreen(
    onEdit: (Long) -> Unit,
//    model: OutfitViewModel = viewModel(factory = AppViewModelProvider.OutFitFactory)
) {
    Surface {
        Card(
            modifier = Modifier
                .heightIn(max = 250.dp)
                .fillMaxWidth(),
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
                    val topRow = listOf(
                        mapOf(
                            "icon" to R.drawable.hats_icon,
                            "image" to null
                        ),
                        mapOf(
                            "icon" to R.drawable.dress_icon,
                            "image" to null
                        ),
                        mapOf(
                            "icon" to R.drawable.shirt_icon,
                            "image" to null
                        ),
                        mapOf(
                            "icon" to R.drawable.pants_icon,
                            "image" to null
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(5.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for(item in topRow) {
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
                                    painter = painterResource(item["icon"]!!),
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


                    val bottomRow = listOf(
                        mapOf(
                            "icon" to R.drawable.hats_icon,
                            "image" to null
                        ),
                        mapOf(
                            "icon" to R.drawable.dress_icon,
                            "image" to null
                        ),
                        mapOf(
                            "icon" to R.drawable.shirt_icon,
                            "image" to null
                        ),
                        mapOf(
                            "icon" to R.drawable.pants_icon,
                            "image" to null
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(5.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for(item in bottomRow) {
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
                                    painter = painterResource(item["icon"]!!),
                                    contentDescription = "Clothing Type",
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .align(Alignment.CenterHorizontally),
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}