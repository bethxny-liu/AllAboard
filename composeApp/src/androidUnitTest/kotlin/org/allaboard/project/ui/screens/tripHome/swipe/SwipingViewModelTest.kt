package org.allaboard.project.ui.screens.tripHome.swipe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.allaboard.project.ViewModelTestBase
import org.allaboard.project.allAboardModelFromMocks

internal class SwipingViewModelTest : ViewModelTestBase() {

    private val model = allAboardModelFromMocks()

    @Test
    fun swipingViewModel_simpleName() {
        assertEquals("SwipingViewModel", SwipingViewModel::class.simpleName)
    }

    @Test
    fun categorySelection_updatesIndexWithoutAsyncWait() {
        val vm = SwipingViewModel(model, tripId = "trip-1")
        vm.onCategorySelected(2)
        assertEquals(2, vm.uiState.value.selectedCategoryIndex)
    }

    @Test
    fun categories_listNotEmpty() {
        val vm = SwipingViewModel(model, tripId = "trip-1")
        assertTrue(vm.uiState.value.categories.isNotEmpty())
    }

    @Test
    fun swipedIds_startsEmpty() {
        val vm = SwipingViewModel(model, tripId = "trip-1")
        assertTrue(vm.uiState.value.swipedIds.isEmpty())
    }

    @Test
    fun swipingUiState_errorEdgeCase() {
        val s = SwipingUiState(error = "offline", isLoading = false)
        assertEquals("offline", s.error)
    }
}
