package dhmp.wearwise.ui.screens.user
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


//More info on Pie chart code:
// https://medium.com/@developerchunk/create-custom-pie-chart-with-animations-in-jetpack-compose-android-studio-kotlin-49cf95ef321e

val defaultColors =  listOf(
    Color(0xFFE0E0E0), // Light Pastel Gray
    Color(0xFFFFB3C1), // Saturated Pastel Pink
    Color(0xFFAEC6CF), // Saturated Pastel Blue
    Color(0xFFFFF176), // Saturated Pastel Yellow
    Color(0xFF77DD77), // Saturated Pastel Green
    Color(0xFFFFDAB9), // Saturated Pastel Peach
    Color(0xFFD4A4FF), // Saturated Pastel Purple
    Color(0xFFFFAB91), // Saturated Pastel Coral
    Color(0xFFFFC4E0), // Saturated Pastel Rose
    Color(0xFFB3E5FC), // Saturated Pastel Sky Blue
    Color(0xFFFFF59D),  // Saturated Pastel Lemon Yellow
    Color(0xFFB2DFDB), // Saturated Pastel Teal
    Color(0xFFFFCCBC), // Saturated Pastel Orange
    Color(0xFFCCE5FF), // Saturated Pastel Light Blue
    Color(0xFFF5C6CB), // Saturated Pastel Light Red
    Color(0xFFDFD3C3), // Saturated Pastel Taupe
    Color(0xFFC4E17F), // Saturated Pastel Lime
    Color(0xFFFDDF98), // Saturated Pastel Apricot
    Color(0xFFFFE4B5), // Saturated Pastel Moccasin
    Color(0xFFF8BBD0), // Saturated Pastel Pink Blush
    Color(0xFFE6E6FA), // Saturated Pastel Lavender
    Color(0xFFFFD700)  // Saturated Pastel Gold
)

@Composable
fun PieCard(
    title: String,
    data: Map<String?, Int>,
    radiusOuter: Dp = 50.dp,
    chartBarWidth: Dp = 35.dp,
    animDuration: Int = 700,
    colors: List<Color> = defaultColors
){
    Card(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(max = 220.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        PieRow(
            title,
            data = data,
            radiusOuter = radiusOuter,
            chartBarWidth = chartBarWidth,
            animDuration = animDuration,
            colors = colors
        )
    }
}

@Composable
fun PieRow(
    title: String,
    data: Map<String?, Int>,
    radiusOuter: Dp = 50.dp,
    chartBarWidth: Dp = 35.dp,
    animDuration: Int = 700,
    colors: List<Color> = defaultColors
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PieChart(
                data,
                radiusOuter,
                chartBarWidth,
                animDuration,
                colors
            )
        }

        Column(
            Modifier
                .weight(2f)
                .fillMaxSize()
                .padding(start = 20.dp),
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
            ) {
                Text(title)
            }

            Row(
                Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start
            ) {
                DetailsPieChart(
                    data = data,
                    colors = colors
                )
            }
        }
    }
}

@Composable
fun PieChart(
    data: Map<String?, Int>,
    radiusOuter: Dp,
    chartBarWidth: Dp,
    animDuration: Int,
    colors: List<Color>
){
    val totalSum = data.values.sum()
    val floatValue = mutableListOf<Float>()
    var lastValue = 0f
    val density = LocalDensity.current

    data.values.forEachIndexed { index, values ->
        floatValue.add(index, 360 * values.toFloat() / totalSum.toFloat())
    }

    var animationPlayed by remember { mutableStateOf(false) }
    val animateSize by animateFloatAsState(
        targetValue = if (animationPlayed) radiusOuter.value * 2f else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        )
    )
    val animateRotation by animateFloatAsState(
        targetValue = if (animationPlayed) 180f else 0f,
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


//                val middleAngle = lastValue + value / 2f
//                val angleInRadians = middleAngle * PI / 180f
//                val labelRadius = radiusOuterPx + chartBarWidthPx / 2f
//                val x = (size.width / 2) + (labelRadius * cos(angleInRadians)).toFloat()
//                val y = (size.height / 2) + (labelRadius * sin(angleInRadians)).toFloat()
//                drawContext.canvas.nativeCanvas.apply {
//                    val textPaint = android.graphics.Paint().apply {
//                        color = android.graphics.Color.BLACK
//                        textSize = 32f  // Text size is in pixels
//                        textAlign = android.graphics.Paint.Align.CENTER
//                    }
//                    drawText("Label", x, y, textPaint)
//                }
                lastValue += value
            }

        }
    }

}

@Composable
fun DetailsPieChart(
    data: Map<String?, Int>,
    colors: List<Color>,
    maxRows: Int = 3
) {
    val chunkedKeys = data.entries.chunked(maxRows)
    val chunkedColor = colors.chunked(maxRows)

    chunkedKeys.forEachIndexed { index, _ ->
        val dataMap = chunkedKeys[index]
        val colorList = chunkedColor[index]

        Column{
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
    data: Pair<String?, Int>,
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
                text = data.first ?: "Not Specified",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.Black,
                maxLines = 1
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