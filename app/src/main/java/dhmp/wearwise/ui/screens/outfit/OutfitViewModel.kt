package dhmp.wearwise.ui.screens.outfit

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.OutfitsRepository
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.Outfit
import dhmp.wearwise.model.Season
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val ITEMS_PER_PAGE = 3

private const val tag = "OutfitViewModel"

class OutfitViewModel (
    private val garmentRepository: GarmentsRepository,
    private val outfitsRepository: OutfitsRepository,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO
): ViewModel() {
    val outfitUri: MutableStateFlow<Uri?> = MutableStateFlow(null)

    private val _outfit: MutableStateFlow<Outfit?> = MutableStateFlow(null)
    val outfit: StateFlow<Outfit?> = _outfit.asStateFlow()

    val savedOutfitFlag = MutableStateFlow(true)

    val outfits: Flow<PagingData<Outfit>> =
        Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
            pagingSourceFactory = { outfitsRepository.getAllOutfitsPaged() }
        )
            .flow
            .cachedIn(viewModelScope)

    private val exceptionHandler =  CoroutineExceptionHandler { _, exception ->
        Log.e(tag, "Unhandled Exception: ${exception.localizedMessage}")
    }

    fun getOutfitsByListOfId(outfitIds: List<Long>): Flow<PagingData<Outfit>> {
        val data =  Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
            pagingSourceFactory = { outfitsRepository.getOutfitsByListOfIdsPaged(outfitIds) }
        ).flow.cachedIn(viewModelScope)

        return data
    }

    fun getGarments(outfit: Outfit): Flow<List<Garment>> = flow {
        val garments = mutableListOf<Garment>()
        outfit.garmentsId.forEach { id ->
            val garment = garmentRepository.getGarmentStream(id)
                .flowOn(dispatcherIO)
                .first()
            garment?.let {
                garments.add(it) // Emit a new copy of the list each time an item is added
            }
        }
        emit(garments)
    }.flowOn(dispatcherIO)


    fun getOutfitThumbnail(outfit: Outfit): Flow<String> = flow {
        val thumbnail = outfitsRepository.getOutfitThumbnail(outfit)
        if (thumbnail.isNullOrEmpty() ){
            outfit.image?.let {
                emit(it)
            }
        } else{
            emit(thumbnail)
        }
    }.flowOn(dispatcherIO)


    fun saveImage(appDir: File, image: Bitmap, rotation: Float, id: Long?): Job {
        outfitUri.value = Uri.EMPTY // Let UI continue to next screen while processing image
        val job = viewModelScope.launch(dispatcherIO + exceptionHandler) {
            if(!(id == null || id == 0L)) {

                // Let UI know image is processing
                val outfit = outfitsRepository.getOutfitStream(id).first()

                outfit?.let {
                    val outfitImage = it.image
                    it.image = "PROCESSING"
                    outfitsRepository.updateOutfit(it)

                    if(!outfitImage.isNullOrEmpty()){
                        val imageUri = Uri.parse(outfitImage)
                        imageUri.path?.let { path ->
                            deleteFile(path)
                        }
                    }
                }

                val matrix = Matrix()
                matrix.postRotate(rotation)
                val rotatedBitmap =
                    Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
                rotatedBitmap.let {
                    val uri = garmentRepository.saveImageToStorage(appDir, it)
                    outfitUri.value = uri
                    saveImageToOutfit(id)
                }
            }
        }
        return job
    }

    fun newOutfit(){
        _outfit.update {
            Outfit()
        }
        savedOutfitFlag.update {
            false
        }
    }

    suspend fun saveOutfit(): Long? {
        return _outfit.value?.let {
            saveOutfit(it)
        }
    }

    fun deleteOutfit(){
        viewModelScope.launch(dispatcherIO + exceptionHandler) {
            _outfit.value?.let {
                deleteOutfit(it)
                _outfit.update {
                    null
                }
            }
        }
    }

    fun deleteOutfit(id: Long){
        viewModelScope.launch(dispatcherIO + exceptionHandler) {
            outfitsRepository.getOutfitStream(id).flowOn(dispatcherIO).collect { outfit ->
                outfit?.let {
                    deleteOutfit(it)
                }
            }
        }
    }


    fun getOutfit(id: Long) {
        viewModelScope.launch(dispatcherIO + exceptionHandler) {
            outfitsRepository.getOutfitStream(id).flowOn(dispatcherIO).collect { outfit ->
                outfit?.let {
                    _outfit.update {
                        outfit.copy()
                    }
                }
            }
        }
    }

    fun addToOutfit(garment: Garment){
        viewModelScope.launch(dispatcherIO + exceptionHandler) {
            _outfit.value?.let { outfit ->
                if(!outfit.garmentsId.contains(garment.id)) {
                    _outfit.update {
                        outfit.copy(
                            garmentsId = outfit.garmentsId.plus(garment.id)
                        )
                    }
                    savedOutfitFlag.update {
                        false
                    }
                }
            }
        }
    }

    fun removeFromOutfit(garment: Garment){
        viewModelScope.launch(dispatcherIO + exceptionHandler) {
            _outfit.value?.let { outfit ->
                if(outfit.garmentsId.contains(garment.id)) {
                    _outfit.update {
                        outfit.copy(
                            garmentsId = outfit.garmentsId.minus(garment.id)
                        )
                    }
                    savedOutfitFlag.update {
                        false
                    }
                }
            }
        }
    }

    fun setSeason(season: Season){
        viewModelScope.launch(dispatcherIO + exceptionHandler) {
            _outfit.value?.let { it ->
                it.season = season
            }
            savedOutfitFlag.update {
                false
            }
        }
    }

    suspend fun saveOutfit(outfit: Outfit): Long {
        return withContext(dispatcherIO + exceptionHandler) {
            val id = if (outfit.id == 0L) {
                val newId = outfitsRepository.insertOutfit(outfit)

                //insert outfit id into each garment
                outfit.garmentsId.forEach { garmentId ->
                    val garment = garmentRepository.getGarmentStream(garmentId).first()
                    garment?.let{
                        it.outfitsId = it.outfitsId.plus(newId)
                        garmentRepository.updateGarment(it)
                    }
                }
                getOutfit(newId)
                newId
            } else {
                val oldOutfit = outfitsRepository.getOutfitStream(outfit.id).first()
                val removedGarmentsId = oldOutfit?.garmentsId?.filter { !outfit.garmentsId.contains(it) } ?: listOf()
                removedGarmentsId.forEach { garmentId ->
                    val garment = garmentRepository.getGarmentStream(garmentId).first()
                    garment?.let{
                        it.outfitsId = it.outfitsId.minus(outfit.id)
                        garmentRepository.updateGarment(it)
                    }
                }
                outfitsRepository.updateOutfit(outfit)
                outfit.garmentsId.forEach { garmentId ->
                    val garment = garmentRepository.getGarmentStream(garmentId).first()
                    garment?.let{
                        if(!it.outfitsId.contains(outfit.id)) {
                            it.outfitsId = it.outfitsId.plus(outfit.id)
                        }
                        garmentRepository.updateGarment(it)
                    }
                }
                outfit.id
            }
            savedOutfitFlag.update { true }
            id
        }
    }


    private fun saveImageToOutfit(outfitId: Long) {
        viewModelScope.launch(dispatcherIO + exceptionHandler) {
            val outfit = outfitsRepository.getOutfitStream(outfitId).first()
            outfit?.let {
                it.image = outfitUri.value.toString()
                outfitsRepository.updateOutfit(it)
            }
        }
    }

    private fun deleteFile(uri: String){
        Uri.parse(uri).path?.let { path ->
            try{
                File(path).delete()
            }catch (exception: SecurityException){
                Log.e(tag, "No permission to delete file")
                false
            }
        }
    }

    private suspend fun deleteOutfit(outfit: Outfit){
        outfitsRepository.deleteOutfit(outfit)
        outfit.garmentsId.forEach { garmentId ->
            val garment = garmentRepository.getGarmentStream(garmentId).first()
            garment?.let{ g ->
                g.outfitsId = g.outfitsId.minus(outfit.id)
                garmentRepository.updateGarment(g)
            }
        }
        outfit.image?.let { imageUri ->
            Uri.parse(imageUri).path?.let { path ->
                deleteFile(path)
            }
        }
    }
}