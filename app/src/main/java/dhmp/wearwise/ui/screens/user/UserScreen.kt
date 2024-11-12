package dhmp.wearwise.ui.screens.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dhmp.wearwise.model.AISource
import dhmp.wearwise.model.UserConfig
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.common.DropdownMenu

//More info on Pie chart code:
// https://medium.com/@developerchunk/create-custom-pie-chart-with-animations-in-jetpack-compose-android-studio-kotlin-49cf95ef321e

@Composable
fun UserScreen(
    userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.UserFactory),
){
    val garmentCount by userViewModel.garmentCount().collectAsState(initial = 0)
    val outfitCount by userViewModel.outfitCount().collectAsState(initial = 0)
    val colorData by userViewModel.colorCount().collectAsState(initial = mapOf())
    val categoryData by userViewModel.categoryCount().collectAsState(initial = mapOf())
    val occasionData by userViewModel.occasionCount().collectAsState(initial = mapOf())
    val outfitSeasonData by userViewModel.outfitSeasonCount().collectAsState(initial = mapOf())
    val clothingColors = userViewModel.getColorPalette(colorData.keys.toList())
    val userConfig by userViewModel.userConfig.collectAsState()
    val showConfig by userViewModel.showConfig.collectAsState()
    val onSaveUserConfig = { config: UserConfig ->
        userViewModel.updateConfig(config)
    }
    UserScreenContent(
        garmentCount,
        outfitCount,
        colorData,
        categoryData,
        occasionData,
        outfitSeasonData,
        clothingColors,
        userViewModel::toggleConfig,
        showConfig,
        userConfig,
        onSaveUserConfig
    )
}


@Composable
fun UserScreenContent(
    garmentCount: Int,
    outfitCount: Int,
    colorData: Map<String? , Int>,
    categoryData: Map<String? , Int>,
    occasionData: Map<String? , Int>,
    outfitSeasonData: Map<String? , Int>,
    clothingColors: List<Color>,
    onConfig: () -> Unit,
    showConfig: Boolean,
    userConfig: UserConfig,
    onSaveUserConfig: (UserConfig) -> Unit
){

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {

        Column( modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ){
            Row (modifier = Modifier
                .clickable { onConfig() }
            ) {
                Icon(Icons.Filled.Settings, "User Settings")
            }
            Row (modifier = Modifier
                .padding(vertical = 2.dp)
                .fillMaxWidth()
            ) {
                UserConfigSlideOut(showConfig, userConfig, onSaveUserConfig)
            }
        }

        Row( modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 5.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ){
            Box (modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(end = 5.dp)
            ){
                CountCard("Clothes", garmentCount)
            }


            Box (modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 5.dp)
            ){
                CountCard("Outfits", outfitCount)
            }
        }

        //Garment Category
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 5.dp, horizontal = 10.dp),
        ) {
            PieCard(
                "Clothing's Category",
                data = categoryData
            )
        }

        //Color
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 5.dp, horizontal = 10.dp),
        ) {
            PieCard(
                "Clothing's Color",
                data = colorData,
                colors = clothingColors
            )
        }

        //Occasion
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 5.dp, horizontal = 10.dp),
        ) {
            PieCard(
                "Clothing's Occasions",
                data = occasionData
            )
        }


        //Outfit Season
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 5.dp, horizontal = 10.dp),
        ) {
            PieCard(
                "Outfit Seasons",
                data = outfitSeasonData
            )
        }

    }
}


@Composable
fun CountCard(
    title: String,
    count: Int,
    animDuration: Int = 1000,
) {
    var countTarget by remember { mutableIntStateOf(0) }
    LaunchedEffect(count) {
        countTarget = count
    }

    val animatedCounter by animateIntAsState(
        targetValue = countTarget,
        animationSpec = tween(
            durationMillis = animDuration,
            easing = FastOutSlowInEasing
        )
    )

    Card(
        modifier = Modifier
            .fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                title,

            )
            Text(
                animatedCounter.toString(),
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}



@Composable
fun UserConfigSlideOut(
    showConfig: Boolean,
    userConfig: UserConfig,
    onSaveUserConfig: (UserConfig) ->  Unit,
){
    val density = LocalDensity.current
    val comingSoon = listOf(AISource.OPENAI)
    val comingSoonPrefix = "(Coming Soon)"
    var aiSource by remember { mutableStateOf(userConfig.aiSource) }
    var apiKey by remember { mutableStateOf(userConfig.aiApiKey) }
    var aiModel by remember { mutableStateOf(userConfig.aiModelName) }

    LaunchedEffect(userConfig.id) {
        aiSource = userConfig.aiSource
        apiKey = userConfig.aiApiKey
        aiModel = userConfig.aiModelName
    }

    AnimatedVisibility(
        showConfig,
        enter = slideInVertically {
            // Slide in from 40 dp from the top.
            with(density) { -40.dp.roundToPx() }
        } + expandVertically(
            // Expand from the top.
            expandFrom = Alignment.Top
        ) + fadeIn(
            // Fade in with the initial alpha of 0.3f.
            initialAlpha = 0.3f
        ),
        exit = slideOutVertically{
            with(density) { 40.dp.roundToPx() }
        } + shrinkVertically(

        ) + fadeOut( targetAlpha = 0.0f)
    ){
        Card(
            modifier = Modifier
                .fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors(Color.White),
        ) {

            var sourceFieldValue = aiSource?.name
            if(comingSoon.contains(aiSource)){
                sourceFieldValue = "${sourceFieldValue}${comingSoonPrefix}"
            }
            val onSourceChange = { sourceString: String ->
                val name = sourceString.replace(comingSoonPrefix, "")
                aiSource = AISource.valueOf(name)
            }

            Column (
                modifier = Modifier.padding(10.dp)
            ){
                DropdownMenu(
                    "AI Source",
                    AISource.entries.map { if(comingSoon.contains(it)) "${it.name}${comingSoonPrefix}" else it.name },
                    if(comingSoon.contains(aiSource)) "${sourceFieldValue}${comingSoonPrefix}" else sourceFieldValue,
                    onSourceChange
                )
                if(!comingSoon.contains(aiSource)) {
                    if (aiSource == AISource.GOOGLE) {
                        DropdownMenu(
                            "AI Model",
                            listOf("gemini-1.5-flash-latest"),
                            userConfig.aiModelName,
                            { model ->
                                aiModel = model
                            }
                        )
                    }
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = {
                           apiKey = it
                        },
                        label = { Text("AI API Key") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            userConfig.aiSource = aiSource
                            userConfig.aiModelName = aiModel
                            userConfig.aiApiKey = apiKey
                            onSaveUserConfig(userConfig)
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}