package cl.duoc.agromatch.ui.farmer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun NeedPhotoThumbnail(
    uri: Uri,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap = produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) { loadThumbnail(context, uri) }
    }.value

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier.border(1.dp, FarmerBorder),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(FarmerBlueSoft).border(1.dp, FarmerBorder),
            contentAlignment = Alignment.Center
        ) {
            Text("Imagen", color = FarmerCharcoal, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun loadThumbnail(context: Context, uri: Uri): Bitmap? {
    return try {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            null
        } else {
            val largestSide = maxOf(bounds.outWidth, bounds.outHeight)
            var sampleSize = 1
            while (largestSide / sampleSize > 480) sampleSize *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        }
    } catch (_: Exception) {
        null
    }
}
