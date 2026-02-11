package org.allaboard.project.ui.screens.tripHome.swipe.swipingResults

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.allaboard.project.Category
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType

/**
 * UI state for the Swiping Results screen.
 *
 * When the swiping flow is merged, [results] will be derived from an array of
 * activities and per-participant yes/no swipe data (aggregated into vote counts
 * and voter names).
 */
data class SwipingResultsUiState(
    val results: List<SwipingResult> = emptyList(),
    val categories: List<Category> = Category.allCategories,
    val selectedCategoryIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val selectedCategory: Category
        get() = categories.getOrNull(selectedCategoryIndex) ?: Category.ALL

    /** Results filtered by selected category (or all when "All"), sorted by vote count descending. */
    val sortedFilteredResults: List<SwipingResult>
        get() {
            val filtered = when (selectedCategory) {
                Category.ALL -> results
                Category.RESTAURANTS -> results.filter { it.activity.type == ActivityType.RESTAURANT }
                Category.LANDMARKS -> results.filter { it.activity.type == ActivityType.LANDMARK }
                Category.EXPERIENCES -> results.filter { it.activity.type == ActivityType.EXPERIENCES }
            }
            return filtered.sortedByDescending { it.voteCount }
        }
}

/**
 * ViewModel for Swiping Results. Holds the list of activities that received
 * votes (yes swipes), ordered by vote count for "Top Matches".
 */
class SwipingResultsViewModel(
    initialResults: List<SwipingResult>? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(SwipingResultsUiState())
    val uiState: StateFlow<SwipingResultsUiState> = _uiState.asStateFlow()

    init {
        if (!initialResults.isNullOrEmpty()) {
            setResults(initialResults)
        } else {
            loadStubResults()
        }
    }

    private fun loadStubResults() {
        _uiState.value = SwipingResultsUiState(
            results = stubSwipingResults()
        )
    }

    fun onCategorySelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedCategoryIndex = index)
    }

    /** Replace with real data when swiping is merged: activities + yes/no per user. */
    fun setResults(results: List<SwipingResult>) {
        _uiState.value = _uiState.value.copy(results = results)
    }
}

/** Stub data matching the Figma (Pretty Place, vote counts, Daniel/Rachael + N more). */
private fun stubSwipingResults(): List<SwipingResult> {
    val stubActivity = { id: String ->
        Activity(
            id = id,
            title = "Pretty Place",
            location = "",
            description = "",
            mapPinLabel = "Pretty Place",
            voteCount = 0,
            type = ActivityType.LANDMARK
        )
    }
    return listOf(
        SwipingResult(activity = stubActivity("1"), voteCount = 4, voterNames = listOf("Daniel", "Rachael", "Alex", "Jordan")),
        SwipingResult(activity = stubActivity("2"), voteCount = 3, voterNames = listOf("Daniel", "Rachael", "Alex")),
        SwipingResult(activity = stubActivity("3"), voteCount = 2, voterNames = listOf("Daniel", "Rachael")),
        SwipingResult(activity = stubActivity("4"), voteCount = 2, voterNames = listOf("Daniel", "Jordan"))
    )
}
