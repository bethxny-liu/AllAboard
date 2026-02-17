

# All Aboard - Sprint 2 Implementation Plan

## Overview

This document outlines the complete architecture and implementation plan for Sprint 2 of the All Aboard travel planning application. The goal is to build a proper layered architecture with Domain, Data, and UI layers using mock repositories for Sprint 2, designed for easy migration to a real backend in Sprint 3+.

**Key Decisions:**
- ✅ Model layer included for frontend coordination (thin layer)
- ✅ No background sync - direct API calls are acceptable
- ✅ No separate membership class - Trip.members is List<User> (if user is in list, they are joined)
- ✅ Core business logic lives in BACKEND - frontend Model is a thin coordinator

---

## Business Logic Distribution: Frontend vs Backend

### Backend Handles (Authoritative Source of Truth):
| Logic | Reason |
|-------|--------|
| **Vote counting & confirmation** | Must be consistent across all users |
| **Activity recommendations** | Requires access to all user preferences + all activities |
| **Trip membership validation** | Security - can't trust client |
| **Data validation** | Prevent invalid data from being stored |
| **Itinerary conflict detection** | Requires full schedule visibility |
| **User authentication** | Security critical |

### Frontend Model Handles (UI Coordination Only):
| Logic | Reason |
|-------|--------|
| **Aggregating API responses** | Combine multiple endpoints for single screen |
| **UI state management** | Loading, errors, current selections |
| **Simple filtering for display** | Filter activities by category for tabs |
| **Caching decisions** | When to refetch vs use cached data |
| **Coordinating multiple repository calls** | Dashboard needs trips + activities + votes |

### Key Insight:
The frontend `TripPlannerModel` becomes a **thin coordinator** that:
1. Calls repository methods (which become API calls in Sprint 3)
2. Combines responses for UI consumption
3. Does NOT duplicate business logic that the backend will handle

The backend will expose endpoints that return **pre-computed results** (e.g., `GET /trips/{id}/voting-results` returns `ActivityVoteResult` directly, rather than raw votes that frontend calculates)

---

## Architecture Overview

### Final Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              UI LAYER (composeApp)                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │ HomeScreen  │  │TripHomeScreen│ │SwipingScreen│  │ProfileScreen│  ...   │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘        │
│         │                │                │                │                │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐        │
│  │HomeViewModel│  │TripHomeVM   │  │SwipingVM    │  │ProfileVM    │  ...   │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘        │
└─────────┼────────────────┼────────────────┼────────────────┼────────────────┘
          │                │                │                │
          └────────────────┴────────────────┴────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DOMAIN LAYER (shared)                             │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                       TripPlannerModel                               │   │
│  │  • Coordinates repositories                                          │   │
│  │  • Enforces business rules (voting logic, recommendations)           │   │
│  │  • Aggregates data from multiple sources                             │   │
│  │  • Single source of truth for business logic                         │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐         │
│  │  Trip    │ │ Activity │ │   User   │ │   Vote   │ │Itinerary │  ...    │
│  │(domain)  │ │(exists)  │ │(domain)  │ │(domain)  │ │(domain)  │         │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘         │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            DATA LAYER (shared)                              │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    Repository Interfaces                             │   │
│  │  TripRepository │ ActivityRepository │ VoteRepository │ UserRepository │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│              ┌─────────────────────┴─────────────────────┐                 │
│              ▼                                           ▼                  │
│  ┌─────────────────────────┐               ┌─────────────────────────┐     │
│  │   Mock Repositories     │               │   Real Repositories     │     │
│  │   (Sprint 2)            │               │   (Sprint 3+)           │     │
│  │   • Hard-coded data     │               │   • API calls           │     │
│  │   • Simulated delays    │               │   • Local caching       │     │
│  └─────────────────────────┘               └─────────────────────────┘     │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Project Structure

### Where Code Lives

```
team-102-8/
├── shared/src/commonMain/kotlin/org/allaboard/project/
│   ├── domain/                          ← DOMAIN CLASSES + MODEL
│   │   ├── Activity.kt                  ← Already exists
│   │   ├── Trip.kt                      ← NEW
│   │   ├── User.kt                      ← NEW
│   │   ├── Vote.kt                      ← NEW
│   │   ├── Itinerary.kt                 ← NEW
│   │   └── TripPlannerModel.kt          ← NEW (Business Logic)
│   │
│   └── data/                            ← DATA LAYER
│       └── repository/
│           ├── TripRepository.kt        ← Interface
│           ├── ActivityRepository.kt    ← Interface
│           ├── VoteRepository.kt        ← Interface
│           ├── UserRepository.kt        ← Interface
│           ├── ItineraryRepository.kt   ← Interface
│           │
│           └── mock/                    ← Sprint 2 implementations
│               ├── MockTripRepository.kt
│               ├── MockActivityRepository.kt
│               ├── MockVoteRepository.kt
│               ├── MockUserRepository.kt
│               └── MockItineraryRepository.kt
│
├── composeApp/src/commonMain/kotlin/org/allaboard/project/
│   ├── di/
│   │   └── AppModule.kt                 ← Dependency Injection
│   │
│   └── ui/screens/                      ← Already exists, refactor ViewModels
│       ├── home/
│       ├── tripHome/
│       ├── onboarding/
│       └── ...
│
└── server/src/main/kotlin/              ← Backend (Sprint 3+)
    └── ...
```

---

