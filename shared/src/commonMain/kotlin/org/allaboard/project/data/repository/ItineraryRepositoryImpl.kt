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

    @kotlinx.serialization.Serializable
    private data class GoogleCalendarExportRequest(
        val googleAccessToken: String,
        val timeZone: String = "UTC",
        val calendarId: String = "primary"
    )

    @kotlinx.serialization.Serializable
    private data class GoogleCalendarExportResponse(
        val created: Int,
        val failed: Int
    )

    override suspend fun getItinerary(tripId: String): Itinerary? {
        return try {
            ApiClient.get<Itinerary>("/trips/$tripId/itinerary")
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 404) null else throw e
        }
    }

    override suspend fun regenerateItinerary(tripId: String): Itinerary? {
        return try {
            ApiClient.postNoBodyResponse<Itinerary>("/trips/$tripId/itinerary/regenerate")
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

    override suspend fun exportToGoogleCalendar(
        tripId: String,
        googleAccessToken: String,
        timeZone: String,
        calendarId: String
    ): Int {
        val response = ApiClient.post<GoogleCalendarExportRequest, GoogleCalendarExportResponse>(
            "/trips/$tripId/itinerary/export/google-calendar",
            GoogleCalendarExportRequest(
                googleAccessToken = googleAccessToken,
                timeZone = timeZone,
                calendarId = calendarId
            )
        )
        return response.created
    }
}
