package dhmp.wearwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
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
    Scaffold (
        bottomBar = {
            WearWiseBottomAppBar(
                navOutfit = { navController.navigate(AppScreens.Outfit.name) },
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
//        App()
    }
}