## Part 1: Domain Classes

### 1.1 Trip.kt (NEW)

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/domain/Trip.kt
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

// Helper extension
val Trip.dateRange: String
    get() = "$startDate - $endDate"
```

### 1.2 User.kt (NEW)

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/domain/User.kt
package org.allaboard.project.domain

data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val budget: BudgetLevel = BudgetLevel.MEDIUM,
    val travelVibe: TravelVibe = TravelVibe.BALANCED,
    val interests: Set<String> = emptySet(),
    val imageUrl: String? = null
)

enum class BudgetLevel {
    LOW,
    MEDIUM,
    HIGH;

    val symbol: String
        get() = when (this) {
            LOW -> "$"
            MEDIUM -> "$$"
            HIGH -> "$$$"
        }
}

enum class TravelVibe {
    RELAXED,
    ADVENTUROUS,
    BALANCED;

    companion object {
        fun fromString(value: String?): TravelVibe? = when (value?.lowercase()) {
            "relaxed" -> RELAXED
            "adventurous" -> ADVENTUROUS
            "balanced" -> BALANCED
            else -> null
        }
    }
}
```

### 1.3 Vote.kt (NEW)

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/domain/Vote.kt
package org.allaboard.project.domain

data class Vote(
    val id: String,
    val activityId: String,
    val userId: String,
    val tripId: String,
    val voteType: VoteType,
    val timestamp: Long
)

enum class VoteType {
    YES,
    NO,
    SKIP
}

/**
 * Aggregated voting results for display
 */
data class ActivityVoteResult(
    val activity: Activity,
    val yesVotes: Int,
    val noVotes: Int,
    val totalVotes: Int,
    val yesPercentage: Float,
    val isComplete: Boolean,      // All members have voted
    val isConfirmed: Boolean,     // >50% yes votes AND complete
    val voterNames: List<String>  // Names of users who voted yes
)
```

### 1.4 Itinerary.kt (NEW)

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/domain/Itinerary.kt
package org.allaboard.project.domain

data class Itinerary(
    val tripId: String,
    val days: List<ItineraryDay>
)

data class ItineraryDay(
    val date: String,
    val dayNumber: Int,
    val activities: List<ScheduledActivity>
)

data class ScheduledActivity(
    val activity: Activity,
    val startTime: String,
    val endTime: String,
    val notes: String = ""
)
```

### 1.5 Activity.kt (ALREADY EXISTS - No Changes Needed)

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/domain/Activity.kt
// Already exists with: id, title, location, description, rating, priceLevel, 
// mapPinLabel, voteCount, imageUrl, type (ActivityType enum)
```

---

## Part 2: Repository Interfaces

### 2.1 TripRepository.kt

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/data/repository/TripRepository.kt
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
```

### 2.2 ActivityRepository.kt

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/data/repository/ActivityRepository.kt
package org.allaboard.project.data.repository

import org.allaboard.project.domain.Activity

interface ActivityRepository {
    suspend fun getActivity(activityId: String): Activity?
    suspend fun getActivitiesForTrip(tripId: String): List<Activity>
    suspend fun addActivity(tripId: String, activity: Activity)
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(activityId: String)
    
    // Backend computes recommendations based on user preferences
    suspend fun getRecommendedActivities(tripId: String, userId: String): List<Activity>
}
```

### 2.3 VoteRepository.kt

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/data/repository/VoteRepository.kt
package org.allaboard.project.data.repository

import org.allaboard.project.domain.Vote
import org.allaboard.project.domain.ActivityVoteResult

interface VoteRepository {
    suspend fun submitVote(vote: Vote)
    
    // Backend computes and returns the result directly
    suspend fun getVotingResultsForTrip(tripId: String): List<ActivityVoteResult>
    suspend fun getVotingResultForActivity(tripId: String, activityId: String): ActivityVoteResult
    
    // For checking if user needs to vote (swiping screen)
    suspend fun getUnvotedActivityIds(tripId: String, userId: String): List<String>
}
```

### 2.4 UserRepository.kt

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/data/repository/UserRepository.kt
package org.allaboard.project.data.repository

import org.allaboard.project.domain.User
import org.allaboard.project.domain.BudgetLevel
import org.allaboard.project.domain.TravelVibe

interface UserRepository {
    suspend fun getCurrentUser(): User?
    suspend fun getUser(userId: String): User?
    suspend fun updateUserPreferences(
        userId: String,
        budget: BudgetLevel,
        vibe: TravelVibe,
        interests: Set<String>
    )
    suspend fun updateUserProfile(user: User)
}
```

### 2.5 ItineraryRepository.kt

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/data/repository/ItineraryRepository.kt
package org.allaboard.project.data.repository

import org.allaboard.project.domain.Itinerary
import org.allaboard.project.domain.ScheduledActivity

interface ItineraryRepository {
    suspend fun getItinerary(tripId: String): Itinerary?
    suspend fun addActivityToDay(tripId: String, date: String, scheduledActivity: ScheduledActivity)
    suspend fun removeActivityFromDay(tripId: String, date: String, activityId: String)
    suspend fun updateScheduledActivity(tripId: String, date: String, scheduledActivity: ScheduledActivity)
}
```

---

## Part 3: TripPlannerModel (Thin Frontend Coordinator)

The Model is now a **thin coordinator** that:
- Delegates business logic to backend (via repositories)
- Combines multiple repository calls for UI screens
- Does NOT duplicate logic that backend handles

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/domain/TripPlannerModel.kt
package org.allaboard.project.domain

