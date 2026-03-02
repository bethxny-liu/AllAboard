package org.allaboard.project.data.repository.mock

import org.allaboard.project.data.repository.ItineraryRepository
import org.allaboard.project.domain.Itinerary
import org.allaboard.project.domain.ScheduledActivity
import org.allaboard.project.domain.ItineraryDay
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType

class MockItineraryRepository : ItineraryRepository {
    private val store: MutableMap<String, Itinerary> = mutableMapOf()

    init {
        // Seed sample itinerary data for the default demo trip id "trip-1" so the UI shows content immediately.
        val sampleAct1 = Activity(
            id = "act-1",
            title = "Pretty Place",
            location = "Asakusa, Tokyo",
            description = "Historic temple with vibrant market streets and iconic gate.",
            rating = 4.7f,
            priceLevel = "$$",
            mapPinLabel = "Senso-ji",
            voteCount = 3,
            imageUrl = null,
            link = null,
            type = ActivityType.LANDMARK
        )

        val sampleAct2 = Activity(
            id = "act-2",
            title = "Ichiran Ramen",
            location = "Shibuya, Tokyo",
            description = "Solo-booth ramen experience known for rich broth.",
            rating = 4.5f,
            priceLevel = "$",
            mapPinLabel = "Ichiran",
            voteCount = 4,
            imageUrl = null,
            link = null,
            type = ActivityType.RESTAURANT
        )

        val sampleAct3 = Activity(
            id = "act-3",
            title = "Mount Fuji Day Trip",
            location = "Yamanashi Prefecture",
            description = "Scenic day trip with lake views and photo spots near Fuji.",
            rating = 4.8f,
            priceLevel = "$$$",
            mapPinLabel = "Fuji Viewpoint",
            voteCount = 4,
            imageUrl = null,
            link = null,
            type = ActivityType.EXPERIENCES
        )

        val day1 = ItineraryDay(
            date = "2026-01-23",
            dayNumber = 1,
            activities = listOf(
                ScheduledActivity(activity = sampleAct1, startTime = "10:00 AM", endTime = "12:00 PM", notes = "Meet at front gate"),
                ScheduledActivity(activity = sampleAct2, startTime = "2:00 PM", endTime = "3:30 PM", notes = "Lunch")
            )
        )

        val day2 = ItineraryDay(
            date = "2026-01-24",
            dayNumber = 2,
            activities = listOf(
                ScheduledActivity(activity = sampleAct3, startTime = "8:00 AM", endTime = "6:00 PM", notes = "Full day trip")
            )
        )

        // Use same demo trip id as MockTripRepository ("trip-1")
        store["trip-1"] = Itinerary(tripId = "trip-1", days = listOf(day1, day2))
    }

    override suspend fun getItinerary(tripId: String): Itinerary? {
        return store["trip-1"] // Always return the seeded itinerary for demo purposes
    }

    override suspend fun addActivityToDay(tripId: String, date: String, scheduledActivity: ScheduledActivity) {
        val itinerary = store.getOrPut(tripId) { Itinerary(tripId = tripId, days = emptyList()) }
        val updatedDays = itinerary.days.toMutableList()
        val dayIndex = updatedDays.indexOfFirst { it.date == date }
        if (dayIndex >= 0) {
            val day = updatedDays[dayIndex]
            val newDay = day.copy(activities = day.activities + scheduledActivity)
            updatedDays[dayIndex] = newDay
        } else {
            val newDay = ItineraryDay(date = date, dayNumber = updatedDays.size + 1, activities = listOf(scheduledActivity))
            updatedDays.add(newDay)
        }
        store[tripId] = itinerary.copy(days = updatedDays)
    }

    override suspend fun removeActivityFromDay(tripId: String, date: String, activityId: String) {
        val itinerary = store[tripId] ?: return
        val updatedDays = itinerary.days.map { day ->
            if (day.date == date) day.copy(activities = day.activities.filterNot { it.activity.id == activityId }) else day
        }
        store[tripId] = itinerary.copy(days = updatedDays)
    }

    override suspend fun updateScheduledActivity(tripId: String, date: String, scheduledActivity: ScheduledActivity) {
        val itinerary = store[tripId] ?: return
        val updatedDays = itinerary.days.map { day ->
            if (day.date == date) {
                day.copy(activities = day.activities.map { if (it.activity.id == scheduledActivity.activity.id) scheduledActivity else it })
            } else day
        }
        store[tripId] = itinerary.copy(days = updatedDays)
    }
}
