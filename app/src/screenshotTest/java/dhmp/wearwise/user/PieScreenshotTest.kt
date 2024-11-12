package dhmp.wearwise.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import dhmp.wearwise.model.AISource
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.model.UserConfig
import dhmp.wearwise.ui.screens.user.UserScreenContent
import dhmp.wearwise.ui.theme.WearWiseTheme

class PieScreenshotTest {

    @Preview(showBackground = true)
    @Composable
    fun PieCard() {
        val fakeDataOneRow: Map<String?, Int> = mapOf(
            "Blue" to 11,
            "Red" to 5,
            "Green" to 20,
            null to 8
        )

        val fakeDataTwoRow: Map<String?, Int> = mapOf(
            "1" to 11,
            "2" to 5,
            "3" to 20,
            "4" to 14,
            "5" to 11,
            "6" to 5,
            "7" to 28,
            "8" to 7,
        )

        val fakeDataThreeRow: Map<String?, Int> = mapOf(
            "1" to 11,
            "2" to 5,
            "3" to 20,
            "4" to 14,
            "5" to 11,
            "6" to 5,
            "7" to 28,
            "8" to 7,
            "9" to 9,
            "10" to 10,
            "11" to 11,
            "12" to 12,
        )

        val fakeColors = listOf(
            Color(GarmentColorNames[1].color),
            Color(GarmentColorNames[2].color),
            Color(GarmentColorNames[3].color),
            Color(GarmentColorNames[4].color),
            Color(GarmentColorNames[5].color),
            Color(GarmentColorNames[6].color),
            Color(GarmentColorNames[7].color),
            Color(GarmentColorNames[8].color),
            Color(GarmentColorNames[9].color),
            Color(GarmentColorNames[10].color),
            Color(GarmentColorNames[11].color),
            Color(GarmentColorNames[12].color),
            Color(GarmentColorNames[13].color),
        )
        WearWiseTheme {
            Scaffold(
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    UserScreenContent(
                        10,
                        20,
                        fakeDataOneRow,
                        fakeDataTwoRow,
                        fakeDataThreeRow,
                        fakeDataOneRow,
                        fakeColors,
                        {},
                        false,
                        UserConfig(-1, AISource.GOOGLE, "", ""),
                        { _ -> }
                    )
                }
            }
        }
    }

}