import org.allaboard.project.data.repository.*

/**
 * Thin coordinator layer for the frontend.
 * 
 * Key principle: Business logic lives in BACKEND.
 * This class only:
 * 1. Coordinates multiple repository calls
 * 2. Combines data for UI consumption
 * 3. Manages what to fetch and when
 * 
 * Does NOT:
 * - Calculate vote percentages (backend does this)
 * - Determine if activity is confirmed (backend does this)
 * - Filter recommendations (backend does this)
 */
class TripPlannerModel(
    private val tripRepository: TripRepository,
    private val activityRepository: ActivityRepository,
    private val voteRepository: VoteRepository,
    private val userRepository: UserRepository,
    private val itineraryRepository: ItineraryRepository
) {
    // ========================================
    // TRIP OPERATIONS (Simple delegation)
    // ========================================

    suspend fun getTrip(tripId: String): Trip? {
        return tripRepository.getTrip(tripId)
    }

    suspend fun getAllTripsForUser(userId: String): List<Trip> {
        return tripRepository.getTripsForUser(userId)
    }

    suspend fun getUpcomingTrips(userId: String): List<Trip> {
        // Simple filtering for UI - could also be a backend endpoint
        return getAllTripsForUser(userId).filter { it.status == TripStatus.UPCOMING }
    }

    suspend fun getPastTrips(userId: String): List<Trip> {
        return getAllTripsForUser(userId).filter { it.status == TripStatus.COMPLETED }
    }

    suspend fun createTrip(
        destination: String,
        region: String,
        startDate: String,
        endDate: String,
        creatorId: String
    ): Trip {
        // Backend handles member creation and validation
        val trip = Trip(
            id = "", // Backend generates ID
            title = "All Aboard to $destination!",
            destination = destination,
            region = region,
            startDate = startDate,
            endDate = endDate,
            members = emptyList() // Backend adds creator as member
        )
        return tripRepository.createTrip(trip)
    }

    suspend fun addUserToTrip(tripId: String, userId: String) {
        val user = userRepository.getUser(userId) ?: return
        tripRepository.addMemberToTrip(tripId, user)
    }

    // ========================================
    // ACTIVITY OPERATIONS
    // ========================================

    suspend fun getActivitiesForTrip(tripId: String): List<Activity> {
        return activityRepository.getActivitiesForTrip(tripId)
    }

    suspend fun addActivityToTrip(tripId: String, activity: Activity) {
        activityRepository.addActivity(tripId, activity)
    }

    // ========================================
    // VOTING OPERATIONS
    // Backend computes all vote logic - we just delegate
    // ========================================

    /**
     * Submit a vote. Backend handles:
     * - Storing the vote
     * - Recalculating vote percentages
     * - Determining if activity is confirmed
     * - Adding to itinerary if confirmed
     */
    suspend fun voteOnActivity(
        tripId: String,
        activityId: String,
        userId: String,
        voteType: VoteType
    ): ActivityVoteResult {
        val vote = Vote(
            id = "", // Backend generates ID
            activityId = activityId,
            userId = userId,
            tripId = tripId,
            voteType = voteType,
            timestamp = System.currentTimeMillis()
        )
        voteRepository.submitVote(vote)
        
        // Backend returns the updated result
        return voteRepository.getVotingResultForActivity(tripId, activityId)
    }

    /**
     * Get voting results - backend computes percentages, confirmation status, etc.
     */
    suspend fun getVotingResults(tripId: String): List<ActivityVoteResult> {
        return voteRepository.getVotingResultsForTrip(tripId)
    }

    /**
     * Get activities user hasn't voted on - backend filters
     */
    suspend fun getUnvotedActivities(tripId: String, userId: String): List<Activity> {
        val unvotedIds = voteRepository.getUnvotedActivityIds(tripId, userId)
        val allActivities = activityRepository.getActivitiesForTrip(tripId)
        return allActivities.filter { it.id in unvotedIds }
    }

    /**
     * Get recommended activities - backend computes based on user preferences
     */
    suspend fun getRecommendedActivities(tripId: String, userId: String): List<Activity> {
        return activityRepository.getRecommendedActivities(tripId, userId)
    }

    // ========================================
    // USER OPERATIONS
    // ========================================

    suspend fun getCurrentUser(): User? {
        return userRepository.getCurrentUser()
    }

    suspend fun updateUserPreferences(
        userId: String,
        budget: BudgetLevel,
        vibe: TravelVibe,
        interests: Set<String>
    ) {
        userRepository.updateUserPreferences(userId, budget, vibe, interests)
    }

    // ========================================
    // ITINERARY OPERATIONS
    // ========================================

    suspend fun getItinerary(tripId: String): Itinerary? {
        return itineraryRepository.getItinerary(tripId)
    }

    // ========================================
    // DASHBOARD - Combines multiple calls for UI
    // This is the main value of the Model layer
    // ========================================

    /**
     * Get all data needed for TripHomeScreen in one call.
     * This is where the Model adds value - coordinating multiple fetches.
     */
    suspend fun getTripDashboard(tripId: String): TripDashboard {
        val trip = tripRepository.getTrip(tripId)
        val activities = activityRepository.getActivitiesForTrip(tripId)
        val votingResults = voteRepository.getVotingResultsForTrip(tripId)
        val itinerary = itineraryRepository.getItinerary(tripId)

        return TripDashboard(
            trip = trip,
            activities = activities,
            votingResults = votingResults,
            itinerary = itinerary
        )
    }
}

