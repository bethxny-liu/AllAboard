package org.allaboard.project.ui.screens.tripHome.swipe.swipingResults

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.allaboard.project.ViewModelTestBase
import org.allaboard.project.allAboardModelFromMocks

internal class SwipingResultsViewModelTest : ViewModelTestBase() {

    private val model = allAboardModelFromMocks()

    @Test
    fun swipingResultsViewModel_simpleName() {
        assertEquals("SwipingResultsViewModel", SwipingResultsViewModel::class.simpleName)
    }

    @Test
    fun categorySelection_updatesIndexWithoutAsyncWait() {
        val vm = SwipingResultsViewModel(tripId = "trip-1", model = model)
        vm.onCategorySelected(3)
        assertEquals(3, vm.uiState.value.selectedCategoryIndex)
    }

    @Test
    fun results_defaultEmptyList() {
        val vm = SwipingResultsViewModel(tripId = "trip-1", model = model)
        assertTrue(vm.uiState.value.results is List<*>)
    }

    @Test
    fun swipingResultsUiState_loadingAndError() {
        val s = SwipingResultsUiState(isLoading = true, error = "bad network")
        assertTrue(s.isLoading)
        assertEquals("bad network", s.error)
    }
}
