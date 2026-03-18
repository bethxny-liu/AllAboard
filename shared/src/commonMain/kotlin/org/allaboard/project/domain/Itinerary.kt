package org.allaboard.project.domain

import kotlinx.serialization.Serializable

@Serializable
data class Itinerary(
    val tripId: String,
    val days: List<ItineraryDay>
)
@Serializable
data class ItineraryDay(
    val date: String,
    val dayNumber: Int,
    val activities: List<ScheduledActivity>
)
@Serializable
data class ScheduledActivity(
    val activity: Activity,
    val startTime: String,
    val endTime: String,
    val notes: String = ""
)
