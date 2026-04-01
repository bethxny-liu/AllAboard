package org.allaboard.project.ui.screens.itinerary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.allaboard.project.domain.ItineraryDay

internal class ItineraryViewTest {

    @Test
    fun itineraryUiState_withDays() {
        val day = ItineraryDay(date = "2026-06-01", dayNumber = 1, activities = emptyList())
        val s = ItineraryUiState(days = listOf(day), isLoading = false)
        assertEquals(1, s.days.size)
    }

    @Test
    fun itineraryUiState_emptyDays() {
        val s = ItineraryUiState(days = emptyList())
        assertTrue(s.days.isEmpty())
    }

    @Test
    fun itineraryUiState_error() {
        val s = ItineraryUiState(error = "x", isLoading = false)
        assertEquals("x", s.error)
    }
}
