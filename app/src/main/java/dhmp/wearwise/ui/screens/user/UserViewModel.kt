package dhmp.wearwise.ui.screens.user

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.OutfitsRepository
import dhmp.wearwise.model.CategoryCount
import dhmp.wearwise.model.ColorCount
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.model.OccasionCount
import dhmp.wearwise.model.SeasonCount
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserViewModel (
    private val garmentRepository: GarmentsRepository,
    private val outfitsRepository: OutfitsRepository,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO
): ViewModel() {

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
            val garmentColor = GarmentColorNames.firstOrNull { garmentColor -> garmentColor.name == it }?.color
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
}