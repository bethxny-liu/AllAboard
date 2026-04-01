package org.allaboard.project.ui.screens.home

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class HomeViewTest {

    @Test
    fun homeUiState_tripSummary() {
        val trip = TripSummary(
            id = "t1",
            title = "Tokyo",
            dateRange = "Jan 1 - Jan 5",
            memberCount = 3,
            imageUrl = null
        )
        val s = HomeUiState(displayName = "Dan", upcomingTrips = listOf(trip))
        assertEquals("Tokyo", s.upcomingTrips.single().title)
        assertEquals(3, s.upcomingTrips.single().memberCount)
    }

    @Test
    fun tripSummary_idAndDateRange() {
        val t = TripSummary("id-2", "Paris", "Mar 1", 2, null)
        assertEquals("id-2", t.id)
        assertEquals("Mar 1", t.dateRange)
    }

    @Test
    fun homeUiState_emptyLists() {
        val s = HomeUiState(upcomingTrips = emptyList(), pastTrips = emptyList())
        assertTrue(s.upcomingTrips.isEmpty())
        assertTrue(s.pastTrips.isEmpty())
    }
}
