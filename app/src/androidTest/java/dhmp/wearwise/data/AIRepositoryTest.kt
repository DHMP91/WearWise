package dhmp.wearwise.data

import com.google.ai.client.generativeai.common.CountTokensResponse
import com.google.ai.client.generativeai.common.InvalidAPIKeyException
import com.google.ai.client.generativeai.common.ServerException
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any

class FakeException(msg: String): Exception(msg)

class AIRepositoryTest {

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

}