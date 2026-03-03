package org.allaboard.project.ui.screens.tripHome.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import androidx.lifecycle.viewmodel.compose.viewModel
import org.allaboard.project.domain.Activity
import org.allaboard.project.ui.screens.activityDetails.ActivityDetailsScreen
import org.allaboard.project.ui.screens.tripHome.swipe.swipingResults.SwipingResultsScreen
import org.allaboard.project.ui.theme.Background
import org.allaboard.project.ui.theme.FieldBackground
import org.allaboard.project.ui.theme.SwipeDislike
import org.allaboard.project.ui.theme.SwipeLike
import org.allaboard.project.ui.theme.SwipeSuperLike
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.allaboard.project.Category
import org.allaboard.project.ui.components.CategoryDropdown
import org.allaboard.project.di.AppModule
import org.allaboard.project.domain.VoteType

class SwipingScreen(private val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: SwipingViewModel = viewModel {
            SwipingViewModel(
                model = AppModule.allAboardModel,
                tripId = tripId
            )
        }
        val uiState by viewModel.uiState.collectAsState()

        SwipingScreenContent(
            uiState = uiState,
            onBack = { navigator?.pop() },
            vote = viewModel::vote,
            onCategorySelected = viewModel::onCategorySelected,
            onLearnMore = { activity ->
                navigator?.push(ActivityDetailsScreen(activity, activity.id))
            },
            onAllDone = {
                // Navigate to results screen which loads results from the model
                navigator?.push(SwipingResultsScreen(tripId))
            }
        )
    }
}

@Composable
fun SwipingScreenContent(
    uiState: SwipingUiState,
    onBack: () -> Unit,
    vote: (VoteType) -> Unit,
    onCategorySelected: (Int) -> Unit,
    onLearnMore: (Activity) -> Unit = {},
    onAllDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val flashAlpha = remember { Animatable(0f) }
    var flashColor by remember { mutableStateOf<Color?>(null) }
    var flashIcon by remember { mutableStateOf<ImageVector?>(null) }
    var onAllDoneFired by remember { mutableStateOf(false) }

    val triggerFlash: (Color, ImageVector) -> Unit = { color, icon ->
        scope.launch {
            flashColor = color
            flashIcon = icon
            flashAlpha.snapTo(0f)
            flashAlpha.animateTo(0.35f, animationSpec = tween(150))
            delay(250)
            flashAlpha.animateTo(0f, animationSpec = tween(300))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LaunchedEffect(uiState.isAllDone) {
            if (uiState.hasCards && uiState.isAllDone && !onAllDoneFired) {
                onAllDoneFired = true
                onAllDone()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Swipe!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CategoryDropdown(
                categories = uiState.categories,
                selectedIndex = uiState.selectedCategoryIndex,
                onCategorySelected = onCategorySelected,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val widthFromHeight = maxHeight * 0.70f
            val cardWidth = 360.dp.coerceAtMost(widthFromHeight)
            val card = uiState.currentCard
            AnimatedContent(
                targetState = card,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "SwipeCardFade"
            ) { targetCard ->
                if (targetCard != null) {
                    key(targetCard.id) {
                        SwipeCard(
                            activity = targetCard,
                            modifier = Modifier.width(cardWidth),
                            onDislike = {
                                triggerFlash(SwipeDislike, Icons.Filled.Close)
                                vote(VoteType.NO)
                            },
                            onSuperLike = {
                                triggerFlash(SwipeSuperLike, Icons.Filled.Star)
                                vote(VoteType.YES)
                            },
                            onLearnMore = { onLearnMore(targetCard) },
                            onLike = {
                                triggerFlash(SwipeLike, Icons.Filled.Check)
                                vote(VoteType.YES)
                            }
                        )
                    }
                } else if (!uiState.hasCardsInSelectedCategory) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 24.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        EmptyCategoryMessage(category = uiState.selectedCategory)
                    }
                } else if (uiState.isCategoryDone) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 24.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        CategoryDoneMessage(category = uiState.selectedCategory)
                    }
                }
            }
        }
        }

        if (flashColor != null && flashAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(flashColor!!.copy(alpha = flashAlpha.value)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = flashIcon ?: Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(96.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryDoneMessage(category: Category, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(FieldBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(42.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "All set for ${category.displayName}",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pick another category to keep swiping.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyCategoryMessage(category: Category, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(FieldBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No activities in ${category.displayName}",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pick another category to keep swiping.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}