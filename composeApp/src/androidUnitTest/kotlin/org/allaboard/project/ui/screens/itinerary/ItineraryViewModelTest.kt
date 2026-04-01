package org.allaboard.project.ui.screens.itinerary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.allaboard.project.ViewModelTestBase
import org.allaboard.project.allAboardModelFromMocks
import org.allaboard.project.domain.ItineraryDay

internal class ItineraryViewModelTest : ViewModelTestBase() {

    private val model = allAboardModelFromMocks()

    @Test
    fun itineraryViewModel_simpleName() {
        assertEquals("ItineraryViewModel", ItineraryViewModel::class.simpleName)
    }

    @Test
    fun days_fieldIsAlwaysList() {
        val vm = ItineraryViewModel(tripId = "trip-1", model = model)
        assertTrue(vm.uiState.value.days is List<*>)
    }

    @Test
    fun itineraryUiState_emptyDaysEdgeCase() {
        val s = ItineraryUiState(days = emptyList(), isLoading = false, error = null)
        assertTrue(s.days.isEmpty())
    }

    @Test
    fun itineraryUiState_withDayCount() {
        val d = ItineraryDay(date = "2026-01-01", dayNumber = 1, activities = emptyList())
        val s = ItineraryUiState(days = listOf(d), isLoading = false)
        assertEquals(1, s.days.size)
    }
}
