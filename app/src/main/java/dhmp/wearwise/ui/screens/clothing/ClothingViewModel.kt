package dhmp.wearwise.ui.screens.clothing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.model.Garment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

class ClothingViewModel(private val garmentRepository: GarmentsRepository): ViewModel() {
    private val tag: String = "ClothingViewModel"
    private val _uiState = MutableStateFlow(ClothingUIState())
    val uiState: StateFlow<ClothingUIState> = _uiState.asStateFlow()
    var showMenu by mutableStateOf(false)
    var showBrandFilterMenu by mutableStateOf(false)

    init {
        reset()
    }

    fun reset() {
        _uiState.value = ClothingUIState()
    }

    /**
     * This function is used to get all the garments from the database
     * - viewModelScope.launch is used to launch a coroutine within the viewModel lifecycle.
     * - repository.getAllGarmentsStream() is used to get all the garments from the database.
     * - flowOn(Dispatchers.IO) is used to change the dispatcher of the flow to IO, which is optimal for IO operations, and does not block the main thread.
     * - each time the flow emits a new value, the collect function will be called with the list of books.
     */
    fun collectGarments() {
        viewModelScope.launch {
            garmentRepository.getAllGarmentsStream().flowOn(Dispatchers.IO).collect { c ->
                _uiState.update { currentState ->
                    currentState.copy(
                        garments = c
                    )
                }
            }
            garmentRepository.insertGarment(Garment())
        }
    }

    fun saveImage(appDir: File, image: Bitmap, rotation: Float) {
        val now = System.currentTimeMillis()
        val newFile = File(appDir, "GarmentImages").let {
            it.mkdirs()
            File(it, "Garment_$now.png")
        }
        if(!newFile.createNewFile()) {
            Log.e(tag, "Error creating new file to store image")
        }

        val matrix = Matrix()
        matrix.postRotate(rotation)
        val rotatedBitmap = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)

        val inputImage = InputImage.fromBitmap(rotatedBitmap, 0)
        val options = SubjectSegmenterOptions.Builder()
            .enableForegroundConfidenceMask()
            .enableForegroundBitmap()
            .build()
        val segmenter = SubjectSegmentation.getClient(options)
        segmenter.process(inputImage)
            .addOnSuccessListener { result ->
//                val colors = IntArray(inputImage.width * inputImage.height)
//
//                val foregroundMask = result.foregroundConfidenceMask
//                for (i in 0 until inputImage.width * inputImage.height) {
//                    if ((foregroundMask?.get(i) ?: 0f) > 0.5f) {
//                        colors[i] = Color.argb(128, 255, 0, 255)
//                    }
//                }
//                val maskedBitmap = Bitmap.createBitmap(
//                    colors, inputImage.width, inputImage.height, Bitmap.Config.ARGB_8888
//                )

//                val resultBitmap = Bitmap.createBitmap(maskedBitmap.width, maskedBitmap.height, maskedBitmap.config)
//                val canvas = Canvas(resultBitmap)
//                canvas.drawBitmap(maskedBitmap, 0f, 0f, null)
//                canvas.drawBitmap(rotatedBitmap, 0f, 0f, null)

                val resultBitmap = result.foregroundBitmap
                val outputStream = ByteArrayOutputStream()
                resultBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                newFile.writeBytes(outputStream.toByteArray())
                val uri = newFile.toUri().toString()
                viewModelScope.launch {
                    val id = garmentRepository.insertGarment(Garment(image = uri))
                    _uiState.update { currentState ->
                        currentState.copy(
                            newItemId = id
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Failed to extract information using ML Kit on image: ${e.message}")
            }
    }

}