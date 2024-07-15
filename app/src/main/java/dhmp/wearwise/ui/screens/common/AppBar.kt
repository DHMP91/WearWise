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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dhmp.wearwise.R

@Composable
fun WearWiseBottomAppBar(
    navOutfit: () -> Unit,
    navClothing: () -> Unit,
    navNewClothing: () -> Unit,
    navShop: () -> Unit,
    route: String?
) {
    val tabColor = MaterialTheme.colorScheme.onBackground
    val clothingModifier = createModifier(route, "clothing", tabColor)
    val outfitModifier =  createModifier(route, "outfit", tabColor)

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
                IconButton(
                    onClick = { navClothing() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.hangar),
                        contentDescription = "Clothing",
                        modifier = clothingModifier
                    )
                }
                IconButton(
                    onClick = { navOutfit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Icon(
                        painterResource(R.drawable.outfit),
                        contentDescription = "Outfits",
                        modifier = outfitModifier
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

private fun createModifier(currentRoute: String?, commonScreenName: String, tabColor: Color): Modifier{
    val bottomBorderModifier =
        Modifier
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = tabColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }
            .padding(bottom = 10.dp)
    val noBorder = Modifier.padding(bottom = 10.dp)
    var matches = listOf<String>()

    currentRoute?.let { r ->
        matches = AppScreens.entries
            .filter {
                it.name.lowercase().contains(commonScreenName)
                        && r.lowercase().contains(it.name.lowercase())
            }
            .map { it.name }
    }

    val tabModifier = if(matches.isNotEmpty()) bottomBorderModifier else noBorder
    return tabModifier
}