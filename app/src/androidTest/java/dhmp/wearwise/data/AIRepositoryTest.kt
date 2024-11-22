package dhmp.wearwise.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.common.CountTokensResponse
import com.google.ai.client.generativeai.common.InvalidAPIKeyException
import com.google.ai.client.generativeai.common.ServerException
import com.google.ai.client.generativeai.type.content
import dhmp.wearwise.model.Categories
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.model.NearestColorMatchList
import dhmp.wearwise.model.Occasion
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any

class FakeException(msg: String): Exception(msg)

class GarmentGeminiRepositoryTest {


    @Test
    fun garmentCategory_MatchingReply() = runTest {
        val expectedCategory = Categories.first()
        val tests = listOf(
            expectedCategory.name,
            "Somethign along the line of ${expectedCategory.name}",
            "...----${expectedCategory.name}----.."
        )
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for(test in tests) {
            val actual = mockGarmentContentResponse(test).garmentCategory(bitmap)
            assertEquals(expectedCategory, actual)
        }
    }


    @Test
    fun garmentCategory_NonMatchingReply() = runTest {
        val expectedCategory = Categories.first()
        val tests = listOf(
            expectedCategory.name.substring(0, expectedCategory.name.length - 2),
            "gibberish",
            "",
            null
        )
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for(test in tests) {
            val actual = mockGarmentContentResponse(test).garmentCategory(bitmap)
            assertEquals(null, actual)
        }
    }

    @Test
    fun garmentSubCategory_MatchingReply() = runTest {
        val expectedCategory = Categories.first().subCategories!!.first()
        val tests = listOf(
            expectedCategory.name,
            "Somethign along the line of ${expectedCategory.name}",
            "...----${expectedCategory.name}----.."
        )
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for(test in tests) {
            val actual = mockGarmentContentResponse(test).garmentSubCategory(bitmap, Categories.first().id)
            assertEquals(expectedCategory, actual)
        }
    }


    @Test
    fun garmentSubCategory_NonMatchingReply() = runTest {
        val expectedCategory = Categories.first().subCategories!!.first()
        val tests = listOf(
            expectedCategory.name.substring(0, expectedCategory.name.length - 2),
            "gibberish",
            "",
            null
        )
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for(test in tests) {
            val actual = mockGarmentContentResponse(test).garmentSubCategory(bitmap, Categories.first().id)
            assertEquals(null, actual)
        }
    }

    @Test
    fun garmentSubCategory_NonExistentCategoryId() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val actual = mockGarmentContentResponse("Anything Goes").garmentSubCategory(bitmap, 99111)
        assertEquals(null, actual)
    }

    @Test
    fun garmentColor_MatchingReply() = runTest {
        val expectedColor = NearestColorMatchList.first()
        val tests = listOf(
            expectedColor.name,
            "Somethign along the line of ${expectedColor.name}",
            "...----${expectedColor.name}----.."
        )
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for(test in tests) {
            val actual = mockGarmentContentResponse(test).garmentColor(bitmap)
            assertEquals(expectedColor, actual)
        }
    }


    @Test
    fun garmentColor_NonMatchingReply() = runTest {
        val expectedColor = GarmentColorNames.first()
        val tests = listOf(
            expectedColor.name.substring(0, expectedColor.name.length - 2),
            "gibberish",
            "",
            null
        )
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for(test in tests) {
            val actual = mockGarmentContentResponse(test).garmentColor(bitmap)
            assertEquals(null, actual)
        }
    }

    @Test
    fun garmentOccasion_MatchingReply() = runTest {
        val expectedOccasion = Occasion.entries.first()
        val tests = listOf(
            expectedOccasion.name,
            "Somethign along the line of ${expectedOccasion.name}",
            "...----${expectedOccasion.name}----.."
        )
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for(test in tests) {
            val actual = mockGarmentContentResponse(test).garmentOccasion(bitmap)
            assertEquals(expectedOccasion, actual)
        }
    }


    @Test
    fun garmentOccasion_NonMatchingReply() = runTest {
        val expectedOccasion = Occasion.entries.first()
        val tests = listOf(
            expectedOccasion.name.substring(0, expectedOccasion.name.length - 2),
            "gibberish",
            "",
            null
        )
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for(test in tests) {
            val actual = mockGarmentContentResponse(test).garmentOccasion(bitmap)
            assertEquals(null, actual)
        }
    }


    @Test
    fun garmentBrand_MatchingReply() = runTest {
        val tests = listOf(
            "Anything Goes",
            "None",
        )
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for(test in tests) {
            val expectedAns = if(test == "None") null else test
            val actual = mockGarmentContentResponse(test).garmentBrand(bitmap)
            assertEquals(expectedAns, actual)
        }
    }

    @Test
    fun testConfig_GarmentGeminiRepository_Valid() = runTest {
        val content = content() { text("Check request") }
        val mockModel = Mockito.mock(GenerativeModelWrapper::class.java)
        Mockito.`when`(mockModel.countTokens(content))
            .thenAnswer{ CountTokensResponse(1) }
        val result = GarmentGeminiRepository(mockModel).testConfig()
        assertEquals(Pair(true, "Success"), result)
    }

    @Test
    fun testConfig_GeminiConfigExceptions() = runTest {
        val tests = listOf(
            Pair(InvalidAPIKeyException("InvalidAPIKeyException"), "Setting is not valid. Double check your API Key"),
            Pair(ServerException("ServerException"), "Setting is not valid. Verify the model selection"),
            Pair(FakeException("FakeException"),  "Error validating config. Network related?"),
            Pair(Exception("Exception"),  "Error validating config. Network related?")
        )

        for(test in tests) {
            val throwException = test.first
            val expectedMessage = test.second
            val mockModel = Mockito.mock(GenerativeModelWrapper::class.java)
            Mockito.`when`(mockModel.countTokens(any())).thenAnswer( { throw throwException } )
            val result = GarmentGeminiRepository(mockModel).testConfig()
            assertEquals(Pair(false, expectedMessage), result)
        }
    }

    private suspend fun mockGarmentContentResponse(mockedAnswer: String?): GarmentGeminiRepository {
        val mockedResponse = Mockito.mock(GenerateContentResponseWrapper::class.java)
        Mockito.`when`(mockedResponse.text).thenReturn(mockedAnswer)
        val mockModel = Mockito.mock(GenerativeModelWrapper::class.java)
        Mockito.`when`(mockModel.generateContent(any()))
            .thenAnswer { mockedResponse }
        return GarmentGeminiRepository(mockModel)
    }

}