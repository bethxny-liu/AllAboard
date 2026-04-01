package org.allaboard.project.data.repository.mock

import kotlinx.coroutines.delay
import kotlinx.serialization.modules.EmptySerializersModule
import org.allaboard.project.data.repository.TripRepository
import org.allaboard.project.domain.*

class MockTripRepository : TripRepository {
    // Pre-defined users for mock data
    private val mockUsers = listOf(
        User("user-1", "Daniel", "daniel@allaboard.com", BudgetLevel.MEDIUM, TravelVibe.BALANCED, setOf("Culture", "Food")),
        User("user-2", "Rachael", "rachael@allaboard.com", BudgetLevel.HIGH, TravelVibe.RELAXED, setOf("Culture")),
        User("user-3", "Sarah", "sarah@allaboard.com", BudgetLevel.MEDIUM, TravelVibe.ADVENTUROUS, setOf("Adventure")),
        User("user-4", "Bethany", "bethany@allaboard.com", BudgetLevel.LOW, TravelVibe.BALANCED, setOf("History")),
        User("user-5", "Alex", "alex@allaboard.com", BudgetLevel.HIGH, TravelVibe.ADVENTUROUS, setOf("Nature"))
    )

    private val trips = mutableListOf(
        Trip(
            id = "trip-1",
            title = "All Aboard to Japan!",
            destination = "Japan",
            region = "Tokyo",
            startDate = "Dec 15",
            endDate = "Jan 22",
            imageUrl = null,
            status = TripStatus.UPCOMING,
            members = mockUsers.take(4) // Daniel, Rachael, Sarah, Bethany
        ),
        Trip(
            id = "trip-2",
            title = "All Aboard to Paris!",
            destination = "France",
            region = "Paris",
            startDate = "Mar 10",
            endDate = "Mar 20",
            imageUrl = "https://res.klook.com/image/upload/fl_lossy.progressive,q_60/Mobile/City/swox6wjsl5ndvkv5jvum.jpg",
            status = TripStatus.COMPLETED,
            members = listOf(mockUsers[0], mockUsers[4]) // Daniel, Alex
        )
    )

    override suspend fun getTrip(tripId: String): Trip? {
        delay(100)
        return trips.find { it.id == tripId }
    }

    override suspend fun getTripsForUser(): List<Trip> {
        delay(100)
        return trips.toList()
    }

    override suspend fun createTrip(trip: Trip): Trip {
        delay(200)
        val newTrip = trip.copy(id = "trip-${trips.size + 1}")
        trips.add(newTrip)
        return newTrip
    }

    override suspend fun updateTrip(trip: Trip): Trip {
        delay(100)
        val index = trips.indexOfFirst { it.id == trip.id }
        if (index != -1) {
            trips[index] = trip
        }
        return trip
    }

    override suspend fun deleteTrip(tripId: String) {
        delay(100)
        trips.removeAll { it.id == tripId }
    }

    override suspend fun joinTrip(tripId: String) {
        // do nothing in mock
        return
    }
    override suspend fun removeMemberFromTrip(tripId: String, userId: String) {
        delay(100)
        val trip = trips.find { it.id == tripId } ?: return
        val updatedTrip = trip.copy(members = trip.members.filter { it.id != userId })
        updateTrip(updatedTrip)
    }

    override suspend fun getTripDashboard(tripId: String): TripDashboard {
        delay(100)
        val trip = trips.find { it.id == tripId } ?: throw IllegalArgumentException("Trip not found")
        return TripDashboard(
            trip = trip,
            activities = emptyList(),
            votingResults = emptyList(), // For simplicity, no voting results in mock
            itinerary = null
        )
    }
}
