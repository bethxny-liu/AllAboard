package org.allaboard.project.itinerary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItineraryDayRow(
    val id: String,
    @SerialName("trip_id") val tripId: String,
    @SerialName("day_date") val dayDate: String,
    @SerialName("day_number") val dayNumber: Int
)

@Serializable
data class ItineraryDayInsert(
    @SerialName("trip_id") val tripId: String,
    @SerialName("day_date") val dayDate: String,
    @SerialName("day_number") val dayNumber: Int
)


@Serializable
data class ScheduledActivityRow(
    val id: String,
    @SerialName("itinerary_day_id") val itineraryDayId: String,
    @SerialName("activity_id") val activityId: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val notes: String = "",
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class ScheduledActivityInsert(
    @SerialName("itinerary_day_id") val itineraryDayId: String,
    @SerialName("activity_id") val activityId: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val notes: String = "",
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class UpdateScheduledActivityRequest(
    val activityId: String,
    val startTime: String,
    val endTime: String,
    val notes: String = ""
)
