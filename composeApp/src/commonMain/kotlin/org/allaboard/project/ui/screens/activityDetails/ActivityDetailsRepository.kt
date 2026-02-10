package org.allaboard.project.ui.screens.activityDetails

/**
 * Source of activity/place details. Implement with stubbed data, OpenTripMap API, or another backend.
 * The UI layer depends only on this interface.
 */
interface ActivityDetailsRepository {
    /**
     * Fetches details for the given activity id.
     * @param activityId id of the activity (from trip home cards or API).
     * @param fallbackTitle optional title to use when the source has no name (e.g. from card).
     * @return [Result.success] with details, or [Result.failure] with an error message/exception.
     */
    suspend fun getDetails(activityId: String, fallbackTitle: String): Result<ActivityDetails>
}
