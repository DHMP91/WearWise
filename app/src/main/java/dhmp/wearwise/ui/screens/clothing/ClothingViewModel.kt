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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.palette.graphics.Palette
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.nearestColorMatchList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.internal.closeQuietly
import java.io.File

private const val ITEMS_PER_PAGE = 10
class ClothingViewModel(
    private val garmentRepository: GarmentsRepository,
): ViewModel() {
    private val tag: String = "ClothingViewModel"

    private val _uiState = MutableStateFlow(ClothingUIState())
    val uiState: StateFlow<ClothingUIState> = _uiState.asStateFlow()

    private val _uiEditState = MutableStateFlow(EditClothingUIState())
    val uiEditState: StateFlow<EditClothingUIState> = _uiEditState.asStateFlow()

    private val _uiMenuState = MutableStateFlow(ClothingMenuUIState())
    val uiMenuState: StateFlow<ClothingMenuUIState> = _uiMenuState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val garments: Flow<PagingData<Garment>> = combine(
        _uiMenuState
    ) { menuState ->
        val filterExcludeCategories = menuState.last().filterExcludeCategories
        val filterExcludeBrands = menuState.last().filterExcludeBrands
        val filterExcludeColors = menuState.last().filterExcludeColors
        Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
            pagingSourceFactory = {
                garmentRepository.getFilteredGarments(
                    excludedCategories = filterExcludeCategories,
                    excludedColors = filterExcludeColors,
                    excludedBrands = filterExcludeBrands,
                )
//                garmentRepository.getAllGarmentsPaged()
            }
        )
            .flow
    }.flattenMerge().cachedIn(viewModelScope)

    private var currentGarmentCategoryFlows: MutableMap<Int, Flow<PagingData<Garment>>> = mutableMapOf()

    private val _brands = MutableStateFlow(listOf<String>())
    val brands: StateFlow<List<String>> = _brands.asStateFlow()

    private val _isProcessingBackground = MutableStateFlow(false)
    val isProcessingBackground: StateFlow<Boolean> = _isProcessingBackground

    init {
        reset()
    }

    fun showMenu(bool: Boolean) {
        _uiMenuState.update {
                current ->
            current.copy(
                showMenu = bool
            )
        }
    }

    fun showBrandFilterMenu(bool: Boolean) {
        _uiMenuState.update {
                current ->
            current.copy(
                showBrandFilterMenu = bool
            )
        }
    }

    fun showColorFilterMenu(bool: Boolean) {
        _uiMenuState.update {
                current ->
            current.copy(
                showColorFilterMenu = bool
            )
        }
    }

    fun showCategoryFilterMenu(bool: Boolean) {
        _uiMenuState.update {
                current ->
            current.copy(
                showCategoryFilterMenu = bool
            )
        }
    }

    fun removeBrandFromFilter(brand: String){
        _uiMenuState.update {
                current ->
            current.copy(
                filterExcludeBrands = current.filterExcludeBrands.minus(brand)
            )
        }
    }

    fun addBrandToFilter(brand: String){
        _uiMenuState.update {
                current ->
            current.copy(
                filterExcludeBrands = current.filterExcludeBrands.plus(brand)
            )
        }
    }


    fun removeCategoryFromFilter(category: Category){
        _uiMenuState.update {
                current ->
            current.copy(
                filterExcludeCategories = current.filterExcludeCategories.minus(category)
            )
        }
    }

    fun addCategoryToFilter(category: Category){
        _uiMenuState.update {
                current ->
            current.copy(
                filterExcludeCategories = current.filterExcludeCategories.plus(category)
            )
        }
    }

    fun removeColorFromFilter(color: String){
        _uiMenuState.update {
                current ->
            current.copy(
                filterExcludeColors = current.filterExcludeColors.minus(color)
            )
        }
    }

    fun addColorToFilter(color: String){
        _uiMenuState.update {
                current ->
            current.copy(
                filterExcludeColors = current.filterExcludeColors.plus(color)
            )
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

    fun getGarmentsByCategory(categoryId: Int?):  Flow<PagingData<Garment>> {
        val index = categoryId ?: -1
        val storedPager = currentGarmentCategoryFlows[index]
        if(storedPager != null){
            return storedPager
        }
        val pager =  Pager(
                config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
                pagingSourceFactory = { garmentRepository.getGarmentsByCategoryPaged(categoryId) }
            )
                .flow
                .cachedIn(viewModelScope)
        currentGarmentCategoryFlows[index] = pager
        return pager
    }

    fun getGarmentsCount(
        excludedCategories: List<Category>,
        excludedColors: List<String>,
        excludedBrands: List<String>
    ): Flow<Int> = garmentRepository.getGarmentsCount(
        excludedCategories = excludedCategories,
        excludedColors = excludedColors,
        excludedBrands = excludedBrands
    )

    fun storeChanges(garment: Garment?) {
        _uiEditState.update {
            current ->
            current.copy(
                changes = garment
            )
        }
    }

    fun getGarmentThumbnail(garment: Garment): Flow<String> = flow {
        val thumbnail = garmentRepository.getGarmentThumbnail(garment)
        if (thumbnail.isNullOrEmpty() ){
            garment.image?.let {
                emit(it)
            }
        } else{
            emit(thumbnail)
        }
    }.flowOn(Dispatchers.IO)

    fun saveImage(appDir: File, image:Bitmap, rotation: Float, id: Long?): Job {
        val job = viewModelScope.launch(Dispatchers.IO) {
            val garmentId = garmentRepository.insertGarment(Garment())
            _uiState.update { currentState ->
                currentState.copy(
                    newItemId = garmentId
                )
            }

            val matrix = Matrix()
            matrix.postRotate(rotation)
            val rotatedBitmap = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
            rotatedBitmap.let {
                val uri = garmentRepository.saveImageToStorage(appDir, it)
                garmentRepository.updateGarment(Garment(id = garmentId, image = uri.toString()))
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

    fun analyzeGarment(id: Long){
        viewModelScope.launch {
            val garment = garmentRepository.getGarmentStream(id).flowOn(Dispatchers.IO).firstOrNull()
            garment?.let {
                val image = it.imageOfSubject ?: it.image
                val bitmap = BitmapFactory.decodeFile(Uri.parse(image).path!!)
                    Palette.Builder(bitmap).generate { it2 ->
                    it2?.let { palette ->
                        val color = palette.getDominantColor(0)
                        if (color != 0 ) {
                            // Convert Color to RGB components
//                            val red = color.red
//                            val green = color.green
//                            val blue = color.blue
//                            val hex = String.format("#%02X%02X%02X", red, green, blue)
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
            segmenter.process(inputImage)
                .addOnSuccessListener { result ->
                    val resultBitmap = result.foregroundBitmap //subject of interest
                    viewModelScope.launch {
                        resultBitmap?.let {
                            val subjectUri = garmentRepository.saveImageToStorage(
                                file.parentFile!!.parentFile!!,
                                resultBitmap
                            )
                            canvas.drawBitmap(blurredImage, 0f, 0f, null)
                            blurredImage.recycle()
                            canvas.drawBitmap(it, 0f, 0f, null) // Add subject to grayed background
                            val uri =
                                garmentRepository.saveImageToStorage(file.parentFile!!.parentFile!!, finalImage)
                            it.recycle()
                            val garment = garmentRepository.getGarmentStream(id).flowOn(Dispatchers.IO).firstOrNull()
                            garment?.let { c ->
                                c.image = uri.toString()
                                c.imageOfSubject = subjectUri.toString()
                                garmentRepository.updateGarment(c)
                                _isProcessingBackground.value = false
                                segmenter.closeQuietly()
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
            val garment = garmentRepository.getGarmentStream(id).flowOn(Dispatchers.IO).firstOrNull()
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

    fun collectBrands(){
        viewModelScope.launch(Dispatchers.IO) {
             garmentRepository
                 .getBrands()
                 .flowOn(Dispatchers.IO)
                 .collect{
                     _brands.emit(it)
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

    private fun reset() {
        _uiState.value = ClothingUIState()
        _uiMenuState.value = ClothingMenuUIState()
    }

}