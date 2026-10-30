package cl.duoc.agromatch.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class NeedPhotoStorage(context: Context) {
    private val appContext = context.applicationContext

    suspend fun copyIntoNeed(uri: Uri, needId: Long): File = withContext(Dispatchers.IO) {
        val resolver = appContext.contentResolver
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(resolver.getType(uri))
            ?.lowercase()
            ?: "jpg"
        val directory = File(appContext.filesDir, "need_photos/$needId")
        check(directory.exists() || directory.mkdirs()) { "No se pudo crear la carpeta de imágenes." }
        val destination = File(directory, "${UUID.randomUUID()}.$extension")

        try {
            val input = resolver.openInputStream(uri)
                ?: throw IllegalArgumentException("No se pudo abrir la imagen seleccionada.")
            input.use { source -> destination.outputStream().use(source::copyTo) }
            check(destination.length() > 0L) { "La imagen seleccionada está vacía." }
            destination
        } catch (exception: Exception) {
            destination.delete()
            throw exception
        }
    }

    fun delete(filePath: String) {
        File(filePath).delete()
    }
}
