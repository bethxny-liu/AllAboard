package org.allaboard.project.data.repository

import org.allaboard.project.domain.Trip
import org.allaboard.project.domain.User

interface TripRepository {
    suspend fun getTrip(tripId: String): Trip?
    suspend fun getAllTrips(): List<Trip>
    suspend fun getTripsForUser(userId: String): List<Trip>
    suspend fun createTrip(trip: Trip): Trip
    suspend fun updateTrip(trip: Trip): Trip
    suspend fun deleteTrip(tripId: String)
    suspend fun addMemberToTrip(tripId: String, user: User)
    suspend fun removeMemberFromTrip(tripId: String, userId: String)
}
