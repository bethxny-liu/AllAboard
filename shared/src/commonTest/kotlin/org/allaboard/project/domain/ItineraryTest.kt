package org.allaboard.project.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for Itinerary, ItineraryDay, and ScheduledActivity. Verifies ScheduledActivity creation
 * with times and notes and default empty notes; ItineraryDay with date, day number, and activities list;
 * Itinerary with tripId and days list.
 */
internal class ItineraryTest {

    private fun createActivity() = Activity(
        id = "a1",
        title = "Morning Tour",
        location = "Tokyo",
        description = "Desc",
        mapPinLabel = "Tour",
        voteCount = 0,
        type = ActivityType.EXPERIENCES
    )

    @Test
    fun scheduledActivity_creation_storesProperties() {
        val activity = createActivity()
        val scheduled = ScheduledActivity(
            activity = activity,
            startTime = "09:00",
            endTime = "12:00",
            notes = "Bring water"
        )
        assertEquals(activity, scheduled.activity)
        assertEquals("09:00", scheduled.startTime)
        assertEquals("12:00", scheduled.endTime)
        assertEquals("Bring water", scheduled.notes)
    }

    @Test
    fun scheduledActivity_defaultNotes_empty() {
        val scheduled = ScheduledActivity(
            activity = createActivity(),
            startTime = "10:00",
            endTime = "11:00"
        )
        assertEquals("", scheduled.notes)
    }

    @Test
    fun itineraryDay_creation_storesProperties() {
        val activity = createActivity()
        val day = ItineraryDay(
            date = "2025-01-15",
            dayNumber = 1,
            activities = listOf(
                ScheduledActivity(activity = activity, startTime = "09:00", endTime = "12:00")
            )
        )
        assertEquals("2025-01-15", day.date)
        assertEquals(1, day.dayNumber)
        assertEquals(1, day.activities.size)
        assertEquals("09:00", day.activities[0].startTime)
    }

    @Test
    fun itinerary_creation_storesProperties() {
        val day = ItineraryDay(
            date = "2025-01-15",
            dayNumber = 1,
            activities = emptyList()
        )
        val itinerary = Itinerary(tripId = "t1", days = listOf(day))
        assertEquals("t1", itinerary.tripId)
        assertEquals(1, itinerary.days.size)
        assertEquals(1, itinerary.days[0].dayNumber)
    }
}
