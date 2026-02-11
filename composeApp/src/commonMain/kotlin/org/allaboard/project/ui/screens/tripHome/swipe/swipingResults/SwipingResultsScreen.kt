package org.allaboard.project.ui.screens.tripHome.swipe.swipingResults

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.domain.Activity
import org.allaboard.project.ui.components.CategoryDropdown
import org.allaboard.project.ui.screens.activityDetails.ActivityDetailsScreen
import org.allaboard.project.ui.screens.tripHome.TripHomeScreen
import org.allaboard.project.ui.theme.FieldBackground
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary

/**
 * Screen showing swiping results: category filter and "Top Matches" list (scrollable,
 * sorted by votes). Back goes to trip dashboard; tapping a card opens activity details.
 *
 * @param initialResults When coming from the swipe flow, pass the liked activities here.
 */
class SwipingResultsScreen(
    private val initialResults: List<SwipingResult>? = null
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: SwipingResultsViewModel = viewModel {
            SwipingResultsViewModel(initialResults = initialResults)
        }
        val uiState by viewModel.uiState.collectAsState()

        SwipingResultsContent(
            uiState = uiState,
            onBack = {
                navigator?.replace(TripHomeScreen())
            },
            onCategorySelected = viewModel::onCategorySelected,
            onActivityClick = { activity ->
                navigator?.push(ActivityDetailsScreen(activity = activity, fallbackActivityId = activity.id))
            }
        )
    }
}

@Composable
private fun SwipingResultsContent(
    uiState: SwipingResultsUiState,
    onBack: () -> Unit,
    onCategorySelected: (Int) -> Unit,
    onActivityClick: (Activity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        // Header: title centered on screen, back button on left
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp, start = 8.dp, end = 8.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Swiping Results",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to trip dashboard"
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
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
                        onClick = { onActivityClick(result.activity) }
                    )
                }
            }
        }
    }
}
