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
import dhmp.wearwise.ui.screens.outfit.EditOutfitScreen
import dhmp.wearwise.ui.screens.outfit.NewOutfitScreen
import dhmp.wearwise.ui.screens.outfit.OutfitPictureScreen
import dhmp.wearwise.ui.screens.outfit.OutfitScreen
import dhmp.wearwise.ui.screens.outfit.OutfitsByIdsScreen

enum class AppScreens(@StringRes val title: Int) {
    Clothing(title = R.string.inventory),
    NewClothing(title= R.string.new_garment),
    EditClothing(title= R.string.edit_garment),
    Outfit(title = R.string.outfit),
    OutfitPicture(title = R.string.outfitPicture),
    EditOutfit(title= R.string.edit_outfit),
    NewOutfit(title= R.string.new_outfit),
    Shop(title = R.string.shop),
}

@Composable
fun AppNav(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()){
    NavHost(
        navController = navController,
        startDestination = AppScreens.Clothing.name,
        modifier = modifier
    ) {
        val navToOutfit = {
            navController.navigate(AppScreens.Outfit.name) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
        }

        val navClothings = {
            navController.navigate(AppScreens.Clothing.name) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
        }

        composable(route = AppScreens.Clothing.name) {
            ClothingScreen(
                onEdit = { id: Long -> navController.navigate("${AppScreens.EditClothing.name}/$id")},
                onNewClothing = { navController.navigate(AppScreens.NewClothing.name)}
            )
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
                    onOutfits = { id: Long ->
                        navController.navigate("${AppScreens.Outfit.name}?garmentId=$id")
                    },
                    onBack = navClothings,
                    garmentId = garmentId
                )
            }
        }
        composable(route = AppScreens.Outfit.name) {
            OutfitScreen(
                onEdit = { id: Long -> navController.navigate("${AppScreens.EditOutfit.name}/$id"){
                    popUpTo(AppScreens.Outfit.name) {
                        inclusive = false
                    }
                } },
                onTakePicture = { id: Long -> navController.navigate("${AppScreens.OutfitPicture.name}/$id"){
                    popUpTo(AppScreens.Outfit.name) {
                        inclusive = false
                    }
                }},
                onNewOutfit = {
                    navController.navigate(AppScreens.NewOutfit.name){
                        popUpTo(AppScreens.Outfit.name) {
                            inclusive = false
                        }
                    }
                }
            )
        }
        composable(
            route = "${AppScreens.Outfit.name}?garmentId={garmentId}",
            arguments = listOf(navArgument("garmentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val garmentId = backStackEntry.arguments?.getLong("garmentId")
            when(garmentId) {
                null -> navController.navigate(navController.graph.findStartDestination().id)
                else -> {
                    OutfitsByIdsScreen(
                        garmentId,
                        onEdit = { id: Long ->
                            navController.navigate("${AppScreens.EditOutfit.name}/$id") {
                                popUpTo(AppScreens.Outfit.name) {
                                    inclusive = false
                                }
                            }
                        },
                        onTakePicture = { id: Long ->
                            navController.navigate("${AppScreens.OutfitPicture.name}/$id") {
                                popUpTo(AppScreens.Outfit.name) {
                                    inclusive = false
                                }
                            }
                        },
                        onNewOutfit = {
                            navController.navigate(AppScreens.NewOutfit.name) {
                                popUpTo(AppScreens.Outfit.name) {
                                    inclusive = false
                                }
                            }
                        }
                    )
                }
            }
        }
        composable(
            route = "${AppScreens.OutfitPicture.name}/{outfitId}",
            arguments = listOf(navArgument("outfitId") { type = NavType.LongType })
        ) {backStackEntry ->
            val outfitId = backStackEntry.arguments?.getLong("outfitId")
            when(outfitId) {
                null -> navController.navigate(navController.graph.findStartDestination().id)
                else ->
                    OutfitPictureScreen(
                        outfitId,
                        onFinish = { id: Long ->
                            navController.navigate("${AppScreens.EditOutfit.name}/$id")
                            //Don't pop up to outfit otherwise job will be cancelled
                        },
                        navOutfit = {
                            navController.navigate(AppScreens.Outfit.name){
                                popUpTo(AppScreens.Outfit.name) {
                                    inclusive = false
                                }
                            }
                        }
                    )
            }
        }
        composable(
            route = "${AppScreens.EditOutfit.name}/{outfitId}",
            arguments = listOf(navArgument("outfitId") {
                type = NavType.LongType
                defaultValue = 0L  // Default value to handle the case when outfitId is not provided
            })
        ) { backStackEntry ->
            val outfitId = backStackEntry.arguments?.getLong("outfitId")

            when(outfitId){
                null -> navController.navigate(navController.graph.findStartDestination().id)
                else -> EditOutfitScreen(
                    id = outfitId,
                    onTakePicture = { id: Long -> navController.navigate("${AppScreens.OutfitPicture.name}/$id") },
                    onFinish = navToOutfit,
                    onBack = navToOutfit
                )
            }
        }
        composable(
            route = AppScreens.NewOutfit.name,
        ) {
            NewOutfitScreen(
                onTakePicture = { id: Long ->
                    navController.navigate("${AppScreens.OutfitPicture.name}/$id") {
                        popUpTo(AppScreens.Outfit.name) {
                            inclusive = false
                        }
                    }
                },
                onFinish = navToOutfit
            )
        }
    }
}