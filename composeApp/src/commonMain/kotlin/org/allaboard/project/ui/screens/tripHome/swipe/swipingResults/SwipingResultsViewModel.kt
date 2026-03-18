package org.allaboard.project.ui.screens.tripHome.swipe.swipingResults

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.Category
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.domain.ActivityVoteResult
import org.allaboard.project.domain.AllAboardModel

/**
 * UI state for the Swiping Results screen.
 *
 * Uses [ActivityVoteResult] from the domain layer which contains
 * activity details and per-participant yes/no swipe data (aggregated into vote counts
 * and voter names).
 */
data class SwipingResultsUiState(
    val results: List<ActivityVoteResult> = emptyList(),
    val categories: List<Category> = Category.allCategories,
    val selectedCategoryIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val selectedCategory: Category
        get() = categories.getOrNull(selectedCategoryIndex) ?: Category.ALL

    /** Results filtered by selected category (or all when "All"), sorted by yes vote count descending. */
    val sortedFilteredResults: List<ActivityVoteResult>
        get() {
            val filtered = when (selectedCategory) {
                Category.ALL -> results
                Category.RESTAURANTS -> results.filter { it.activity.type == ActivityType.RESTAURANT }
                Category.LANDMARKS -> results.filter { it.activity.type == ActivityType.LANDMARK }
                Category.EXPERIENCES -> results.filter { it.activity.type == ActivityType.EXPERIENCES }
            }
            return filtered.sortedByDescending { it.yesVotes }
        }
}

/**
 * ViewModel for Swiping Results. Holds the list of activities that received
 * votes (yes swipes), ordered by vote count for "Top Matches".
 */
class SwipingResultsViewModel(
    private val tripId: String,
    private val model: AllAboardModel
) : ViewModel() {
    private val _uiState = MutableStateFlow(SwipingResultsUiState())
    val uiState: StateFlow<SwipingResultsUiState> = _uiState.asStateFlow()

    init {
        loadVotingResults()
    }

    /** Call this when the screen re-appears to ensure results are up-to-date. */
    fun refresh() {
        loadVotingResults()
    }

    private fun loadVotingResults() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val results = model.getVotingResults(tripId)
                _uiState.value = _uiState.value.copy(
                    results = results,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load voting results"
                )
            }
        }
    }

    fun onCategorySelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedCategoryIndex = index)
    }
}