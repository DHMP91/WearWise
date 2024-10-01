package dhmp.wearwise.ui.screens.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import dhmp.wearwise.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import okhttp3.internal.closeQuietly
import java.io.File
import java.util.concurrent.Executor


private val TAG = "CameraX Screen"


@Composable
fun ImageScreen(saveImage: (File, Bitmap, Float) -> Job, onBack: () -> Unit) {
    val func = { file: File, bitmap: Bitmap, float: Float, _: Long? ->
        saveImage(file, bitmap, float)
    }
    ImageScreen(func, id = 0, onBack)
}

@Composable
fun ImageScreen(saveImage: (File, Bitmap, Float, Long?) -> Job, id: Long = 0, onBack: () -> Unit) {
    var imageMethod by remember { mutableStateOf("") }
    var backPressHandled by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    BackHandler(enabled = !backPressHandled) {
        println("back pressed")
        backPressHandled = true
        coroutineScope.launch(Dispatchers.Main) {
            awaitFrame()
            onBack()
            backPressHandled = false
        }
    }

    when(imageMethod){
        "camera" -> { Camera(saveImage = saveImage, id = id)}
        "imagePicker" -> { ExternalPhotoSelector(saveImage = saveImage, id = id, onBack = onBack)}
        else -> {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.5f)
                        .padding(dimensionResource(id = R.dimen.screen_title_padding)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    ScreenTitle(
                        "Choose Source"
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(10.dp)
                            .clickable { imageMethod = "camera" }
                            .testTag(TestTag.USE_CAMERA_SELECTION),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_camera_24),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .heightIn(max = 120.dp)
                            )
                            Text(
                                "Use Camera To Take Photo",
                                overflow = TextOverflow.Visible,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(10.dp)
                            .clickable { imageMethod = "imagePicker" }
                            .testTag(TestTag.USE_GALLERY_SELECTION),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountBox,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .heightIn(max = 120.dp)
                            )
                            Text(
                                "Import Photo from External App",
                                overflow = TextOverflow.Visible,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.5f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {}
            }
        }
    }
}



@Composable
fun ExternalPhotoSelector(saveImage: (File, Bitmap, Float, Long?) -> Job, id: Long = 0, onBack: () -> Unit){
    val context = LocalContext.current
    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                if(uri != null) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val imageBitMap = BitmapFactory.decodeStream(inputStream)
                    saveImage(context.filesDir, imageBitMap, 0f, id)
                    inputStream?.closeQuietly()
                }else {
                    onBack()
                }
            }
        )
    LaunchedEffect(null) {
        galleryLauncher.launch("image/*")
    }
}

@Composable
fun Camera(saveImage: (File, Bitmap, Float, Long?) -> Job, id: Long = 0){
    val context = LocalContext.current
    val permission = Manifest.permission.CAMERA
    var hasPermission by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Camera Permission Granted")
            hasPermission = true
        } else {
            Log.d(TAG, "Camera Permission Denied")
        }
    }

    LaunchedEffect(permission) {
        val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else {
            launcher.launch(permission)
        }
    }

    if (hasPermission) {
        CameraView(saveImage, id)
    }else{
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.screen_title_padding)),
            verticalArrangement = Arrangement.Center
        ){
            Text(" :( No Camera Permission")
            Text("Go to app setting and allow Camera permission")
        }
    }
}

@Composable
fun CameraView(saveImage: (File, Bitmap, Float, Long?) -> Job, id: Long = 0) {
    val context = LocalContext.current
    val previewView: PreviewView = remember { PreviewView(context) }
    val cameraController = remember { LifecycleCameraController(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    var isCameraInitialized by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(cameraController, lifecycleOwner) {
        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraController.imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
        cameraController.setZoomRatio(0f)
        cameraController.bindToLifecycle(lifecycleOwner)
        previewView.controller = cameraController
        previewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        previewView.scaleType = PreviewView.ScaleType.FIT_CENTER

        previewView.previewStreamState.observe(lifecycleOwner) { streamState ->
            isCameraInitialized = streamState == PreviewView.StreamState.STREAMING
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        IconButton(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp),
            onClick = {
                if (!loading) {
                    loading = true
                    savePhoto(context, cameraController, saveImage, id)
                }
            }
        ) {
            if (!loading && isCameraInitialized) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_camera_24),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag(TestTag.CAMERA_TAKE_ICON)
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }

}


private fun savePhoto(context: Context, cameraController: LifecycleCameraController, saveImage: (File, Bitmap, Float, Long?) -> Job, id: Long){
    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)
    val appDir: File = context.filesDir
    cameraController.takePicture(
        mainExecutor,
        object: ImageCapture.OnImageCapturedCallback() {
            @OptIn(ExperimentalGetImage::class)
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                saveImage(appDir, imageProxy.toBitmap(), imageProxy.imageInfo.rotationDegrees.toFloat(), id)
                imageProxy.close()
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Error on capturing image")
                super.onError(exception)
            }
        }
    )
}