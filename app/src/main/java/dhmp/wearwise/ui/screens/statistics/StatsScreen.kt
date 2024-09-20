package dhmp.wearwise.ui.screens.statistics

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import dhmp.wearwise.ui.AppViewModelProvider

//More info on Pie chart code:
// https://medium.com/@developerchunk/create-custom-pie-chart-with-animations-in-jetpack-compose-android-studio-kotlin-49cf95ef321e

@Composable
fun StatsScreem(
    onBack: () -> Unit,
    statsViewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.StatsFactory),
){
    PieRow(
        data = mapOf(
            Pair("Sample-1", 150),
            Pair("Sample-2", 120),
            Pair("Sample-3", 110),
            Pair("Sample-4", 170),
            Pair("Sample-5", 120),
        )
    )
}