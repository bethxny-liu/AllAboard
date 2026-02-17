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
) {
    val confirmedActivities: List<Activity>
        get() = votingResults.filter { it.isConfirmed }.map { it.activity }

    val pendingActivities: List<Activity>
        get() = votingResults.filter { !it.isComplete }.map { it.activity }

    val rejectedActivities: List<Activity>
        get() = votingResults.filter { it.isComplete && !it.isConfirmed }.map { it.activity }
}
