package org.allaboard.project.ui.screens.tripHome.swipe

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SwipeCardUi(
    val id: String,
    val name: String,
    val location: String,
    val category: String
)

data class SwipingUiState(
    val categories: List<String> = listOf("Landmarks", "Food", "Activities"),
    val selectedCategoryIndex: Int = 0,
    val categoryProgress: List<Int> = List(categories.size) { 0 },
    val cards: List<SwipeCardUi> = listOf(
        SwipeCardUi("1", "Pretty Place", "Tokyo, Japan", "Landmarks"),
        SwipeCardUi("2", "Skyline Walk", "Osaka, Japan", "Landmarks"),
        SwipeCardUi("3", "Lake View", "Kawaguchiko, Japan", "Landmarks"),
        SwipeCardUi("4", "Sushi Night", "Tokyo, Japan", "Food"),
        SwipeCardUi("5", "Street Eats", "Osaka, Japan", "Food"),
        SwipeCardUi("6", "Tea Ceremony", "Kyoto, Japan", "Activities"),
        SwipeCardUi("7", "Bike Tour", "Nara, Japan", "Activities")
    )
) {
    val selectedCategory: String
        get() = categories.getOrNull(selectedCategoryIndex) ?: categories.first()

    private val selectedCards: List<SwipeCardUi>
        get() = cards.filter { it.category == selectedCategory }

    val currentCard: SwipeCardUi?
        get() = selectedCards.getOrNull(
            categoryProgress.getOrNull(selectedCategoryIndex) ?: 0
        )

    val isCategoryDone: Boolean
        get() {
            val progress = categoryProgress.getOrNull(selectedCategoryIndex) ?: 0
            return progress >= selectedCards.size
        }

    val isAllDone: Boolean
        get() = categories.indices.all { index ->
            val category = categories[index]
            val total = cards.count { it.category == category }
            val progress = categoryProgress.getOrNull(index) ?: 0
            progress >= total
        }
}

class SwipingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SwipingUiState())
    val uiState: StateFlow<SwipingUiState> = _uiState.asStateFlow()

    fun onDislike() = advance()

    fun onSuperLike() = advance()

    fun onLike() = advance()

    fun onCategorySelected(index: Int) {
        val state = _uiState.value
        _uiState.value = state.copy(selectedCategoryIndex = index)
    }

    private fun advance() {
        val state = _uiState.value
        val selectedIndex = state.selectedCategoryIndex
        val cards = state.cards.filter { it.category == state.selectedCategory }
        val progress = state.categoryProgress.toMutableList()
        val current = progress.getOrNull(selectedIndex) ?: 0
        val nextIndex = (current + 1).coerceAtMost(cards.size)

        if (selectedIndex >= progress.size) {
            return
        }

        progress[selectedIndex] = nextIndex
        _uiState.value = state.copy(categoryProgress = progress)
    }
}
