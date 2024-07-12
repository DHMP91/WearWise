package dhmp.wearwise.ui.screens.outfit

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
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

class OutfitViewModel (
    private val garmentRepository: GarmentsRepository,
    private val outfitsRepository: OutfitsRepository,
): ViewModel() {
    val outfitUri: MutableStateFlow<Uri?> = MutableStateFlow(null)

    private val _outfit: MutableStateFlow<Outfit?> = MutableStateFlow(null)
    val outfit: StateFlow<Outfit?> = _outfit.asStateFlow()

    val savedOutfitFlag = MutableStateFlow(true)
    val navtocamera = MutableStateFlow(false)

    val outfits: Flow<PagingData<Outfit>> =
        Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
            pagingSourceFactory = { outfitsRepository.getAllOutfitsPaged() }
        )
            .flow
            .cachedIn(viewModelScope)

    fun getGarments(outfit: Outfit): Flow<List<Garment>> = flow {
        val garments = mutableListOf<Garment>()
        outfit.garmentsId.forEach { id ->
            val garment = garmentRepository.getGarmentStream(id)
                .flowOn(Dispatchers.IO)
                .first()
            garment?.let {
                garments.add(it) // Emit a new copy of the list each time an item is added
            }
        }
        emit(garments)
    }.flowOn(Dispatchers.IO)

    fun saveImage(appDir: File, image: Bitmap, rotation: Float, id: Long?): Job {
        outfitUri.value = Uri.EMPTY // Let UI continue to next screen while processing image
        val job = viewModelScope.launch(Dispatchers.IO) {
            if(!(id == null || id == 0L)) {

                // Let UI know image is processing
                val outfit = outfitsRepository.getOutfitStream(id).first()
                outfit?.let {
                    it.image = "PROCESSING"
                    outfitsRepository.updateOutfit(it)
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
        viewModelScope.launch(Dispatchers.IO) {
            _outfit.value?.let {
                outfitsRepository.deleteOutfit(it)
                _outfit.update {
                    null
                }
            }
        }
    }

    fun getOutfit(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            outfitsRepository.getOutfitStream(id).flowOn(Dispatchers.IO).collect { outfit ->
                outfit?.let {
                    _outfit.update {
                        outfit.copy()
                    }
                }
            }
        }
    }

    fun addToOutfit(garment: Garment){
        viewModelScope.launch {
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
        viewModelScope.launch {
            _outfit.value?.let { outfit ->
                if(!outfit.garmentsId.contains(garment.id)) {
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


    private fun saveImageToOutfit(outfitId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val outfit = outfitsRepository.getOutfitStream(outfitId).first()
            outfit?.let {
                it.image = outfitUri.value.toString()
                outfitsRepository.updateOutfit(it)
            }
        }
    }

    suspend fun saveOutfit(outfit: Outfit): Long {
        return withContext(Dispatchers.IO) {
            val id = if (outfit.id == 0L) {
                val newId = outfitsRepository.insertOutfit(outfit)
                getOutfit(newId)
                newId
            } else {
                outfitsRepository.updateOutfit(outfit)
                outfit.id
            }
            savedOutfitFlag.update { true }
            id
        }
    }
}