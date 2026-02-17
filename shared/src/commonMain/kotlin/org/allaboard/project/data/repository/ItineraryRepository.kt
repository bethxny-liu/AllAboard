package org.allaboard.project.data.repository

import org.allaboard.project.domain.Itinerary
import org.allaboard.project.domain.ScheduledActivity

interface ItineraryRepository {
    suspend fun getItinerary(tripId: String): Itinerary?
    suspend fun addActivityToDay(tripId: String, date: String, scheduledActivity: ScheduledActivity)
    suspend fun removeActivityFromDay(tripId: String, date: String, activityId: String)
    suspend fun updateScheduledActivity(tripId: String, date: String, scheduledActivity: ScheduledActivity)
}
