package org.allaboard.project.domain

/**
 * Aggregated data for TripHomeScreen.
 * Combines data from multiple sources for efficient UI rendering.
 */
data class TripDashboard(
    val trip: Trip?,
    val activities: List<Activity>,
    val votingResults: List<ActivityVoteResult>,
    val itinerary: Itinerary?
)
