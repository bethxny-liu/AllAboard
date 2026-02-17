package org.allaboard.project.domain

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

enum class TripStatus {
    UPCOMING,
    ONGOING,
    COMPLETED
}

val Trip.dateRange: String
    get() = "$startDate - $endDate"