/**
 * Aggregated data for TripHomeScreen
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
}
```

---

## Part 4: Mock Repository Implementations

### 4.1 MockTripRepository.kt

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/data/repository/mock/MockTripRepository.kt
package org.allaboard.project.data.repository.mock

import kotlinx.coroutines.delay
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
            status = TripStatus.COMPLETED,
            members = listOf(mockUsers[0], mockUsers[4]) // Daniel, Alex
        )
        )
    )

    override suspend fun getTrip(tripId: String): Trip? {
        delay(100)
        return trips.find { it.id == tripId }
    }

    override suspend fun getAllTrips(): List<Trip> {
        delay(100)
        return trips.toList()
    }

    override suspend fun getTripsForUser(userId: String): List<Trip> {
        delay(100)
        return trips.filter { trip ->
            trip.members.any { it.id == userId }
        }
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

    override suspend fun addMemberToTrip(tripId: String, user: User) {
        delay(100)
        val trip = trips.find { it.id == tripId } ?: return
        if (trip.members.none { it.id == user.id }) {
            val updatedTrip = trip.copy(members = trip.members + user)
            updateTrip(updatedTrip)
        }
    }

    override suspend fun removeMemberFromTrip(tripId: String, userId: String) {
        delay(100)
        val trip = trips.find { it.id == tripId } ?: return
        val updatedTrip = trip.copy(members = trip.members.filter { it.id != userId })
        updateTrip(updatedTrip)
    }
}
```

### 4.2 MockActivityRepository.kt

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/data/repository/mock/MockActivityRepository.kt
package org.allaboard.project.data.repository.mock

import kotlinx.coroutines.delay
import org.allaboard.project.data.repository.ActivityRepository
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.domain.BudgetLevel

class MockActivityRepository(
    private val userRepository: MockUserRepository? = null
) : ActivityRepository {
    private val activities = mutableListOf(
        Activity(
            id = "act-1",
            title = "Senso-ji Temple",
            location = "Asakusa, Tokyo",
            description = "Historic temple with vibrant market streets and iconic Kaminarimon gate.",
            rating = 4.7f,
            priceLevel = "$$",
            mapPinLabel = "Senso-ji",
            voteCount = 0,
            type = ActivityType.LANDMARK
        ),
        Activity(
            id = "act-2",
            title = "Ichiran Ramen",
            location = "Shibuya, Tokyo",
            description = "Solo-booth ramen experience known for rich tonkotsu broth.",
            rating = 4.5f,
            priceLevel = "$",
            mapPinLabel = "Ichiran",
            voteCount = 0,
            type = ActivityType.RESTAURANT
        ),
        Activity(
            id = "act-3",
            title = "Mount Fuji Day Trip",
            location = "Yamanashi Prefecture",
            description = "Scenic day trip with lake views and photo spots near Fuji.",
            rating = 4.8f,
            priceLevel = "$$$",
            mapPinLabel = "Fuji",
            voteCount = 0,
            type = ActivityType.EXPERIENCES
        ),
        Activity(
            id = "act-4",
            title = "Shibuya Crossing",
            location = "Shibuya, Tokyo",
            description = "World's busiest pedestrian crossing and iconic Tokyo landmark.",
            rating = 4.6f,
            priceLevel = "$",
            mapPinLabel = "Shibuya",
            voteCount = 0,
            type = ActivityType.LANDMARK
        ),
        Activity(
            id = "act-5",
            title = "Tsukiji Outer Market",
            location = "Chuo City, Tokyo",
            description = "Fresh sushi and seafood market experience.",
            rating = 4.7f,
            priceLevel = "$$",
            mapPinLabel = "Tsukiji",
            voteCount = 0,
            type = ActivityType.RESTAURANT
        )
    )

    private val tripActivities = mutableMapOf(
        "trip-1" to mutableListOf("act-1", "act-2", "act-3", "act-4", "act-5")
    )

    override suspend fun getActivity(activityId: String): Activity? {
        delay(50)
        return activities.find { it.id == activityId }
    }

    override suspend fun getActivitiesForTrip(tripId: String): List<Activity> {
        delay(100)
        val activityIds = tripActivities[tripId] ?: return emptyList()
        return activities.filter { it.id in activityIds }
    }

    override suspend fun addActivity(tripId: String, activity: Activity) {
        delay(100)
        activities.add(activity)
        tripActivities.getOrPut(tripId) { mutableListOf() }.add(activity.id)
    }

    override suspend fun updateActivity(activity: Activity) {
        delay(50)
        val index = activities.indexOfFirst { it.id == activity.id }
        if (index != -1) {
            activities[index] = activity
        }
    }

    override suspend fun deleteActivity(activityId: String) {
        delay(50)
        activities.removeAll { it.id == activityId }
    }

    /**
     * Simulates backend recommendation logic.
     * In Sprint 3, this will be a backend API call.
     */
    override suspend fun getRecommendedActivities(tripId: String, userId: String): List<Activity> {
        delay(100)
        val user = userRepository?.getUser(userId) ?: return getActivitiesForTrip(tripId)
        val tripActivitiesList = getActivitiesForTrip(tripId)
        
        return tripActivitiesList.filter { activity ->
            matchesBudget(activity, user.budget)
        }
    }

    private fun matchesBudget(activity: Activity, budget: BudgetLevel): Boolean {
        return when (budget) {
            BudgetLevel.LOW -> activity.priceLevel in listOf("$", "$$")
            BudgetLevel.MEDIUM -> activity.priceLevel in listOf("$$", "$$$")
            BudgetLevel.HIGH -> true
        }
    }
}
```

### 4.3 MockVoteRepository.kt

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/data/repository/mock/MockVoteRepository.kt
package org.allaboard.project.data.repository.mock

import kotlinx.coroutines.delay
import org.allaboard.project.data.repository.VoteRepository
import org.allaboard.project.domain.*

/**
 * Mock implementation that simulates backend vote computation.
 * In Sprint 3, the real repository will call backend API endpoints
 * that return pre-computed ActivityVoteResult.
 */
class MockVoteRepository(
    private val activityRepository: MockActivityRepository,
    private val tripRepository: MockTripRepository
) : VoteRepository {
    private val votes = mutableListOf<Vote>()

    override suspend fun submitVote(vote: Vote) {
        delay(100)
        // Remove existing vote from same user for same activity
        votes.removeAll { it.userId == vote.userId && it.activityId == vote.activityId }
        votes.add(vote)
    }

    override suspend fun getVotingResultsForTrip(tripId: String): List<ActivityVoteResult> {
        delay(100)
        val trip = tripRepository.getTrip(tripId) ?: return emptyList()
        val activities = activityRepository.getActivitiesForTrip(tripId)
        
        return activities.map { activity ->
            computeVotingResult(trip, activity)
        }.sortedByDescending { it.yesVotes }
    }

    override suspend fun getVotingResultForActivity(tripId: String, activityId: String): ActivityVoteResult {
        delay(50)
        val trip = tripRepository.getTrip(tripId)
        val activity = activityRepository.getActivity(activityId)
        
        return if (trip != null && activity != null) {
            computeVotingResult(trip, activity)
        } else {
            createEmptyResult(activityId)
        }
    }

    override suspend fun getUnvotedActivityIds(tripId: String, userId: String): List<String> {
        delay(50)
        val activities = activityRepository.getActivitiesForTrip(tripId)
        val userVotedActivityIds = votes
            .filter { it.userId == userId && it.tripId == tripId }
            .map { it.activityId }
            .toSet()
        
        return activities.map { it.id }.filter { it !in userVotedActivityIds }
    }

    /**
     * Simulates backend vote computation logic.
     * This logic will live in the backend in Sprint 3.
     */
    private fun computeVotingResult(trip: Trip, activity: Activity): ActivityVoteResult {
        val activityVotes = votes.filter { it.activityId == activity.id }
        
        val yesVotes = activityVotes.count { it.voteType == VoteType.YES }
        val noVotes = activityVotes.count { it.voteType == VoteType.NO }
        val totalMembers = trip.memberCount
        val totalVotes = activityVotes.size

        val yesPercentage = if (totalMembers > 0) yesVotes.toFloat() / totalMembers else 0f
        val isComplete = totalVotes >= totalMembers
        val isConfirmed = isComplete && yesPercentage > 0.5f

        val voterNames = activityVotes
            .filter { it.voteType == VoteType.YES }
            .mapNotNull { vote ->
                trip.members.find { it.userId == vote.userId }?.displayName
            }

        return ActivityVoteResult(
            activity = activity,
            yesVotes = yesVotes,
            noVotes = noVotes,
            totalVotes = totalVotes,
            yesPercentage = yesPercentage,
            isComplete = isComplete,
            isConfirmed = isConfirmed,
            voterNames = voterNames
        )
    }

    private fun createEmptyResult(activityId: String) = ActivityVoteResult(
        activity = Activity(
            id = activityId,
            title = "Unknown",
            location = "",
            description = "",
            mapPinLabel = "",
            voteCount = 0,
            type = ActivityType.LANDMARK
        ),
        yesVotes = 0,
        noVotes = 0,
        totalVotes = 0,
        yesPercentage = 0f,
        isComplete = false,
        isConfirmed = false,
        voterNames = emptyList()
    )
}
```

