package dhmp.wearwise.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.ui.screens.user.UserScreenContent
import dhmp.wearwise.ui.theme.WearWiseTheme

class PieScreenshotTest {

    @Preview(showBackground = true)
    @Composable
    fun PieCard() {
        val fakeData: Map<String?, Int> = mapOf(
            "Blue" to 11,
            "Red" to 5,
            "Green" to 20,
            null to 8
        )

        val fakeColors = listOf(
            Color(GarmentColorNames[1].color),
            Color(GarmentColorNames[2].color),
            Color(GarmentColorNames[3].color),
            Color(GarmentColorNames[4].color),
            Color(GarmentColorNames[5].color),
        )
        WearWiseTheme {
            Scaffold(
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    UserScreenContent(
                        10,
                        20,
                        fakeData,
                        fakeData,
                        fakeData,
                        fakeData,
                        fakeColors
                    )
                }
            }
        }
    }

}