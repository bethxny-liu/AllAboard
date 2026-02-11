package org.allaboard.project.ui.screens.tripHome.swipe

import androidx.compose.foundation.Image
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.allaboard.project.Category
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.ui.theme.BluePrimaryDark
import org.allaboard.project.ui.theme.CoralAccent
import org.allaboard.project.ui.theme.GreenAccent
import org.allaboard.project.ui.theme.Surface as AppSurface
import org.allaboard.project.ui.theme.SwipeDislike
import org.allaboard.project.ui.theme.SwipeLike
import org.allaboard.project.ui.theme.SwipeSuperLike
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun SwipeCard(
    activity: Activity,
    imagePainter: Painter,
    modifier: Modifier = Modifier,
    onDislike: () -> Unit = {},
    onSuperLike: () -> Unit = {},
    onLike: () -> Unit = {},
    onLearnMore: () -> Unit = {}
) {
    val cardShape = RoundedCornerShape(28.dp)
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    BoxWithConstraints(modifier = modifier.aspectRatio(0.70f)) {
        val maxWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val swipeThreshold = maxWidthPx * 0.25f

        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(maxWidthPx) {
                    detectDragGestures(
                        onDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetX.value > swipeThreshold -> {
                                        offsetX.animateTo(
                                            targetValue = maxWidthPx * 1.1f,
                                            animationSpec = tween(200)
                                        )
                                        offsetX.snapTo(0f)
                                        onLike()
                                    }
                                    offsetX.value < -swipeThreshold -> {
                                        offsetX.animateTo(
                                            targetValue = -maxWidthPx * 1.1f,
                                            animationSpec = tween(200)
                                        )
                                        offsetX.snapTo(0f)
                                        onDislike()
                                    }
                                    else -> {
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring()
                                        )
                                    }
                                }
                            }
                        }
                    )
                },
            shape = cardShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = imagePainter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            color = Color.White.copy(alpha = 0.88f)
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activity.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activity.location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        CategoryChip(
                            text = activity.type,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Text(
                        text = "Learn more",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable(onClick = onLearnMore)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SwipeActionButton(
                            backgroundColor = SwipeDislike,
                            icon = Icons.Filled.Close,
                            contentDescription = "Dislike",
                            onClick = onDislike,
                            enabled = false
                        )
                        SwipeActionButton(
                            backgroundColor = SwipeSuperLike,
                            icon = Icons.Filled.Star,
                            contentDescription = "Super like",
                            onClick = onSuperLike
                        )
                        SwipeActionButton(
                            backgroundColor = SwipeLike,
                            icon = Icons.Filled.Check,
                            contentDescription = "Like",
                            onClick = onLike,
                            enabled = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(text: ActivityType, modifier: Modifier = Modifier) {
    val categoryColor = categoryColorFor(text)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(categoryColor.copy(alpha = 0.30f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = Category.fromType(text).displayName,
            style = MaterialTheme.typography.labelMedium,
            color = categoryColor
        )
    }
}

private fun categoryColorFor(category: ActivityType): Color {
    return when (category) {
        ActivityType.LANDMARK -> BluePrimaryDark
        ActivityType.RESTAURANT -> CoralAccent
        ActivityType.EXPERIENCES -> GreenAccent
    }
}

@Composable
private fun SwipeActionButton(
    backgroundColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(53.dp),
        shape = CircleShape,
        color = backgroundColor,
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White
            )
        }
    }
}
