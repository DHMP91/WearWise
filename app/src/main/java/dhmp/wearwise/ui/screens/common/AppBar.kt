package dhmp.wearwise.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dhmp.wearwise.R

@Composable
fun WearWiseBottomAppBar(
    navOutfit: () -> Unit,
    navClothing: () -> Unit,
    navNewClothing: () -> Unit,
    navShop: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(2f)
                .padding(start = 10.dp, bottom = 10.dp)

        ) {
            Row {
                IconButton(onClick = { navClothing() }) {
                    Icon(
                        painter = painterResource(R.drawable.hangar),
                        contentDescription = "Clothing",
                        modifier = Modifier
                            .padding(5.dp)
                    )
                }
                IconButton(onClick = { navOutfit() }) {
                    Icon(
                        painterResource(R.drawable.outfit),
                        contentDescription = "Outfits",
                        modifier = Modifier
                            .padding(5.dp)
                    )
                }

//                IconButton(onClick = { navShop() }) {
//                    Icon(
//                        Icons.Filled.ShoppingCart,
//                        contentDescription = "Shop",
//                    )
//                }
            }
        }
//        Column(
//            modifier = Modifier
//                .padding(bottom = 15.dp, end = 10.dp)
//        ) {
//            FloatingActionButton(
//                onClick = { navNewClothing() },
//                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
//                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
//            ) {
//                Icon(Icons.Filled.Add, "Add Garment")
//            }
//        }
    }
}