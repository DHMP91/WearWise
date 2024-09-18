package dhmp.wearwise.ui.screens.common

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import okhttp3.internal.closeQuietly
import java.io.File

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


@Composable
fun ScreenTitle(text: String){
    Text(
        text = text,
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        maxLines = 1,
        modifier = Modifier
            .padding(bottom = 10.dp)
            .testTag(TestTag.SCREEN_TITLE)
    )
}


@Composable
fun ViewImage(imagePath: String){
    Box(modifier = Modifier
        .fillMaxSize()){
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(imagePath)
                .build(),
            contentDescription = "",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}



@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun CropImage(imagePath: String, onBack: () -> Unit) {
    var cropBitMap: Bitmap? by remember { mutableStateOf(null) }
    val context = LocalContext.current as Activity

    val imageCropLauncher = rememberLauncherForActivityResult(contract = CropImageContract()) { result ->
        if (result.isSuccessful) {
            Uri.parse(imagePath)?.path?.let { original ->
                result.uriContent?.let {
                    moveFile(context, it, original)
                }
            }
        } else {
            // If something went wrong you can handle the error here
            println("ImageCropping error: ${result.error}")
        }

        onBack()
    }

    LaunchedEffect(imagePath) {
        imageCropLauncher.launch(
            CropImageContractOptions(
                uri = Uri.parse(imagePath),
                CropImageOptions(
                    guidelines = CropImageView.Guidelines.ON,
                    outputCompressFormat = Bitmap.CompressFormat.PNG
                )
            )
        )
    }

    cropBitMap?.let { bitmap ->
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

private fun moveFile(context: Context, sourceUri: Uri, destinationPath: String) {
    val inputStream = context.contentResolver.openInputStream(sourceUri)
    val destinationFile = File(destinationPath)

    inputStream?.use { input ->
        destinationFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    inputStream?.closeQuietly()
}

@Composable
fun fieldBorder(): BorderStroke {
    val color = Color.LightGray
    return remember(color) { BorderStroke(1.dp, color) }
}

@Composable
fun DropdownMenu(label: String, options: List<String>, fieldValue: String?, updateField: (String) -> Unit){
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = configuration.screenHeightDp.dp
    var expanded by remember { mutableStateOf(false) }
    var expandedByFocus by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf(false) }
    var staticSelectedText by remember { mutableStateOf(fieldValue ?: "") }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val menuHeight = with(density) {
        (screenHeightPx / 4).toPx().toDp()
    }
    val focusRequester = FocusRequester()
    val icon = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            readOnly = true,
            value = if (staticSelectedText == "" && fieldValue != null) fieldValue else staticSelectedText,
            onValueChange = {
                updateField(it)
                staticSelectedText = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    //This value is used to assign to the DropDown the same width
                    textFieldSize = coordinates.size.toSize()
                }
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.hasFocus && !expanded && !expandedByFocus) {
                        //Expand once when focus on field (clicking directly)
                        expanded = true
                        expandedByFocus = true
                    }
                }
                .testTag("${TestTag.DROPDOWN_MENU_PREFIX}$label"),
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    icon,
                    "contentDescription",
                    Modifier.clickable {
                        if (!dismissed) {
                            expanded = !expanded
                        } else if (!expanded) {
                            // Clickable is not called when readOnly (allowCustomValue = false)
                            expanded = true
                            dismissed = false //Reset, conflict event with closing + dismiss menu
                        } else {
                            dismissed = false
                        }
                    }
                )
            },
            colors = textFieldColors()
        )

        val textFieldHeightDp: Dp = with(density) { textFieldSize.height.toDp() }
        val textFieldWidthDp: Dp = with(density) { textFieldSize.width.toDp() }

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                dismissed = true
            },
            offset = DpOffset(x = 0.dp, y = -menuHeight - textFieldHeightDp),
            modifier = Modifier
                .width(textFieldWidthDp)
                .height(menuHeight),
            properties = PopupProperties(focusable = true)
        ) {
            options.forEach { label ->
                DropdownMenuItem(
                    text = {
                        Text(text = label)
                    },
                    onClick = {
                        updateField(label)
                        staticSelectedText = label
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DropdownMenuEditable(label: String, options: List<String>, fieldValue: String?, updateField: (String) -> Unit){
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = configuration.screenHeightDp.dp
    var expanded by remember { mutableStateOf(false) }
    var expandedByFocus by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(fieldValue ?: "") }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val menuHeight = with(density) {
        (screenHeightPx / 4).toPx().toDp()
    }
    val focusRequester = FocusRequester()
    val icon = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = if (selectedText == "" && fieldValue != null) fieldValue else selectedText,
            onValueChange = {
                updateField(it)
                selectedText = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    //This value is used to assign to the DropDown the same width
                    textFieldSize = coordinates.size.toSize()
                }
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.hasFocus && !expanded && !expandedByFocus) {
                        //Expand once when focus on field (clicking directly)
                        expanded = true
                        expandedByFocus = true
                    } else if (!it.hasFocus && expanded) {
                        expanded = false
                        expandedByFocus = true
                    }
                }
                .testTag("${TestTag.DROPDOWN_MENU_PREFIX}$label"),
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    icon,
                    "contentDescription",
                    Modifier.clickable {
                        if (!dismissed) {
                            expanded = !expanded
                            if (expanded) {
                                focusRequester.requestFocus()
                            }
                        } else {
                            dismissed = false
                        }
                    }
                )
            },
            colors = textFieldColors()
        )

        val textFieldHeightDp: Dp = with(density) { textFieldSize.height.toDp() }
        val textFieldWidthDp: Dp = with(density) { textFieldSize.width.toDp() }

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                dismissed = true
            },
            offset = DpOffset(x = 0.dp, y = -textFieldHeightDp),
            modifier = Modifier
                .width(textFieldWidthDp)
                .heightIn(max = menuHeight),
            properties = PopupProperties(focusable = false)
        ) {
            options.forEach { label ->
                DropdownMenuItem(
                    text = {
                        Text(text = label)
                    },
                    onClick = {
                        updateField(label)
                        selectedText = label
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun textFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedLabelColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedBorderColor = Color.LightGray,
        unfocusedBorderColor = Color.LightGray
    )
}