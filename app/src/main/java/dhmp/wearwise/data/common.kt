package dhmp.wearwise.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File


suspend fun getThumbnail(imagePath: String): String? {
    val imageUri = Uri.parse(imagePath)
    imageUri.path?.let { path ->
        val fullResImage = File(path)
        if (fullResImage.exists()) {
            val nameWithoutExtention = fullResImage.nameWithoutExtension
            val thumbnailName = "${nameWithoutExtention}_thumbnailv2.jpg"
            val thumbNailPath = path.replace(fullResImage.name, thumbnailName)
            val thumbNailFile = File(thumbNailPath)

            if (!thumbNailFile.exists()) {
                withContext(Dispatchers.IO) {
                    val imageBitmap = BitmapFactory.decodeFile(path)
                    val outputStream = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 10, outputStream)
                    thumbNailFile.createNewFile()
                    thumbNailFile.writeBytes(outputStream.toByteArray())
                }
            }
            return thumbNailPath
        }
    }
    return null
}