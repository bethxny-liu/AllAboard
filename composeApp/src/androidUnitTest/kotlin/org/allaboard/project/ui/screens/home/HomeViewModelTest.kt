package org.allaboard.project.ui.screens.home

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.allaboard.project.ViewModelTestBase
import org.allaboard.project.allAboardModelFromMocks

internal class HomeViewModelTest : ViewModelTestBase() {

    private val model = allAboardModelFromMocks()

    @Test
    fun homeViewModel_simpleName() {
        assertEquals("HomeViewModel", HomeViewModel::class.simpleName)
    }

    @Test
    fun searchQuery_updatesSynchronously() {
        val vm = HomeViewModel(model)
        vm.onSearchQueryChange("tokyo")
        assertEquals("tokyo", vm.uiState.value.searchQuery)
        vm.onSearchQueryChange("")
        assertEquals("", vm.uiState.value.searchQuery)
    }

    @Test
    fun tripLists_areLists() {
        val vm = HomeViewModel(model)
        assertTrue(vm.uiState.value.upcomingTrips is List<*>)
        assertTrue(vm.uiState.value.pastTrips is List<*>)
    }
}
