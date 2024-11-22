package dhmp.wearwise.data

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.common.InvalidAPIKeyException
import com.google.ai.client.generativeai.common.ServerException
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.CountTokensResponse
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import dhmp.wearwise.model.AISource
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.ColorName
import dhmp.wearwise.model.Occasion
import dhmp.wearwise.model.UserConfig
import dhmp.wearwise.model.nearestColorMatchList

val tag = "AIRepository"

interface AIRepository {
    suspend fun garmentCategory(bitmap: Bitmap): Category?
    suspend fun garmentSubCategory(bitmap: Bitmap, categoryId: Int): Category?
    suspend fun garmentColor(bitmap: Bitmap): ColorName?
    suspend fun garmentOccasion(bitmap: Bitmap): Occasion?
    suspend fun garmentBrand(bitmap: Bitmap): String?
    suspend fun testConfig(): Pair<Boolean, String>
}

open class AIRepositoryProvider  {
    open fun getRepository(userConfig: UserConfig): AIRepository? {
        val model = when(userConfig.aiSource) {
            AISource.GOOGLE ->
                GarmentGeminiRepository(
                    apiKey = userConfig.aiApiKey,
                    modelName = userConfig.aiModelName,
                )
//            "openAI" -> null
            else -> null
        }
        return model
    }
}


//Mainly for mocking due to android final class mockito limitation
open class GenerativeModelWrapper(private val generativeModel: GenerativeModel) {
    open suspend fun countTokens(prompt: Content): CountTokensResponse = generativeModel.countTokens(prompt)
    open suspend fun generateContent(prompt: Content): GenerateContentResponse = generativeModel.generateContent()
}

class GarmentGeminiRepository : AIRepository {
    private var model: GenerativeModelWrapper

    constructor(apiKey: String, modelName: String){
        model = GenerativeModelWrapper(GenerativeModel(modelName = modelName, apiKey))
    }

    constructor(model: GenerativeModelWrapper){
        this.model = model
    }

    override suspend fun garmentCategory(bitmap: Bitmap): Category? {
        val categoryNames = Category.categories().map { c -> c.name }
        val categoryQuestion = "This clothing piece belong to which category: ${categoryNames.joinToString(separator = ",")}?"
        val result = generateImageContent(bitmap, categoryQuestion)
        val categoryAns = result.text

        return categoryAns?.let { ans ->
            Category.categories().find { c -> ans.lowercase().contains(c.name.lowercase()) }
        }
    }

    override suspend fun garmentSubCategory(bitmap: Bitmap, categoryId: Int): Category? {
        val subCategories = Category.getCategory(categoryId)?.subCategories
        subCategories?.let { subCats ->
            val names = subCats.map { c -> c.name }
            val subCategoryQuestion =
                "This clothing piece belong to which sub categories: ${
                    names.joinToString(separator = ",")
                }?"
            val result = generateImageContent(bitmap, subCategoryQuestion)
            val subCategoryAns = result.text
            return subCategoryAns?.let { ans ->
                subCats.find {
                        c -> ans.lowercase().contains(c.name.lowercase())
                }
            }
        }
        return null
    }

    override suspend fun garmentColor(bitmap: Bitmap): ColorName? {
        val colorNames = nearestColorMatchList.map { it.name }
        val colorQuestion = "This clothing piece belong to closest to which color: ${colorNames.joinToString(separator = ",")}?"
        val result = generateImageContent(bitmap, colorQuestion)
        val colorAns = result.text

        return colorAns?.let { ans ->
            nearestColorMatchList.find {
                    c -> ans.lowercase().contains(c.name.lowercase())
            }
        }
    }

    override suspend fun garmentOccasion(bitmap: Bitmap): Occasion? {
        val occasions = Occasion.entries.map { it.name }
        val occasionQuestion = "This clothing piece is best for which occasion: ${occasions.joinToString(separator = ",")}?"
        val result = generateImageContent(bitmap, occasionQuestion)
        val occasionAns = result.text

        return occasionAns?.let { ans ->
            Occasion.entries.find {
                    c -> ans.lowercase().contains(c.name.lowercase())
            }
        }
    }

    override suspend fun garmentBrand(bitmap: Bitmap): String? {
        val noBrand = "None"
        val brandQuestion = "What's the brand of this clothing piece, if no brand reply \"${noBrand}\"?"
        val result = generateImageContent(bitmap, brandQuestion)
        val brandAns = result.text

        return brandAns?.let { ans ->
            if(brandAns.contains(noBrand)){
                return null
            }else{
                return ans
            }
        }
    }

    override suspend fun testConfig(): Pair<Boolean, String> {
        val content = content() { text("Check request") }

        var response = Pair(true, "Success")
        var exception: Exception? = null
        try{
            model.countTokens(content)
        } catch (e: InvalidAPIKeyException) {
            exception = e
            response = Pair(false, "Setting is not valid. Double check your API Key")
        } catch (e: ServerException) {
            exception = e
            response = Pair(false, "Setting is not valid. Verify the model selection")
        } catch (e: Exception){
            exception = e
            response = Pair(false, "Error validating config. Network related?")
        } finally {
            exception?.let {
                it.message?.let { msg ->
                    Log.w(tag, msg)
                }
            }
        }
        return response
    }

    private suspend fun generateImageContent(bitmap: Bitmap, question: String): GenerateContentResponse{
        val colorInput = content() {
            image(bitmap)
            text(question)
        }
        return model.generateContent(colorInput)
    }
}