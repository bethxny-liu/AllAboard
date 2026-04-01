package org.allaboard.project.ui.screens.tripHome.swipe.swipingResults

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.di.AppModule
import org.allaboard.project.domain.ActivityVoteResult
import org.allaboard.project.ui.components.CategoryDropdown
import org.allaboard.project.ui.components.ScreenTopBar
import org.allaboard.project.ui.components.ScreenTopBarDefaults
import org.allaboard.project.ui.components.TitleAlignment
import org.allaboard.project.ui.screens.activityDetails.ActivityDetailsScreen
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.allaboard.project.navigator.pushIfNotTop
import kotlin.random.Random

/**
 * Screen showing swiping results: category filter and "Top Matches" list (scrollable,
 * sorted by votes). Back goes to trip dashboard; tapping a card opens activity details.
 */
class SwipingResultsScreen(
    private val tripId: String
) : Screen {
    override val key = super.key + "${Random.nextDouble(Double.MIN_VALUE, Double.MAX_VALUE)}"
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: SwipingResultsViewModel = viewModel {
            SwipingResultsViewModel(tripId = tripId, model = AppModule.allAboardModel)
        }

        // Refresh every time this screen becomes visible again (e.g., returning from back stack).
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner, viewModel) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refresh()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        val uiState by viewModel.uiState.collectAsState()

        SwipingResultsContent(
            uiState = uiState,
            onBack = { navigator?.pop() },
            onCategorySelected = viewModel::onCategorySelected,
            onActivityClick = { result ->
                navigator?.pushIfNotTop(ActivityDetailsScreen(tripId = tripId, activity = result.activity, fallbackActivityId = result.activity.id))
            }
        )
    }
}

@Composable
private fun SwipingResultsContent(
    uiState: SwipingResultsUiState,
    onBack: () -> Unit,
    onCategorySelected: (Int) -> Unit,
    onActivityClick: (ActivityVoteResult) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        ScreenTopBar(
            title = "Swiping Results",
            onBack = onBack,
            titleAlignment = TitleAlignment.Center
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenTopBarDefaults.ContentPaddingHorizontal)
        ) {
            // Category dropdown (Landmarks, etc.)
            CategoryDropdown(
                categories = uiState.categories,
                selectedIndex = uiState.selectedCategoryIndex,
                onCategorySelected = onCategorySelected,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Top Matches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                uiState.sortedFilteredResults.forEachIndexed { index, result ->
                    SwipingResultCard(
                        result = result,
                        showMostVotesTag = (index == 0),
                        onClick = { onActivityClick(result) }
                    )
                }
            }
        }
    }
}
