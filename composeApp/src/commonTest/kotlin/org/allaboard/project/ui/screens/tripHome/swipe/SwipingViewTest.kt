package org.allaboard.project.ui.screens.tripHome.swipe

import kotlin.test.Test
import kotlin.test.assertEquals
import org.allaboard.project.Category
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType

internal class SwipingViewTest {

    private val landmark = Activity(
        id = "a1",
        title = "Tower",
        location = "X",
        description = null,
        type = ActivityType.LANDMARK
    )
    private val restaurant = Activity(
        id = "a2",
        title = "Cafe",
        location = "Y",
        description = null,
        type = ActivityType.RESTAURANT
    )

    @Test
    fun swipingUiState_cardsFor_restaurantsOnly() {
        val state = SwipingUiState(cards = listOf(landmark, restaurant), isLoading = false)
        assertEquals(1, state.cardsFor(Category.RESTAURANTS).size)
    }

    @Test
    fun swipingUiState_cardsFor_allCategory() {
        val state = SwipingUiState(cards = listOf(landmark, restaurant), isLoading = false)
        assertEquals(2, state.cardsFor(Category.ALL).size)
    }

    @Test
    fun swipingUiState_currentCard_firstInCategory() {
        val state = SwipingUiState(
            cards = listOf(landmark, restaurant),
            selectedCategoryIndex = Category.allCategories.indexOf(Category.RESTAURANTS),
            isLoading = false
        )
        assertEquals(restaurant.id, state.currentCard?.id)
    }
}