### 4.4 MockUserRepository.kt

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/data/repository/mock/MockUserRepository.kt
package org.allaboard.project.data.repository.mock

import kotlinx.coroutines.delay
import org.allaboard.project.data.repository.UserRepository
import org.allaboard.project.domain.*

class MockUserRepository : UserRepository {
    private var currentUserId = "user-1"

    private val users = mutableMapOf(
        "user-1" to User(
            id = "user-1",
            displayName = "Daniel",
            email = "daniel@allaboard.com",
            budget = BudgetLevel.MEDIUM,
            travelVibe = TravelVibe.BALANCED,
            interests = setOf("Culture", "Food", "Adventure")
        ),
        "user-2" to User(
            id = "user-2",
            displayName = "Rachael",
            email = "rachael@allaboard.com",
            budget = BudgetLevel.HIGH,
            travelVibe = TravelVibe.RELAXED,
            interests = setOf("Culture", "Fine Dining")
        ),
        "user-3" to User(
            id = "user-3",
            displayName = "Sarah",
            email = "sarah@allaboard.com",
            budget = BudgetLevel.MEDIUM,
            travelVibe = TravelVibe.ADVENTUROUS,
            interests = setOf("Adventure", "Nature")
        ),
        "user-4" to User(
            id = "user-4",
            displayName = "Bethany",
            email = "bethany@allaboard.com",
            budget = BudgetLevel.LOW,
            travelVibe = TravelVibe.BALANCED,
            interests = setOf("History", "Photography")
        )
    )

