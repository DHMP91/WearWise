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
import androidx.compose.ui.platform.testTag
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
                        .testTag(TestTag.BOTTOMBAR_CLOTHING)
                ) {
                    NavIcon(isCurrentRoute("clothing", route), R.drawable.hangar)
                }
                IconButton(
                    onClick = { navOutfit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag(TestTag.BOTTOMBAR_OUTFIT)
                    ,
                ) {
                    NavIcon(isCurrentRoute("outfit", route), R.drawable.outfit)
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

@Composable
fun NavIcon(isCurrentRoute: Boolean, IconResource: Int){
    val accentColor = MaterialTheme.colorScheme.onBackground
    val bottomBorderModifier =
        Modifier
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = accentColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }
            .padding(bottom = 5.dp)
    val noBorder = Modifier.padding(6.dp)
    if(isCurrentRoute) {
        return Icon(
            painterResource(IconResource),
            contentDescription = "",
//            tint = accentColor,
            modifier = bottomBorderModifier
        )
    }else{
        Icon(
            painterResource(IconResource),
            contentDescription = "",
            modifier = noBorder
        )
    }
}

fun isCurrentRoute(commonScreenName: String, currentRoute: String?): Boolean{
    var matches = listOf<String>()

    currentRoute?.let { r ->
        matches = AppScreens.entries
            .filter {
                it.name.lowercase().contains(commonScreenName)
                        && r.lowercase().contains(it.name.lowercase())
            }
            .map { it.name }
    }

    return matches.isNotEmpty()
}