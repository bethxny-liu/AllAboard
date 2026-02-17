package org.allaboard.project.domain

data class Itinerary(
    val tripId: String,
    val days: List<ItineraryDay>
)

data class ItineraryDay(
    val date: String,
    val dayNumber: Int,
    val activities: List<ScheduledActivity>
)

data class ScheduledActivity(
    val activity: Activity,
    val startTime: String,
    val endTime: String,
    val notes: String = ""
)
