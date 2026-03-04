package org.allaboard.project.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for TripDashboard. Verifies creation with trip, activities, voting results, and itinerary;
 * and that trip and itinerary may be null with empty activities list.
 */
internal class TripDashboardTest {

    private fun createTrip() = Trip(
        id = "t1",
        title = "Japan",
        destination = "Tokyo",
        region = "Kanto",
        startDate = "2025-01-01",
        endDate = "2025-01-10",
        members = emptyList()
    )

    private fun createActivity(id: String) = Activity(
        id = id,
        title = "Activity $id",
        location = "Tokyo",
        description = "Desc",
        mapPinLabel = id,
        voteCount = 0,
        type = ActivityType.LANDMARK
    )

    @Test
    fun tripDashboard_creation_storesProperties() {
        val trip = createTrip()
        val activities = listOf(createActivity("a1"), createActivity("a2"))
        val voteResult = ActivityVoteResult(
            activity = createActivity("a1"),
            yesVotes = 2,
            noVotes = 0,
            totalVotes = 2,
            yesPercentage = 100f,
            isComplete = true,
            isConfirmed = true,
            voterNames = emptyList()
        )
        val itinerary = Itinerary(tripId = "t1", days = emptyList())

        val dashboard = TripDashboard(
            trip = trip,
            activities = activities,
            votingResults = listOf(voteResult),
            itinerary = itinerary
        )
        assertEquals(trip, dashboard.trip)
        assertEquals(2, dashboard.activities.size)
        assertEquals(1, dashboard.votingResults.size)
        assertEquals(itinerary, dashboard.itinerary)
    }

    @Test
    fun tripDashboard_nullTrip_allowed() {
        val dashboard = TripDashboard(
            trip = null,
            activities = emptyList(),
            votingResults = emptyList(),
            itinerary = null
        )
        assertNull(dashboard.trip)
        assertNull(dashboard.itinerary)
        assertEquals(0, dashboard.activities.size)
    }
}
