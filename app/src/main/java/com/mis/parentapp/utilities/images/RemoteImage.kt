package com.mis.parentapp.utilities.images

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.mis.parentapp.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun RemoteImage(
    url: String?,
    @DrawableRes fallbackRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val resolvedUrl = RetrofitInstance.resolveMediaUrl(url)
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, resolvedUrl) {
        value = null
        if (resolvedUrl == null) return@produceState
        value = withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL(resolvedUrl).openConnection().apply {
                    connectTimeout = 5000
                    readTimeout = 7000
                }
                connection.getInputStream().use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    val bitmap = bitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Image(
            painter = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