    override suspend fun getCurrentUser(): User? {
        delay(50)
        return users[currentUserId]
    }

    override suspend fun getUser(userId: String): User? {
        delay(50)
        return users[userId]
    }

    override suspend fun updateUserPreferences(
        userId: String,
        budget: BudgetLevel,
        vibe: TravelVibe,
        interests: Set<String>
    ) {
        delay(100)
        users[userId]?.let { user ->
            users[userId] = user.copy(
                budget = budget,
                travelVibe = vibe,
                interests = interests
            )
        }
    }

    override suspend fun updateUserProfile(user: User) {
        delay(100)
        users[user.id] = user
    }
}
```

### 4.5 MockItineraryRepository.kt

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/data/repository/mock/MockItineraryRepository.kt
package org.allaboard.project.data.repository.mock

import kotlinx.coroutines.delay
import org.allaboard.project.data.repository.ItineraryRepository
import org.allaboard.project.domain.*

class MockItineraryRepository : ItineraryRepository {
    private val itineraries = mutableMapOf<String, Itinerary>()

    override suspend fun getItinerary(tripId: String): Itinerary? {
        delay(100)
        return itineraries[tripId]
    }

    override suspend fun addActivityToDay(
        tripId: String,
        date: String,
        scheduledActivity: ScheduledActivity
    ) {
        delay(100)
        val itinerary = itineraries[tripId] ?: Itinerary(tripId, emptyList())
        val day = itinerary.days.find { it.date == date }
            ?: ItineraryDay(date, itinerary.days.size + 1, emptyList())

        val updatedDay = day.copy(activities = day.activities + scheduledActivity)
        val updatedDays = if (itinerary.days.any { it.date == date }) {
            itinerary.days.map { if (it.date == date) updatedDay else it }
        } else {
            itinerary.days + updatedDay
        }

        itineraries[tripId] = itinerary.copy(days = updatedDays)
    }

    override suspend fun removeActivityFromDay(tripId: String, date: String, activityId: String) {
        delay(100)
        val itinerary = itineraries[tripId] ?: return
        val updatedDays = itinerary.days.map { day ->
            if (day.date == date) {
                day.copy(activities = day.activities.filter { it.activity.id != activityId })
            } else {
                day
            }
        }
        itineraries[tripId] = itinerary.copy(days = updatedDays)
    }

    override suspend fun updateScheduledActivity(
        tripId: String,
        date: String,
        scheduledActivity: ScheduledActivity
    ) {
        delay(100)
        val itinerary = itineraries[tripId] ?: return
        val updatedDays = itinerary.days.map { day ->
            if (day.date == date) {
                day.copy(activities = day.activities.map {
                    if (it.activity.id == scheduledActivity.activity.id) scheduledActivity else it
                })
            } else {
                day
            }
        }
        itineraries[tripId] = itinerary.copy(days = updatedDays)
    }
}
```

---

## Part 5: Dependency Injection

### AppModule.kt

```kotlin
// composeApp/src/commonMain/kotlin/org/allaboard/project/di/AppModule.kt
package org.allaboard.project.di

import org.allaboard.project.data.repository.*
import org.allaboard.project.data.repository.mock.*
import org.allaboard.project.domain.TripPlannerModel

/**
 * Simple dependency injection module.
 * Sprint 2: Uses mock repositories that simulate backend behavior.
 * Sprint 3+: Switch to real API implementations.
 */
object AppModule {
    // Base repositories (no dependencies)
    private val mockUserRepository by lazy { MockUserRepository() }
    private val mockTripRepository by lazy { MockTripRepository() }
    
    // Repositories with dependencies (simulate backend data access)
    private val mockActivityRepository by lazy { 
        MockActivityRepository(mockUserRepository) 
    }
    private val mockVoteRepository by lazy { 
        MockVoteRepository(mockActivityRepository, mockTripRepository) 
    }
    private val mockItineraryRepository by lazy { MockItineraryRepository() }

    // Public interfaces
    val tripRepository: TripRepository get() = mockTripRepository
    val activityRepository: ActivityRepository get() = mockActivityRepository
    val voteRepository: VoteRepository get() = mockVoteRepository
    val userRepository: UserRepository get() = mockUserRepository
    val itineraryRepository: ItineraryRepository get() = mockItineraryRepository

    // Model (Thin coordinator) - Shared across all ViewModels
    val tripPlannerModel: TripPlannerModel by lazy {
        TripPlannerModel(
            tripRepository = tripRepository,
            activityRepository = activityRepository,
            voteRepository = voteRepository,
            userRepository = userRepository,
            itineraryRepository = itineraryRepository
        )
    }
}
```

---

## Part 6: Refactored ViewModel Examples

### TripHomeViewModel (Refactored)

```kotlin
// composeApp/src/commonMain/kotlin/org/allaboard/project/ui/screens/tripHome/TripHomeViewModel.kt
package org.allaboard.project.ui.screens.tripHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.di.AppModule
import org.allaboard.project.domain.*

data class TripHomeUiState(
    val trip: Trip? = null,
    val activities: List<Activity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TripHomeViewModel(
    private val model: TripPlannerModel = AppModule.tripPlannerModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripHomeUiState())
    val uiState: StateFlow<TripHomeUiState> = _uiState.asStateFlow()

    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val trip = model.getTrip(tripId)
                val activities = model.getActivitiesForTrip(tripId)

                _uiState.value = _uiState.value.copy(
                    trip = trip,
                    activities = activities,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
```

