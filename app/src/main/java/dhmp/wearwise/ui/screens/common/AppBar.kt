package dhmp.wearwise.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dhmp.wearwise.R


@Composable
fun WearWiseBottomAppBar(
    modifier: Modifier = Modifier,
    navOutfit: () -> Unit,
    navClothing: () -> Unit,
    navNewClothing: () -> Unit,
    navShop: () -> Unit,
    navUser: () ->  Unit,
    route: String?
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.bottom_bar_height))
            .shadow(elevation = dimensionResource(id = R.dimen.default_elevation))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize()
            ) {
                Row {
                    IconButton(
                        onClick = { navClothing() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag(TestTag.BOTTOMBAR_CLOTHING)
                    ) {
                        NavIcon(isCurrentRoute("clothing", route), painterResource(R.drawable.hangar))
                    }
                    IconButton(
                        onClick = { navOutfit() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag(TestTag.BOTTOMBAR_OUTFIT),
                    ) {
                        NavIcon(isCurrentRoute("outfit", route), painterResource(R.drawable.outfit))
                    }

                    IconButton(
                        onClick = { navUser() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag(TestTag.BOTTOMBAR_USER),
                    ) {
                        NavIcon(isCurrentRoute("user", route), rememberVectorPainter(Icons.Outlined.AccountCircle))
                    }

//                IconButton(onClick = { navShop() }) {
//                    Icon(
//                        Icons.Filled.ShoppingCart,
//                        contentDescription = "Shop",
//                    )
//                }
                }
            }
        }
    }
}

@Composable
fun NavIcon(isCurrentRoute: Boolean, icon: Painter){
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

    return Icon(
        icon,
        contentDescription = "",
//            tint = accentColor,
        modifier = if (isCurrentRoute) bottomBorderModifier else noBorder
    )
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