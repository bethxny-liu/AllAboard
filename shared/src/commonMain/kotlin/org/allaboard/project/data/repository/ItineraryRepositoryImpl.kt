package org.allaboard.project.data.repository

import io.ktor.client.plugins.ClientRequestException
import org.allaboard.project.data.network.ApiClient
import org.allaboard.project.domain.Itinerary
import org.allaboard.project.domain.ScheduledActivity

/**
 * Real ItineraryRepository implementation backed by your backend server.
 */
class ItineraryRepositoryImpl : ItineraryRepository {

    @kotlinx.serialization.Serializable
    private data class UpdateScheduledActivityRequest(
        val activityId: String,
        val startTime: String,
        val endTime: String,
        val notes: String = ""
    )

    override suspend fun getItinerary(tripId: String): Itinerary? {
        return try {
            ApiClient.get<Itinerary>("/trips/$tripId/itinerary")
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 404) null else throw e
        }
    }

    override suspend fun updateScheduledActivity(tripId: String, date: String, scheduledActivity: ScheduledActivity) {
        val body = UpdateScheduledActivityRequest(
            activityId = scheduledActivity.activity.id,
            startTime = scheduledActivity.startTime,
            endTime = scheduledActivity.endTime,
            notes = scheduledActivity.notes
        )

        // Server returns 204 No Content
        ApiClient.patch<UpdateScheduledActivityRequest, String>(
            "/trips/$tripId/itinerary/days/$date/activities",
            body
        )
    }
}
