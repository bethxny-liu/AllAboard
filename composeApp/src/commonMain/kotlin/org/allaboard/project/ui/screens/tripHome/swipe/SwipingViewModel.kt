package org.allaboard.project.ui.screens.tripHome.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.Category
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.AllAboardModel
import org.allaboard.project.domain.VoteType
import org.allaboard.project.domain.ActivityType

data class SwipingUiState(
    val categories: List<Category> = Category.allCategories,
    val selectedCategoryIndex: Int = 0,
    val cards: List<Activity> = emptyList(),
    val swipedIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val selectedCategory: Category
        get() = categories.getOrNull(selectedCategoryIndex) ?: categories.first()

    fun cardsFor(category: Category): List<Activity> {
        val base = when (category) {
            Category.ALL -> cards
            Category.RESTAURANTS -> cards.filter { it.type == ActivityType.RESTAURANT }
            Category.LANDMARKS -> cards.filter { it.type == ActivityType.LANDMARK }
            Category.EXPERIENCES -> cards.filter { it.type == ActivityType.EXPERIENCES }
        }
        return base.filterNot { it.id in swipedIds }
    }

    private val selectedCards: List<Activity>
        get() = cardsFor(selectedCategory)

    val currentCard: Activity?
        get() = selectedCards.firstOrNull()

    val hasCards: Boolean
        get() = cards.isNotEmpty()

    val hasCardsInSelectedCategory: Boolean
        get() = selectedCards.isNotEmpty()

    val isCategoryDone: Boolean
        get() = cardsFor(selectedCategory).isEmpty()

    val isAllDone: Boolean
        get() {
            // Not done if still loading or if there's an error
            if (isLoading || error != null) return false
            // Check if all non-ALL categories have been fully swiped
            val categoriesToCheck = categories.filter { it != Category.ALL }
            return categoriesToCheck.isNotEmpty() && categoriesToCheck.all { category ->
                cardsFor(category).isEmpty()
            }
        }
}

/**
 * ViewModel that drives the swipe UI and persists likes/super-likes as YES votes.
 */
class SwipingViewModel(
    private val model: AllAboardModel,
    private val tripId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(SwipingUiState(isLoading = true))
    val uiState: StateFlow<SwipingUiState> = _uiState.asStateFlow()

    init {
        loadUnvotedActivities()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, swipedIds = emptySet())
        loadUnvotedActivities()
    }

    private fun loadUnvotedActivities() {
        viewModelScope.launch {
            try {
                val user = model.getCurrentUser()
                if (user != null) {
                    val activities = model.getUnvotedActivities(tripId, user.id)
                    _uiState.value = _uiState.value.copy(
                        cards = activities,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not logged in"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load activities"
                )
            }
        }
    }

    fun vote(voteType: VoteType) {
        val id = _uiState.value.currentCard?.id ?: return
        viewModelScope.launch {
            try {
                val user = model.getCurrentUser()?.id
                if (user != null) {
                    model.voteOnActivity(tripId, id, user, voteType)
                }
            } catch (_: Throwable) {
                // ignore persistence errors
            }
        }
        advance()
    }

    fun onCategorySelected(index: Int) {
        val state = _uiState.value
        _uiState.value = state.copy(selectedCategoryIndex = index)
    }

    private fun advance() {
        val state = _uiState.value
        val currentCard = state.currentCard ?: return
        if (currentCard.id in state.swipedIds) return
        _uiState.value = state.copy(swipedIds = state.swipedIds + currentCard.id)
    }
}
