package dhmp.wearwise.data


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class GetThumbnailTest {

    private lateinit var context: Context
    private val fileName = "test_image.png"
    private val thumbnailFileName = "test_image_thumbnailv2.jpg"
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    val width = 1000
    val height = 1000
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        val file = File(context.filesDir, fileName)
        if(file.exists()){
            file.delete()
        }
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }


        val thumbnailFile = File(context.filesDir, thumbnailFileName)
        if(thumbnailFile.exists()){
            thumbnailFile.delete()
        }

        assertTrue(file.exists() && file.length() > 0)
    }

    @Test
    fun getThumbNail_New() {
        //Scenario: Thumbnail is created based on file name if it doesn't exists.
        val file = File(context.filesDir, fileName)
        var retValue: String?
        runBlocking {
            retValue = getThumbnail(file.toPath().toUri().toString())
        }
        val expectedFileName =  "test_image_thumbnailv2.jpg"
        val expectedFile = File(context.filesDir, expectedFileName)
        assertTrue(expectedFile.exists())
        assertTrue(!retValue.isNullOrEmpty())
    }

    @Test
    fun getThumbNail_NoOverwrite() {
        //Scenario: Thumbnail is not overwriting if thumbnail already exists
        val file = File(context.filesDir, fileName)
        val thumbnailFile = File(context.filesDir, thumbnailFileName)
        FileOutputStream(thumbnailFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        val thumbnailSize = thumbnailFile.length()
        var retValue: String?
        runBlocking {
            retValue = getThumbnail(file.toPath().toUri().toString())
        }
        assertTrue(thumbnailFile.length() == thumbnailSize)
        assertTrue(!retValue.isNullOrEmpty())
    }

    @Test
    fun getThumbNail_Overwrite() {
        //Scenario: Thumbnail is recreated if the image date is newer than thumbnail date
        val file = File(context.filesDir, fileName)
        val thumbnailFile = File(context.filesDir, thumbnailFileName)

        FileOutputStream(thumbnailFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        val thumbnailSize = thumbnailFile.length()
        var retValue: String?
        runBlocking {
            retValue = getThumbnail(file.toPath().toUri().toString())
        }
        assertTrue(thumbnailFile.length() != thumbnailSize)
        assertTrue(!retValue.isNullOrEmpty())
    }

    @Test
    fun getThumbNail_NoImage() {
        //Scenario: Supplied image path does not exists
        val file = File(context.filesDir, fileName)
        val thumbnailFile = File(context.filesDir, thumbnailFileName)
        if(file.exists()){
            file.delete()
        }

        var retValue: String?
        runBlocking {
            retValue = getThumbnail(file.toPath().toUri().toString())
        }

        assertTrue(!thumbnailFile.exists())
        assertTrue(!file.exists())
        assertTrue(retValue.isNullOrEmpty())
    }
}