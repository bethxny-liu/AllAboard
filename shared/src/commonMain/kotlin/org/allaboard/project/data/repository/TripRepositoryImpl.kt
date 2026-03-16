package org.allaboard.project.data.repository

import io.ktor.client.plugins.ClientRequestException
import org.allaboard.project.data.network.ApiClient
import org.allaboard.project.domain.Trip

/**
 * Real TripRepository implementation backed by your backend server.
 *
 * All requests automatically include the Supabase JWT (if logged in).
 */
class TripRepositoryImpl : TripRepository {

    override suspend fun getTrip(tripId: String): Trip? {
        return try {
            ApiClient.get<Trip>("/trips/$tripId")
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 404) null else throw e
        }
    }

    override suspend fun getTripsForUser(): List<Trip> {
        return ApiClient.get<List<Trip>>("/trips")
    }

    override suspend fun createTrip(trip: Trip): Trip {
        return ApiClient.post<Trip, Trip>("/trips", trip)
    }

    override suspend fun updateTrip(trip: Trip): Trip {
        return ApiClient.patch<Trip, Trip>("/trips/${trip.id}", trip)
    }

    override suspend fun deleteTrip(tripId: String) {
        ApiClient.deleteNoBody("/trips/$tripId")
    }

    override suspend fun joinTrip(tripId: String) {
        ApiClient.postNoBody("/trips/$tripId/join")
    }

    override suspend fun removeMemberFromTrip(tripId: String, userId: String) {
        ApiClient.deleteNoBody("/trips/$tripId/members/$userId")
    }
}
