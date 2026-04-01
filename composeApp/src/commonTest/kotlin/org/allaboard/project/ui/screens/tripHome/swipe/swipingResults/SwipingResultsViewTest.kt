package org.allaboard.project.ui.screens.tripHome.swipe.swipingResults

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.allaboard.project.Category
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.domain.ActivityVoteResult

internal class SwipingResultsViewTest {

    private val sampleActivity = Activity(
        id = "x1",
        title = "Spot",
        location = "Somewhere",
        description = null,
        type = ActivityType.LANDMARK
    )

    @Test
    fun swipingResultsUiState_sortedFilteredResults_order() {
        val low = ActivityVoteResult(
            activity = sampleActivity.copy(id = "low"),
            yesVotes = 1,
            noVotes = 0,
            totalVotes = 1,
            yesPercentage = 0.2f,
            isComplete = false,
            isConfirmed = false,
            voterNames = emptyList()
        )
        val high = ActivityVoteResult(
            activity = sampleActivity.copy(id = "high", type = ActivityType.RESTAURANT),
            yesVotes = 4,
            noVotes = 0,
            totalVotes = 4,
            yesPercentage = 1f,
            isComplete = true,
            isConfirmed = true,
            voterNames = listOf("A")
        )
        val state = SwipingResultsUiState(results = listOf(low, high), isLoading = false)
        assertEquals("high", state.sortedFilteredResults.first().activity.id)
    }

    @Test
    fun swipingResultsUiState_selectedCategory() {
        val r = ActivityVoteResult(
            activity = sampleActivity,
            yesVotes = 1,
            noVotes = 0,
            totalVotes = 1,
            yesPercentage = 1f,
            isComplete = true,
            isConfirmed = false,
            voterNames = emptyList()
        )
        val idx = Category.allCategories.indexOf(Category.LANDMARKS)
        val state = SwipingResultsUiState(results = listOf(r), selectedCategoryIndex = idx, isLoading = false)
        assertEquals(Category.LANDMARKS, state.selectedCategory)
    }

    @Test
    fun swipingResultsUiState_emptyResults() {
        val state = SwipingResultsUiState(results = emptyList(), isLoading = false)
        assertTrue(state.sortedFilteredResults.isEmpty())
    }
}
