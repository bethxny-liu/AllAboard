package org.allaboard.project.ui.screens.tripHome.swipe

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.ui.screens.tripHome.swipe.swipingResults.SwipingResult

data class SwipingUiState(
    val categories: List<SwipeCategory> = listOf(
        SwipeCategory.ALL,
        SwipeCategory.LANDMARKS,
        SwipeCategory.FOOD,
        SwipeCategory.ACTIVITIES
    ),
    val selectedCategoryIndex: Int = 0,
    val cards: List<Activity>,
    val swipedIds: Set<String> = emptySet()
) {
    val selectedCategory: SwipeCategory
        get() = categories.getOrNull(selectedCategoryIndex) ?: categories.first()

    fun cardsFor(category: SwipeCategory): List<Activity> {
        val type = category.type
        val base = if (type == null) cards else cards.filter { it.type == type }
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
            val categoriesToCheck = categories.filter { it.type != null }
            return categoriesToCheck.isNotEmpty() && categoriesToCheck.all { category ->
                cardsFor(category).isEmpty()
            }
        }
}

class SwipingViewModel(initialCards: List<Activity>) : ViewModel() {
    private val _uiState = MutableStateFlow(SwipingUiState(cards = initialCards))
    val uiState: StateFlow<SwipingUiState> = _uiState.asStateFlow()

    /** Card ids that received a like or super-like (for Swiping Results). */
    private val likedIds = mutableSetOf<String>()

    fun onDislike() = advance()

    fun onSuperLike() {
        _uiState.value.currentCard?.let { likedIds.add(it.id) }
        advance()
    }

    fun onLike() {
        _uiState.value.currentCard?.let { likedIds.add(it.id) }
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

    /**
     * Returns liked/super-liked activities as [SwipingResult] for the Swiping Results screen.
     * Call when [SwipingUiState.isAllDone] is true.
     */
    fun getLikedResults(): List<SwipingResult> {
        val state = _uiState.value
        return state.cards
            .filter { it.id in likedIds }
            .map { activity ->
                SwipingResult(
                    activity = activity,
                    voteCount = 1,
                    voterNames = listOf("You")
                )
            }
    }
}
