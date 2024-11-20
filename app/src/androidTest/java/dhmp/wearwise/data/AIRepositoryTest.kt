package dhmp.wearwise.data
import com.google.ai.client.generativeai.common.InvalidAPIKeyException
import com.google.ai.client.generativeai.common.ServerException
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.CountTokensResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test


class MockModel(val exception: Exception?) : AIModel {
    override suspend fun countTokens(content: Content): CountTokensResponse {
        exception?.let {
            throw exception
        }
        return CountTokensResponse(1)
    }
}

class FakeException(message: String, cause: Throwable? = null): Exception(message, cause)


class AIRepositoryTest {

    @Test
    fun testConfig_validModelAndKey() = runTest {
        val result = GarmentGeminiRepository.testConfig(MockModel(null))
        assertEquals(Pair(true, "Success"), result)
    }

    @Test
    fun testConfig_Exceptions() = runTest {
        val tests = listOf(
            Pair(InvalidAPIKeyException("InvalidAPIKeyException"), "Setting is not valid. Double check your API Key"),
            Pair(ServerException("ServerException"), "Setting is not valid. Verify the model selection"),
            Pair(FakeException("FakeException"),  "Error validating config. Network related?"),
            Pair(Exception("Exception"),  "Error validating config. Network related?")
        )

        for(test in tests) {
            val throwException = test.first
            val expectedMessage = test.second
            val result = GarmentGeminiRepository.testConfig(MockModel(throwException))
            assertEquals(Pair(false, expectedMessage), result)
        }
    }

}