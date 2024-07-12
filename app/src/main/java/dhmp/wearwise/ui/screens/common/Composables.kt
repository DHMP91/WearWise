package dhmp.wearwise.ui.screens.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun Collapsible(headerText: String, content: @Composable () -> Unit){
    //Dropdown state
    var expandedState by rememberSaveable { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f, label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable {
                    expandedState = !expandedState
                }
                .padding(10.dp)
        ) {
            Text(
                modifier = Modifier
                    .weight(6f)
                    .padding(start = 10.dp),
                text = headerText,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Drop-Down Arrow",
                modifier = Modifier
                    .weight(1f)
                    .alpha(0.2f)
                    .rotate(rotationState),
            )

        }
        if (expandedState) {
            content()
        }
    }
}