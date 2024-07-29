package dhmp.wearwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dhmp.wearwise.ui.screens.common.AppNav
import dhmp.wearwise.ui.screens.common.AppScreens
import dhmp.wearwise.ui.screens.common.WearWiseBottomAppBar
import dhmp.wearwise.ui.theme.WearWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WearWiseTheme {
                App()
            }
        }
    }
}



@Composable
fun App(navController: NavHostController = rememberNavController()) {
    // Get the current back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Retrieve the current route
    val currentRoute = navBackStackEntry?.destination?.route
    Scaffold (
        bottomBar = {
            WearWiseBottomAppBar(
                navOutfit = { navController.navigate(AppScreens.Outfit.name){
                    popUpTo(AppScreens.Outfit.name) {
                        inclusive = false
                    }
                } },
                navClothing = {
                    navController.navigate(AppScreens.Clothing.name){
                        popUpTo(AppScreens.Clothing.name) {
                            inclusive = true
                        }
                    }
                },
                navNewClothing = {
                    navController.navigate(AppScreens.NewClothing.name)
                },
                navShop = { navController.navigate(AppScreens.Shop.name) },
                currentRoute
            )
        },
    ) { innerPadding ->
        AppNav(modifier =  Modifier.padding(innerPadding), navController = navController)
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WearWiseTheme {
//        ClothingScreen()
////        App()
//        OutfitScreen(
//            onEdit = {}
//        )
    }
}