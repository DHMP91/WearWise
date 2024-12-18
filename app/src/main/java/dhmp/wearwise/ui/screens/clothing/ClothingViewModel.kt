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
import dhmp.wearwise.data.AIRepository
import dhmp.wearwise.data.AIRepositoryProvider
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.UserConfigRepository
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.NearestColorMatchList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
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
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt


class ClothingViewModel(
    private val garmentRepository: GarmentsRepository,
    private val userConfigRepository: UserConfigRepository,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val aiRepositoryProvider: AIRepositoryProvider = AIRepositoryProvider(),
): ViewModel() {

    val analyzing = MutableStateFlow(false)

    private val _newItemId = MutableStateFlow(0L)
    val newItemId: StateFlow<Long> = _newItemId.asStateFlow()

    private val _uiEditState = MutableStateFlow(EditClothingUIState())
    val uiEditState: StateFlow<EditClothingUIState> = _uiEditState.asStateFlow()

    private val _uiMenuState = MutableStateFlow(ClothingMenuUIState())
    val uiMenuState: StateFlow<ClothingMenuUIState> = _uiMenuState.asStateFlow()

    private val pageSize = 10
    private val tag: String = "ClothingViewModel"
    private val exceptionHandler =  CoroutineExceptionHandler { _, exception ->
        Log.e(tag, "Unhandled Exception: ${exception.localizedMessage}")
    }



    @OptIn(ExperimentalCoroutinesApi::class)
    val garments: Flow<PagingData<Garment>> = combine(
        _uiMenuState
    ) { menuState ->
        val filterExcludeCategories = menuState.last().filterExcludeCategories
        val filterExcludeBrands = menuState.last().filterExcludeBrands
        val filterExcludeColors = menuState.last().filterExcludeColors
        Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
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
    }.flattenMerge()

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

    fun removeBrandFromFilter(brands: List<String>){
        _uiMenuState.update {
                current ->
            current.copy(
                filterExcludeBrands = current.filterExcludeBrands.toSet().minus(brands).toList()
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

    fun addBrandToFilter(brands: List<String>){
        _uiMenuState.update {
                current ->
            current.copy(
                filterExcludeBrands = current.filterExcludeBrands.toSet().plus(brands).toList()
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

    fun removeCategoryFromFilter(categories: List<Category>){
        _uiMenuState.update {
                current ->
            current.copy(
                filterExcludeCategories = current.filterExcludeCategories.toSet().minus(categories).toList()
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

    fun addCategoryToFilter(categories: List<Category>){
        _uiMenuState.update {
                current ->
            current.copy(
                filterExcludeCategories = current.filterExcludeCategories.toSet().plus(categories).toList()
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

    fun removeColorFromFilter(colors: List<String>){
        _uiMenuState.update {
                current ->
            current.copy(
                filterExcludeColors = current.filterExcludeColors.toSet().minus(colors).toList()
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

    fun addColorToFilter(colors: List<String>){
        _uiMenuState.update {
                current ->
            current.copy(
                filterExcludeColors = current.filterExcludeColors.toSet().plus(colors).toList()
            )
        }
    }

    fun getGarmentById(id: Long) {
        viewModelScope.launch(dispatcherIO + exceptionHandler) {
            garmentRepository.getGarmentStream(id).flowOn(dispatcherIO).collect { c ->
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
                config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
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
    }.flowOn(dispatcherIO)

    fun saveImage(appDir: File, image:Bitmap, rotation: Float): Job {
        val job = viewModelScope.launch(dispatcherIO + exceptionHandler) {
            val garmentId = garmentRepository.insertGarment(Garment())
            _newItemId.emit(garmentId)

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
        viewModelScope.launch(dispatcherIO + exceptionHandler) {
            garmentRepository.updateGarment(garment)
            storeChanges(null)
        }
    }

    fun analyzeGarment(id: Long){
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val userConfig = userConfigRepository.getUserConfigStream().flowOn(dispatcherIO).firstOrNull()
            analyzing.update { true }
            if(userConfig != null){
                val model: AIRepository? = aiRepositoryProvider.getRepository(userConfig)
                if (model != null) {
                    try {
                        analyzeByAI(id, model)
                    } catch (e: Exception) {
                        Log.e(tag, e.toString())
                        dumbAnalyze(id)
                    }
                }
            }else{
                dumbAnalyze(id)
            }
            analyzing.update { false }
        }
    }

    fun removeBackGround(id: Long, path: String) {
        viewModelScope.launch(dispatcherIO + exceptionHandler) {
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
                    viewModelScope.launch(Dispatchers.Default) {
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
                            val garment = garmentRepository.getGarmentStream(id).flowOn(dispatcherIO).firstOrNull()
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
        viewModelScope.launch(dispatcherIO + exceptionHandler) {
            val garment = garmentRepository.getGarmentStream(id).flowOn(dispatcherIO).firstOrNull()
            garment?.let {
                val deleteUri = { uri: String ->
                    Uri.parse(uri).path?.let { path ->
                        try{
                            val deleted = File(path).delete()
                            if (deleted) {
                            } else {
                                Log.e(tag, "Failed to delete the file $path")
                            }
                        }catch (exception: SecurityException){
                            Log.e(tag, "No permission to delete file")
                        }
                    }
                }
                it.image?.let { uri ->
                    deleteUri(uri)
                }

                it.imageOfSubject?.let { uri ->
                    deleteUri(uri)
                }
                garmentRepository.deleteGarment(it)
            }
        }
    }

    fun collectBrands(){
        viewModelScope.launch(dispatcherIO + exceptionHandler) {
             garmentRepository
                 .getBrands()
                 .flowOn(dispatcherIO)
                 .collect{
                     _brands.emit(it)
                 }
        }
    }

    private fun getNearestColorName(color: Int): String {
        var nearestColorName = "Unknown"
        var minDistance = Double.MAX_VALUE

        NearestColorMatchList.forEach { colorName ->
            val distance = colorDistance(color, colorName.color)
            if (distance < minDistance) {
                minDistance = distance
                nearestColorName = colorName.name
            }
        }
        return nearestColorName
    }

    private fun colorDistance(color1: Int, color2: Int): Double {
        val c1 = FloatArray(3)
        val c2 = FloatArray(3)

        Color.colorToHSV(color1, c1)
        Color.colorToHSV(color2, c2)

        val dh = min(abs(c1[0] - c2[0]), 360 - abs(c1[0] - c2[0])) / 180.0 // normalize hue to [0, 2]
        val ds = abs(c1[1] - c2[1]) // saturation difference
        val dv = abs(c1[2] - c2[2]) // value (brightness) difference

        // Applying weights
        val hueWeight = 6.0 // Significantly increase hue weight
        val saturationWeight = 2.0
        val valueWeight = 0.5 // Further reduce value weight

        return sqrt(hueWeight * dh * dh + saturationWeight * ds * ds + valueWeight * dv * dv)
    }

    private fun reset() {
        _uiMenuState.value = ClothingMenuUIState()
    }

    private suspend fun dumbAnalyze(id: Long){
        val garment = garmentRepository.getGarmentStream(id).flowOn(dispatcherIO).firstOrNull()
        garment?.let {
            val image = it.imageOfSubject ?: it.image
            val bitmap = BitmapFactory.decodeFile(Uri.parse(image).path!!)
            Palette.Builder(bitmap).generate { it2 ->
                it2?.let { palette ->
                    val color = palette.getDominantColor(0)
                    if (color != 0 ) {
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

    private suspend fun analyzeByAI(id: Long, model: AIRepository){
        val garment = garmentRepository.getGarmentStream(id).flowOn(dispatcherIO).firstOrNull()
        garment?.let {
            val image = it.imageOfSubject ?: it.image
            image?.let { img ->
                val bitmap = BitmapFactory.decodeFile(Uri.parse(img).path!!)
                model.garmentCategory(bitmap)?.let { c -> it.categoryId = c.id }
                model.garmentColor(bitmap)?.let { c -> it.color = c.name }
                model.garmentBrand(bitmap)?.let { b -> it.brand = b }
                model.garmentOccasion(bitmap)?.let { o -> it.occasion = o }
                it.categoryId?.let { id ->
                    model.garmentSubCategory(bitmap, id)?.let { c ->
                        it.subCategoryId = c.id
                    }
                }
                storeChanges(it)
            }
        }
    }

}