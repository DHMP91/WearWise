package dhmp.wearwise.ui.screens.user

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dhmp.wearwise.data.GarmentGeminiRepository
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.OutfitsRepository
import dhmp.wearwise.data.UserConfigRepository
import dhmp.wearwise.model.AISource
import dhmp.wearwise.model.CategoryCount
import dhmp.wearwise.model.ColorCount
import dhmp.wearwise.model.GarmentColorNameTable
import dhmp.wearwise.model.OccasionCount
import dhmp.wearwise.model.SeasonCount
import dhmp.wearwise.model.UserConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class UserViewModel (
    private val garmentRepository: GarmentsRepository,
    private val outfitsRepository: OutfitsRepository,
    private val userConfigRepository: UserConfigRepository,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
): ViewModel() {
    private val _userConfig: MutableStateFlow<UserConfig> = MutableStateFlow(UserConfig(-1, AISource.GOOGLE, "", ""))
    val userConfig: StateFlow<UserConfig> = _userConfig.asStateFlow()
    val showConfig = MutableStateFlow(false)
    val configMessage = MutableStateFlow("")

    init {
        getUserConfig()
    }

    fun garmentCount(): Flow<Int> = garmentRepository.getGarmentsCount(
        excludedCategories = listOf(),
        excludedColors =  listOf(),
        excludedBrands =  listOf()
    )

    fun outfitCount(): Flow<Int> = outfitsRepository.getOutfitsCount()


    fun colorCount(): Flow<Map<String?, Int>> {
        return garmentRepository
            .getColorCount()
            .map { list: List<ColorCount> -> list.associate { it.color to it.count } }
    }

    fun categoryCount(): Flow<Map<String?, Int>> {
        return garmentRepository
            .getCategoryCount()
            .map { list: List<CategoryCount> -> list.associate { it.categoryName to it.count } }
    }

    fun occasionCount(): Flow<Map<String?, Int>> {
        return garmentRepository
            .getOccasionCount()
            .map { list: List<OccasionCount> -> list.associate { it.occasion?.name to it.count } }
    }

    fun outfitSeasonCount(): Flow<Map<String?, Int>> {
        return outfitsRepository
            .getSeasonCount()
            .map { list: List<SeasonCount> -> list.associate { it.season?.name to it.count } }
    }


    fun getColorPalette(colors: List<String?>): List<Color> {
        val colorPalette = mutableListOf<Color>()

        colors.forEach {
            val garmentColor = GarmentColorNameTable[it]?.color
            val color = when(garmentColor) {
                null -> Color(0xFFF2F2F2)
                else -> Color(garmentColor)
            }
            colorPalette.add(
                color
            )
        }

        return colorPalette
    }

    fun toggleConfig(){
        showConfig.update {
            !showConfig.value
        }
    }
    fun testConfig(userConfig: UserConfig): Boolean {
        var success: Boolean
        runBlocking {
            success = when(userConfig.aiSource) {
                AISource.GOOGLE -> {
                    val response = GarmentGeminiRepository.testConfig(
                        userConfig.aiModelName,
                        userConfig.aiApiKey
                    )
                    configMessage.update {
                        response.second
                    }
                    response.first
                }
                else -> false
            }
        }
        return success
    }

    fun updateConfig(userConfig: UserConfig) {
        viewModelScope.launch(dispatcherIO) {
            userConfigRepository.updateUserConfig(userConfig)
            _userConfig.update {
                userConfig
            }
            configMessage.update {
                "Successfully saved settings"
            }
        }
    }

    fun getAIModels(source: AISource): List<String>? {
        return when(source){
            AISource.GOOGLE -> listOf("gemini-1.5-flash-latest")
            else -> null
        }
    }

    private fun getUserConfig(){
        viewModelScope.launch(dispatcherIO) {
            userConfigRepository.getUserConfigStream().flowOn(dispatcherIO).collectLatest { config ->
                config?.let {
                    _userConfig.update {
                        config
                    }
                }
            }
        }
    }
}