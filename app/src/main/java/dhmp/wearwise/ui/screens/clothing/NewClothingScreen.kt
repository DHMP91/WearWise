package dhmp.wearwise.ui.screens.clothing

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.R
import java.io.File
import java.util.concurrent.Executor

private val TAG = "NewClothingScreen"
@Composable
fun NewClothingScreen(
    onFinish: (Long) -> Unit,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by clothingViewModel.uiState.collectAsState()
    if (uiState.newItemId != 0L ){
        onFinish(uiState.newItemId)
    }
    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(it)
        ){
            CameraScreen(clothingViewModel::saveImage)
        }
    }
}

@Composable
fun CameraScreen(saveImage: (File, Bitmap, Float) -> Unit) {
    val context = LocalContext.current
    val previewView: PreviewView = remember { PreviewView(context) }
    val cameraController = remember { LifecycleCameraController(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    cameraController.bindToLifecycle(lifecycleOwner)
    cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    previewView.controller = cameraController
    var loading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        IconButton(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp),
            onClick = {
                if (!loading) {
                    loading = true
                    savePhoto(context, cameraController, saveImage)
                }
            }
        ) {
            if (!loading) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_camera_24),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(54.dp)
                )
            }else {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

private fun savePhoto(context: Context, cameraController: LifecycleCameraController, saveImage: (File, Bitmap, Float) -> Unit){
    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)
    val appDir: File = context.filesDir
    cameraController.takePicture(
        mainExecutor,
        object: ImageCapture.OnImageCapturedCallback() {
            @OptIn(ExperimentalGetImage::class)
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                saveImage(appDir, imageProxy.toBitmap(), imageProxy.imageInfo.rotationDegrees.toFloat())
                imageProxy.close()
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Error on capturing image")
                super.onError(exception)
            }
        }
    )
}