### SwipingViewModel (Refactored)

```kotlin
// composeApp/src/commonMain/kotlin/org/allaboard/project/ui/screens/tripHome/swipe/SwipingViewModel.kt
package org.allaboard.project.ui.screens.tripHome.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.di.AppModule
import org.allaboard.project.domain.*

data class SwipingUiState(
    val cards: List<Activity> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false
) {
    val currentCard: Activity? get() = cards.getOrNull(currentIndex)
    val hasMoreCards: Boolean get() = currentIndex < cards.size
}

class SwipingViewModel(
    private val model: TripPlannerModel = AppModule.tripPlannerModel,
    private val tripId: String,
    private val userId: String = "user-1" // TODO: Get from auth
) : ViewModel() {

    private val _uiState = MutableStateFlow(SwipingUiState())
    val uiState: StateFlow<SwipingUiState> = _uiState.asStateFlow()

    init {
        loadActivities()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val activities = model.getUnvotedActivities(tripId, userId)
            
            _uiState.value = _uiState.value.copy(
                cards = activities,
                currentIndex = 0,
                isLoading = false
            )
        }
    }

    fun swipeRight(activityId: String) {
        viewModelScope.launch {
            model.voteOnActivity(tripId, activityId, userId, VoteType.YES)
            moveToNextCard()
        }
    }

    fun swipeLeft(activityId: String) {
        viewModelScope.launch {
            model.voteOnActivity(tripId, activityId, userId, VoteType.NO)
            moveToNextCard()
        }
    }

    private fun moveToNextCard() {
        _uiState.value = _uiState.value.copy(
            currentIndex = _uiState.value.currentIndex + 1
        )
    }
}
```

---

## Part 7: Data Flow Summary

### Voting Flow (Direct Call - No Background Sync)

```
User swipes right
      │
      ▼
ViewModel.swipeRight(activityId)
      │
      ▼
Model.voteOnActivity(tripId, activityId, userId, VoteType.YES)
      │
      ├──► VoteRepository.submitVote(vote)
      │           │
      │           ▼
      │    [Sprint 2: Mock computes result in-memory]
      │    [Sprint 3: Backend API computes result]
      │
      ▼
VoteRepository.getVotingResultForActivity() ← Returns pre-computed result
      │
      ▼
Return ActivityVoteResult to ViewModel
      │
      ▼
Update UI State → Show next card
```

### Sprint 3+ Migration: Replace Mock → Real

```kotlin
// Just change AppModule.kt:
object AppModule {
    // Switch from Mock to Real implementations
    val tripRepository: TripRepository by lazy {
        RealTripRepository(apiService)  // ← Only change needed
    }
    
    // Model stays the same!
    val tripPlannerModel: TripPlannerModel by lazy {
        TripPlannerModel(
            tripRepository = tripRepository,
            // ... same as before
        )
    }
}
```

---

## Part 8: Backend API Design (Sprint 3 Preview)

This section outlines the backend API endpoints that will replace mock repositories. The key principle is that the **backend computes all business logic** and returns ready-to-display results.

### API Endpoints

#### Trips
```
GET    /api/trips                    → List<Trip>         (user's trips)
GET    /api/trips/{tripId}           → Trip
POST   /api/trips                    → Trip               (create trip)
PUT    /api/trips/{tripId}           → Trip               (update trip)
DELETE /api/trips/{tripId}           → void
POST   /api/trips/{tripId}/members   → User               (add user to trip)
```

#### Activities
```
GET    /api/trips/{tripId}/activities                    → List<Activity>
GET    /api/trips/{tripId}/activities/recommended        → List<Activity>  ← Backend filters by user prefs
POST   /api/trips/{tripId}/activities                    → Activity
PUT    /api/activities/{activityId}                      → Activity
DELETE /api/activities/{activityId}                      → void
```

#### Voting (Backend computes everything)
```
POST   /api/trips/{tripId}/activities/{activityId}/vote  → ActivityVoteResult  ← Returns computed result
GET    /api/trips/{tripId}/voting-results                → List<ActivityVoteResult>  ← Pre-computed
GET    /api/trips/{tripId}/unvoted-activities            → List<String>  ← Activity IDs user hasn't voted on
```

#### Users
```
GET    /api/users/me                 → User               (current user)
GET    /api/users/{userId}           → User
PUT    /api/users/me/preferences     → User               (update prefs)
```

#### Itinerary
```
GET    /api/trips/{tripId}/itinerary → Itinerary
POST   /api/trips/{tripId}/itinerary/days/{date}/activities → ScheduledActivity
DELETE /api/trips/{tripId}/itinerary/days/{date}/activities/{activityId} → void
```

### Key Backend Responsibilities

| Endpoint | Backend Computes |
|----------|------------------|
| `POST /vote` | Stores vote, recalculates percentages, checks if confirmed, returns `ActivityVoteResult` |
| `GET /voting-results` | Aggregates all votes, calculates percentages for each activity, sorts by popularity |
| `GET /recommended` | Filters activities by user budget, interests, excludes already-voted |
| `POST /trips` | Validates input, creates trip, adds creator as JOINED member |

