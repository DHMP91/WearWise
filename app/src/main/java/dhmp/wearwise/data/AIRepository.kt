package dhmp.wearwise.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import dhmp.wearwise.model.AISource
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.ColorName
import dhmp.wearwise.model.UserConfig
import dhmp.wearwise.model.nearestColorMatchList

interface AIRepository {
    suspend fun garmentCategory(bitmap: Bitmap): Category?
    suspend fun garmentSubCategory(bitmap: Bitmap, categoryId: Int): Category?
    suspend fun garmentColor(bitmap: Bitmap): ColorName?
}



class AIGarmentRepository(){
    fun getModel(userConfig: UserConfig): AIRepository? {
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
class GarmentGeminiRepository(val apiKey: String, val modelName: String) : AIRepository {
    private var model: GenerativeModel = GenerativeModel(
        modelName = modelName,
        apiKey
    )

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

    private suspend fun generateImageContent(bitmap: Bitmap, question: String): GenerateContentResponse{
        val colorInput = content() {
            image(bitmap)
            text(question)
        }
        return model.generateContent(colorInput)
    }

}