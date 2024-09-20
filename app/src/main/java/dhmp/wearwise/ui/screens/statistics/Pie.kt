package dhmp.wearwise.ui.screens.statistics
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


//More info on Pie chart code:
// https://medium.com/@developerchunk/create-custom-pie-chart-with-animations-in-jetpack-compose-android-studio-kotlin-49cf95ef321e

val defaultColors =  listOf(
    Color(0xFFFFB3C1), // Saturated Pastel Pink
    Color(0xFFAEC6CF), // Saturated Pastel Blue
    Color(0xFFFFF176), // Saturated Pastel Yellow
    Color(0xFF77DD77), // Saturated Pastel Green
    Color(0xFFFFDAB9), // Saturated Pastel Peach
    Color(0xFFD4A4FF), // Saturated Pastel Purple
    Color(0xFFFFAB91), // Saturated Pastel Coral
    Color(0xFFFFC4E0), // Saturated Pastel Rose
    Color(0xFFB3E5FC), // Saturated Pastel Sky Blue
    Color(0xFFFFF59D)  // Saturated Pastel Lemon Yellow
)

@Composable
fun PieRow(
    data: Map<String, Int>,
    radiusOuter: Dp = 50.dp,
    chartBarWidth: Dp = 35.dp,
    animDuration: Int = 700,
    colors: List<Color> = defaultColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PieChart(
                data,
                radiusOuter,
                chartBarWidth,
                animDuration,
                defaultColors
            )
        }

        Row(
            Modifier
                .weight(2f)
                .fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {

            DetailsPieChart(
                data = data,
                colors = colors
            )

        }
    }
}

@Composable
fun PieChart(
    data: Map<String, Int>,
    radiusOuter: Dp,
    chartBarWidth: Dp,
    animDuration: Int,
    colors: List<Color>
){
    val totalSum = data.values.sum()
    val floatValue = mutableListOf<Float>()

    data.values.forEachIndexed { index, values ->
        floatValue.add(index, 360 * values.toFloat() / totalSum.toFloat())
    }

    var animationPlayed by remember { mutableStateOf(false) }

    var lastValue = 0f

    val animateSize by animateFloatAsState(
        targetValue = if (animationPlayed) radiusOuter.value * 2f else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        )
    )

    val animateRotation by animateFloatAsState(
        targetValue = if (animationPlayed) 90f * 11f else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        )
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Box(
        modifier = Modifier.size(animateSize.dp),
    ) {
        Canvas(
            modifier = Modifier
                .size(radiusOuter * 2f)
                .rotate(animateRotation)
        ) {
            floatValue.forEachIndexed { index, value ->
                drawArc(
                    color = colors[index],
                    lastValue,
                    value,
                    useCenter = false,
                    style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Butt)
                )
                lastValue += value
            }
        }
    }

}

@Composable
fun DetailsPieChart(
    data: Map<String, Int>,
    colors: List<Color>
) {
    val chunkedKeys = data.entries.chunked(3)
    val chunkedColor = colors.chunked(3)

    chunkedKeys.forEachIndexed { index, _ ->
        val dataMap = chunkedKeys[index]
        val colorList = chunkedColor[index]

        Column(
            modifier = Modifier.padding(start = 10.dp)
        ){
            dataMap.forEachIndexed { index, value ->
                DetailsPieChartItem(
                    data = value.toPair(),
                    color = colorList[index]
                )
            }
        }
    }

}

@Composable
fun DetailsPieChartItem(
    data: Pair<String, Int>,
    height: Dp = 20.dp,
    color: Color
) {
    Row {
        Box(
            modifier = Modifier
                .background(
                    color = color,
                    shape = RoundedCornerShape(10.dp)
                )
                .size(height)
        )
        Column {
            Text(
                modifier = Modifier.padding(start = 15.dp),
                text = data.first,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.Black
            )
            Text(
                modifier = Modifier.padding(start = 15.dp),
                text = data.second.toString(),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

    }
}