package dhmp.wearwise.ui.screens.user

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import dhmp.wearwise.data.GarmentsRepository
import dhmp.wearwise.data.OutfitsRepository
import dhmp.wearwise.data.UserConfigRepository
import dhmp.wearwise.model.CategoryCount
import dhmp.wearwise.model.ColorCount
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.model.Occasion
import dhmp.wearwise.model.OccasionCount
import dhmp.wearwise.model.Season
import dhmp.wearwise.model.SeasonCount
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class UserViewModelTest {
    private lateinit var mockedGarmentRepo: GarmentsRepository
    private lateinit var mockedOutfitRepo: OutfitsRepository
    private lateinit var mockedUserConfigRepo: UserConfigRepository
    private lateinit var context: Context
    private lateinit var model: UserViewModel
    private lateinit var testDispatcher: CoroutineDispatcher


    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockedGarmentRepo = Mockito.mock(GarmentsRepository::class.java)
        mockedOutfitRepo = Mockito.mock(OutfitsRepository::class.java)
        mockedUserConfigRepo = Mockito.mock(UserConfigRepository::class.java)
        testDispatcher = StandardTestDispatcher()
        model = UserViewModel(mockedGarmentRepo, mockedOutfitRepo, mockedUserConfigRepo)
    }

//    @Test
//    fun garmentCount() = {} // Covered in ClothingViewModelTest.getGarmentCount()

    @Test
    fun getOutfitCount() = runTest {
        val expected = 99
        Mockito.`when`(mockedOutfitRepo.getOutfitsCount()).thenAnswer {
            flow { emit(expected) }
        }
        val count = model.outfitCount().first()
        Assert.assertEquals(expected, count)
    }


    @Test
    fun colorCount() = runTest {
        val mockedColorCount = listOf(
            ColorCount("fake1", 1),
            ColorCount("fake2", 2),
            ColorCount("fake3", 3),
            ColorCount(null, 4),
        )

        val expected = mutableMapOf<String?, Int>()
        mockedColorCount.forEach { expected[it.color] = it.count }
        Mockito.`when`(mockedGarmentRepo.getColorCount()).thenAnswer {
            flow { emit(mockedColorCount) }
        }
        val count = model.colorCount().first()
        Assert.assertEquals(expected, count)
    }


    @Test
    fun categoryCount() = runTest {
        val mockedCategoryCount = listOf(
            CategoryCount(5, 1),
            CategoryCount(6, 2),
            CategoryCount(8, 3),
            CategoryCount(null, 4),
        )

        val expected = mutableMapOf<String?, Int>()
        mockedCategoryCount.forEach { expected[it.categoryName] = it.count }
        Mockito.`when`(mockedGarmentRepo.getCategoryCount()).thenAnswer {
            flow { emit(mockedCategoryCount) }
        }
        val count = model.categoryCount().first()
        Assert.assertEquals(expected, count)
    }


    @Test
    fun occasionCount() = runTest {
        val mockedOccasionCount = listOf(
            OccasionCount(Occasion.FORMAL, 1),
            OccasionCount(Occasion.BUSINESS, 2),
            OccasionCount(Occasion.SEMIFORMAL, 3),
            OccasionCount(Occasion.BUSINESS, 4),
        )

        val expected = mutableMapOf<String?, Int>()
        mockedOccasionCount.forEach { expected[it.occasion?.name] = it.count }
        Mockito.`when`(mockedGarmentRepo.getOccasionCount()).thenAnswer {
            flow { emit(mockedOccasionCount) }
        }
        val count = model.occasionCount().first()
        Assert.assertEquals(expected, count)
    }

    @Test
    fun outfitSeason() = runTest {
        val mockedOutfitSeasonCount = listOf(
            SeasonCount(Season.SPRING, 1),
            SeasonCount(Season.ANY, 2),
            SeasonCount(Season.FALL, 3),
            SeasonCount(Season.WINTER, 4),
            SeasonCount(Season.SUMMER, 4),
        )

        val expected = mutableMapOf<String?, Int>()
        mockedOutfitSeasonCount.forEach { expected[it.season?.name] = it.count }
        Mockito.`when`(mockedOutfitRepo.getSeasonCount()).thenAnswer {
            flow { emit(mockedOutfitSeasonCount) }
        }
        val count = model.outfitSeasonCount().first()
        Assert.assertEquals(expected, count)
    }

    @Test
    fun getColorPalette() = runTest{
        val colors = listOf(
            null,
            GarmentColorNames[0].name,
            GarmentColorNames[1].name,
            GarmentColorNames[2].name,
            GarmentColorNames[3].name,
        )

        val expected = listOf(
            Color(0xFFF2F2F2),
            Color(GarmentColorNames[0].color),
            Color(GarmentColorNames[1].color),
            Color(GarmentColorNames[2].color),
            Color(GarmentColorNames[3].color),
        )

        val actual = model.getColorPalette(colors)
        Assert.assertEquals(expected, actual)
    }

}