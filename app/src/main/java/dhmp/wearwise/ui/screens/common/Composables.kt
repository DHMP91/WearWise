package dhmp.wearwise.ui.screens.common

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
//    val tabColor = MaterialTheme.colorScheme.onBackground
    val bottomBorderModifier =
        Modifier
//            .drawBehind {
//                val strokeWidth = 2.dp.toPx()
//                val y = size.height - strokeWidth / 2
//                drawLine(
//                    color = tabColor,
//                    start = Offset(0f, y),
//                    end = Offset(size.width, y),
//                    strokeWidth = strokeWidth
//                )
//            }
            .padding(bottom = 10.dp)
            .testTag(TestTag.SCREEN_TITLE)

    Text(
        text = text,
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        maxLines = 1,
        modifier = bottomBorderModifier
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