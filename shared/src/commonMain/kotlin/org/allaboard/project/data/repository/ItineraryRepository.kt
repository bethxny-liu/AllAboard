package org.allaboard.project.data.repository

import org.allaboard.project.domain.Itinerary
import org.allaboard.project.domain.ScheduledActivity

interface ItineraryRepository {
    suspend fun getItinerary(tripId: String): Itinerary?
    suspend fun regenerateItinerary(tripId: String): Itinerary?
    suspend fun updateScheduledActivity(tripId: String, date: String, scheduledActivity: ScheduledActivity)
}
