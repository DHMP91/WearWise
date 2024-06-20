package dhmp.wearwise.ui.screens.clothing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import dhmp.wearwise.data.CategoriesRepository
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.OutfitsRepository
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.Outfit
import dhmp.wearwise.model.nearestColorMatchList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.internal.closeQuietly
import java.io.File
import kotlin.reflect.KMutableProperty0


class ClothingViewModel(
    private val garmentRepository: GarmentsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val outfitsRepository: OutfitsRepository,
): ViewModel() {
    private val tag: String = "ClothingViewModel"
    private val _uiState = MutableStateFlow(ClothingUIState())
    val uiState: StateFlow<ClothingUIState> = _uiState.asStateFlow()

    private val _uiEditState = MutableStateFlow(EditClothingUIState())
    val uiEditState: StateFlow<EditClothingUIState> = _uiEditState.asStateFlow()

    private val _categories = MutableStateFlow(listOf<Category>())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isProcessingBackground = MutableStateFlow(false)
    val isProcessingBackground: StateFlow<Boolean> = _isProcessingBackground

    var showMenu by mutableStateOf(false)
    var showBrandFilterMenu by mutableStateOf(false)
    var selectedHatsId by mutableLongStateOf(-1)
    var selectedIntimatesId by mutableLongStateOf(-1)
    var selectedTopsId by mutableLongStateOf(-1)
    var selectedBottomsId by mutableLongStateOf(-1)
    var selectedOnePieceId by mutableLongStateOf(-1)
    var selectedFootwearId by mutableLongStateOf(-1)
    var SelectedOuterWearId by mutableLongStateOf(-1)
    var selectedAccessoriesId by mutableLongStateOf(-1)
    var selectedOtherId by mutableLongStateOf(-1)
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
        }
    }

    fun getGarmentById(id: Long) {
        viewModelScope.launch {
            garmentRepository.getGarmentStream(id).flowOn(Dispatchers.IO).collect { c ->
                c?.let {
                    _uiEditState.update { currentState ->
                        currentState.copy(
                            editGarment = it
                        )
                    }
                }
            }
        }
    }

    fun storeChanges(garment: Garment?) {
        _uiEditState.update {
            current ->
            current.copy(
                changes = garment
            )
        }
    }

    fun saveImage(appDir: File, image:Bitmap, rotation: Float): Job {
        val job = viewModelScope.launch(Dispatchers.IO) {
            val matrix = Matrix()
            matrix.postRotate(rotation)
            val rotatedBitmap = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
            rotatedBitmap.let {
                val uri = garmentRepository.saveImageToStorage(appDir, it)
                val id = garmentRepository.insertGarment(Garment(image = uri.toString()))
                _uiState.update { currentState ->
                    currentState.copy(
                        newItemId = id
                    )
                }
            }
        }
        return job
    }

    fun saveChanges(garment: Garment){
        viewModelScope.launch(Dispatchers.IO) {
            garmentRepository.updateGarment(garment)
            storeChanges(null)
        }
    }

    fun outfitPiecesIdVariables(): List<KMutableProperty0<Long>>{
        return listOf(
            this::selectedHatsId,
            this::selectedTopsId,
            this::selectedBottomsId,
            this::selectedOnePieceId,
            this::selectedFootwearId,
            this::SelectedOuterWearId,
            this::selectedAccessoriesId,
            this::selectedOtherId,
            this::selectedIntimatesId,
        )
    }

    fun buildOutfit(){
        val newOutfit = Outfit()
        val categoryToModelVarMap = outfitPiecesIdVariables()
        categoryToModelVarMap.forEach{
            val id = it.get()
            if(id > 0){
                newOutfit.garmentsId = newOutfit.garmentsId.plus(id)
            }
        }
        if(newOutfit.garmentsId.isNotEmpty()) {
            viewModelScope.launch {
                outfitsRepository.insertOutfit(newOutfit)
            }
        }
    }

    fun analyzeGarment(id: Long){
        viewModelScope.launch {
            garmentRepository.getGarmentStream(id).flowOn(Dispatchers.IO).collect { garment ->
                garment?.let {
                    val image = it.imageOfSubject ?: it.image
                    val bitmap = BitmapFactory.decodeFile(Uri.parse(image).path!!)
                        Palette.Builder(bitmap).generate { it2 ->
                        it2?.let { palette ->
                            val color = palette.getDominantColor(0)
                            if (color != 0 ) {
                                // Convert Color to RGB components
                                val red = color.red
                                val green = color.green
                                val blue = color.blue
                                val hex = String.format("#%02X%02X%02X", red, green, blue)
                                val colorName = getNearestColorName(color)
                                it.color = colorName
                                storeChanges(it)
                            }else{
                                Log.w(tag, "Could not determine dominant color from image")
                            }
                        }
                    }
                }
            }
        }
    }

    fun removeBackGround(id: Long, path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isProcessingBackground.value = true
            val file = File(path)
            val image = BitmapFactory.decodeFile(path)
            val inputImage = InputImage.fromBitmap(image, 0)
            val options = SubjectSegmenterOptions.Builder()
                .enableForegroundConfidenceMask()
                .enableForegroundBitmap()
                .build()

            // Blurr background
            val finalImage = Bitmap.createBitmap(image.width, image.height, image.config)
            val canvas = Canvas(finalImage)
            val paint = Paint()
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(1f)
            val colorFilter = ColorMatrixColorFilter(colorMatrix)
            paint.colorFilter = colorFilter
            canvas.drawBitmap(image, 0f, 0f, paint)

            val shrinkImage = Bitmap.createScaledBitmap(finalImage, 30, 30, false)
            val blurredImage = Bitmap.createScaledBitmap(shrinkImage, image.width, image.height, true)
            shrinkImage.recycle()

            val segmenter = SubjectSegmentation.getClient(options)
            val job = segmenter.process(inputImage)
                .addOnSuccessListener { result ->
                    val colors = IntArray(image.width * image.height)
                    val resultBitmap = result.foregroundBitmap //subject of interest
                    viewModelScope.launch {
                        resultBitmap?.let {
                            val subjectUri = garmentRepository.saveImageToStorage(
                                file.parentFile!!,
                                resultBitmap
                            )
                            canvas.drawBitmap(blurredImage, 0f, 0f, null)
                            blurredImage.recycle()
                            canvas.drawBitmap(it, 0f, 0f, null) // Add subject to grayed background
                            val uri =
                                garmentRepository.saveImageToStorage(file.parentFile!!, finalImage)
                            it.recycle()
                            garmentRepository.getGarmentStream(id).flowOn(Dispatchers.IO)
                                .collect { c ->
                                    c?.let {
                                        c.image = uri.toString()
                                        c.imageOfSubject = subjectUri.toString()
                                        garmentRepository.updateGarment(c)
                                        _isProcessingBackground.value = false
                                        segmenter.closeQuietly()
                                    }
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Failed to extract information using ML Kit on image: ${e.message}")
                    _isProcessingBackground.value = false
                    segmenter.closeQuietly()
                }
        }
    }

    fun deleteGarment(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            garmentRepository.getGarmentStream(id).flowOn(Dispatchers.IO).collect{ garment ->
                garment?.let {
                    it.image?.let { uri ->
                        val deleted = Uri.parse(uri).path?.let { path ->
                            try{
                                File(path).delete()
                            }catch (exception: SecurityException){
                                Log.e(tag, "No permission to delete file")
                                false
                            }
                        }
                        when(deleted){
                            true ->  garmentRepository.deleteGarment(it)
                            else -> Log.e(tag, "Could not delete the file image")
                        }
                    }

                }
            }
        }
    }

    fun collectCategories(){
        viewModelScope.launch(Dispatchers.IO) {
            categoriesRepository.getAllCategoriesStream().flowOn(Dispatchers.IO).collect { c ->
                _categories.update { c }
            }
        }
    }
    private fun getNearestColorName(color: Int): String {
        var nearestColorName = "Unknown"
        var minDistance = Double.MAX_VALUE

        nearestColorMatchList.forEach { colorName ->
            val distance = colorDistance(color, colorName.color)
            if (distance < minDistance) {
                minDistance = distance
                nearestColorName = colorName.name
            }
        }
        return nearestColorName
    }

    private fun colorDistance(color1: Int, color2: Int): Double {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)

        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)

        return Math.sqrt(
            ((r1 - r2) * (r1 - r2) +
                    (g1 - g2) * (g1 - g2) +
                    (b1 - b2) * (b1 - b2)).toDouble()
        )
    }


}