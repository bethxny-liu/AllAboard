package org.allaboard.project.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.rememberAsyncImagePainter
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.Res
import team_102_8.composeapp.generated.resources.prettyplace

/**
 * A composable that displays an image from a URL with a fallback to a default drawable.
 * If imageUrl is null or fails to load, it falls back to prettyplace drawable.
 */
@Composable
fun NetworkImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: Painter = painterResource(Res.drawable.prettyplace)
) {
    val painter = if (imageUrl != null) {
        rememberAsyncImagePainter(
            model = imageUrl,
            error = fallback
        )
    } else {
        fallback
    }

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}
