package org.allaboard.project.domain

import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String,
    val title: String,
    val destination: String,
    val region: String,
    val startDate: String,
    val endDate: String,
    val imageUrl: String? = null,
    val status: TripStatus = TripStatus.UPCOMING,
    val members: List<User> = emptyList()
) {
    val memberCount: Int get() = members.size
}

@Serializable
enum class TripStatus {
    UPCOMING,
    ONGOING,
    COMPLETED
}

val Trip.dateRange: String
    get() = "$startDate - $endDate"

val Trip.displayDateRange: String
    get() = if (endDate.isBlank() || endDate == startDate) {
        startDate.toTripDisplayDate()
    } else {
        "${startDate.toTripDisplayDate()} - ${endDate.toTripDisplayDate()}"
    }

fun String.toTripDisplayDate(): String {
    // Convert ISO date strings (yyyy-MM-dd) to "Month d"
    val parts = trim().split("-")
    if (parts.size != 3) return this

    val monthNumber = parts[1].toIntOrNull() ?: return this
    val dayNumber = parts[2].toIntOrNull() ?: return this
    if (monthNumber !in 1..12) return this

    val monthName = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "June",
        "July", "Aug", "Sept", "Oct", "Nov", "Dec"
    )[monthNumber - 1]

    return "$monthName $dayNumber"
}
