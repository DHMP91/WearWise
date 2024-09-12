package dhmp.wearwise.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.mockito.ArgumentCaptor
import java.io.File
import java.io.FileOutputStream


class FakePagingSource<T : Any>(
    private val items: List<T>
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return LoadResult.Page(
            data = items,
            prevKey = null,
            nextKey = null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, T>) = null
}

fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()


fun fakeImage(context: Context, fileName: String, fillColor: Int = Color.GREEN): File {
    val width = 1000
    val height = 1000
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val appDir = context.filesDir
    val file = File(appDir, fileName)
    val paint = Paint().apply {
        color = fillColor
        style = Paint.Style.FILL
    }
    val canvas = Canvas(bitmap)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    if (file.exists()) {
        file.delete()
    }
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }
    return file
}