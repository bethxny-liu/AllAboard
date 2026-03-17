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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

/**
 * Screen showing swiping results: category filter and "Top Matches" list (scrollable,
 * sorted by votes). Back goes to trip dashboard; tapping a card opens activity details.
 */
class SwipingResultsScreen(
    private val tripId: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: SwipingResultsViewModel = viewModel {
            SwipingResultsViewModel(tripId = tripId, model = AppModule.allAboardModel)
        }
        val uiState by viewModel.uiState.collectAsState()

        SwipingResultsContent(
            uiState = uiState,
            onBack = { navigator?.pop() },
            onCategorySelected = viewModel::onCategorySelected,
            onActivityClick = { result ->
                navigator?.push(ActivityDetailsScreen(tripId = tripId, activity = result.activity, fallbackActivityId = result.activity.id))
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
