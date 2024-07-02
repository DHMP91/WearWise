package dhmp.wearwise.ui.screens.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dhmp.wearwise.R
import dhmp.wearwise.ui.screens.clothing.ClothingScreen
import dhmp.wearwise.ui.screens.clothing.EditClothingScreen
import dhmp.wearwise.ui.screens.clothing.NewClothingScreen
import dhmp.wearwise.ui.screens.clothing.OutfitScreen

enum class AppScreens(@StringRes val title: Int) {
    Clothing(title = R.string.inventory),
    NewClothing(title= R.string.new_garment),
    EditClothing(title= R.string.edit_garment),
    Outfit(title = R.string.outfit),
    EditOutfit(title= R.string.edit_outfit),
    Shop(title = R.string.shop),
}

@Composable
fun AppNav(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()){
    NavHost(
        navController = navController,
        startDestination = AppScreens.Clothing.name,
        modifier = modifier
    ) {
        composable(route = AppScreens.Clothing.name) {
            ClothingScreen(onEdit = { id: Long -> navController.navigate("${AppScreens.EditClothing.name}/$id")} )
        }
        composable(route = AppScreens.NewClothing.name) {
            NewClothingScreen(onFinish = { id: Long ->
                navController.navigate("${AppScreens.EditClothing.name}/$id")
            })
        }
        composable(
            route = "${AppScreens.EditClothing.name}/{garmentId}",
            arguments = listOf(navArgument("garmentId") { type = NavType.LongType })
        ) {backStackEntry ->
            when (val garmentId = backStackEntry.arguments?.getLong("garmentId")) {
                null -> navController.navigate(navController.graph.findStartDestination().id)
                else -> EditClothingScreen(
                    onFinish = {
                        navController.navigate(AppScreens.Clothing.name){
                            popUpTo(AppScreens.Clothing.name) {
                                inclusive = true
                            }
                        }
                    },
                    garmentId = garmentId
                )
            }
        }
        composable(route = AppScreens.Outfit.name) {
            OutfitScreen(onEdit = { id: Long -> navController.navigate("${AppScreens.EditOutfit.name}/$id")} )
        }
    }
}