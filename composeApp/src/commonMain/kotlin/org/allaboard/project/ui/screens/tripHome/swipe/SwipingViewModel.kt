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
import org.allaboard.project.ui.screens.tripHome.swipe.swipingResults.SwipingResult

data class SwipingUiState(
    val categories: List<Category> = Category.allCategories,
    val selectedCategoryIndex: Int = 0,
    val cards: List<Activity> = emptyList(),
    val swipedIds: Set<String> = emptySet()
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
    initialCards: List<Activity> = emptyList(),
    private val model: AllAboardModel,
    private val tripId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(SwipingUiState(cards = initialCards))
    val uiState: StateFlow<SwipingUiState> = _uiState.asStateFlow()

    /** Card ids that received a like or super-like (for Swiping Results). */
    private val likedIds = mutableSetOf<String>()

    fun onDislike() = advance()

    fun onSuperLike() {
        //currently acts the same as a like
        _uiState.value.currentCard?.let { likedIds.add(it.id) }
        val id = _uiState.value.currentCard?.id ?: return
        viewModelScope.launch {
            try {
                val user = model.getCurrentUser()?.id
                if (user != null) {
                    model.voteOnActivity(tripId, id, user, VoteType.YES)
                }
            } catch (_: Throwable) {
                // ignore persistence errors
            }
        }
        advance()
    }

    fun onLike() {
        _uiState.value.currentCard?.let { likedIds.add(it.id) }
        val id = _uiState.value.currentCard?.id ?: return
        viewModelScope.launch {
            try {
                val user = model.getCurrentUser()?.id
                if (user != null) {
                    model.voteOnActivity(tripId, id, user, VoteType.YES)
                }
            } catch (_: Throwable) {
                // ignore
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

    /**
     * Returns liked/super-liked activities as [SwipingResult] for the Swiping Results screen.
     * For the mock flow we include a simple voter label.
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