### Example: Vote Endpoint Response

```json
// POST /api/trips/trip-1/activities/act-1/vote
// Request: { "voteType": "YES" }
// Response:
{
  "activity": {
    "id": "act-1",
    "title": "Senso-ji Temple",
    "location": "Asakusa, Tokyo"
  },
  "yesVotes": 3,
  "noVotes": 1,
  "totalVotes": 4,
  "yesPercentage": 0.75,
  "isComplete": true,
  "isConfirmed": true,
  "voterNames": ["Daniel", "Rachael", "Sarah"]
}
```

### Real Repository Implementation (Sprint 3)

```kotlin
// shared/src/commonMain/kotlin/org/allaboard/project/data/repository/impl/RealVoteRepository.kt
class RealVoteRepository(
    private val apiService: AllAboardApiService
) : VoteRepository {
    
    override suspend fun submitVote(vote: Vote) {
        apiService.submitVote(vote.tripId, vote.activityId, VoteRequest(vote.voteType))
    }
    
    override suspend fun getVotingResultsForTrip(tripId: String): List<ActivityVoteResult> {
        // Backend returns pre-computed results
        return apiService.getVotingResults(tripId)
    }
    
    override suspend fun getVotingResultForActivity(tripId: String, activityId: String): ActivityVoteResult {
        // Could be part of submitVote response, or separate call
        return apiService.getVotingResults(tripId).find { it.activity.id == activityId }
            ?: throw ActivityNotFoundException()
    }
    
    override suspend fun getUnvotedActivityIds(tripId: String, userId: String): List<String> {
        return apiService.getUnvotedActivities(tripId)
    }
}
```

---

## Implementation Checklist

### Week 1: Domain + Data Layer

#### Domain Classes (shared/domain/)
- [ ] Create `Trip.kt`
- [ ] Create `User.kt`
- [ ] Create `Vote.kt`
- [ ] Create `Itinerary.kt`
- [ ] Create `TripPlannerModel.kt`
- [ ] Create `TripDashboard.kt` (or include in TripPlannerModel.kt)

#### Repository Interfaces (shared/data/repository/)
- [ ] Create `TripRepository.kt`
- [ ] Create `ActivityRepository.kt`
- [ ] Create `VoteRepository.kt`
- [ ] Create `UserRepository.kt`
- [ ] Create `ItineraryRepository.kt`

#### Mock Implementations (shared/data/repository/mock/)
- [ ] Create `MockTripRepository.kt`
- [ ] Create `MockActivityRepository.kt`
- [ ] Create `MockVoteRepository.kt`
- [ ] Create `MockUserRepository.kt`
- [ ] Create `MockItineraryRepository.kt`

#### DI (composeApp/di/)
- [ ] Create `AppModule.kt`

### Week 2: Integration + Testing

#### Refactor ViewModels
- [ ] Refactor `TripHomeViewModel` to use `TripPlannerModel`
- [ ] Refactor `SwipingViewModel` to use `TripPlannerModel`
- [ ] Refactor `HomeViewModel` to use `TripPlannerModel`
- [ ] Refactor `ProfileViewModel` to use `TripPlannerModel`
- [ ] Remove ALL hard-coded data from ViewModels

#### Unit Tests (shared/commonTest/ and composeApp/commonTest/)
- [ ] Write tests for domain classes (Trip, User, Vote)
- [ ] Write tests for `TripPlannerModel` (voting logic, recommendations)
- [ ] Write tests for ViewModels (state updates, error handling)

---

## Testing Requirements

Aim for **40+ unit tests**:

| Category | Count | Focus Areas |
|----------|-------|-------------|
| Domain Classes | 10 | Trip creation, Vote calculations, User preferences |
| TripPlannerModel | 15 | Voting logic, trip operations, activity filtering |
| ViewModels | 15 | State updates, loading states, error handling |

---

## Summary

This plan implements a clean layered architecture with **backend-first business logic**:

- **UI Layer (composeApp):** Screens + ViewModels that manage UI state only
- **Domain Layer (shared):** TripPlannerModel (thin coordinator) + domain classes
- **Data Layer (shared):** Repository interfaces + mock implementations (simulate backend)
- **Backend (Sprint 3):** Handles all business logic (voting, recommendations, validation)

### Business Logic Distribution

| Layer | Responsibility |
|-------|----------------|
| **Backend** | Vote counting, confirmation logic, recommendations, validation, authorization |
| **Frontend Model** | Coordinate repository calls, combine data for UI, simple display filtering |
| **Frontend ViewModel** | UI state management, user interactions, formatting for display |

### Key Benefits:
1. **Backend is authoritative** - business logic computed server-side
2. **Mock repos simulate backend** - same interface, easy Sprint 3 migration  
3. **Frontend Model is thin** - just coordinates, doesn't duplicate backend logic
4. **Easy to test** - mock repositories return pre-computed results
5. **API-ready** - repository interfaces match backend endpoint design

### Sprint 3 Migration Path:
1. Replace `MockVoteRepository` → `RealVoteRepository` (calls `/api/trips/{id}/voting-results`)
2. Replace `MockActivityRepository` → `RealActivityRepository` (calls `/api/trips/{id}/activities/recommended`)
3. Model and ViewModels remain unchanged!

Ready to begin implementation? Start with Week 1 tasks in the checklist above.
