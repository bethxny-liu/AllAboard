package org.allaboard.project.domain

import org.allaboard.project.data.repository.ActivityRepository
import org.allaboard.project.data.repository.DatabaseRepository
import org.allaboard.project.data.repository.GoogleOAuthTokenStore
import org.allaboard.project.data.repository.ItineraryRepository
import org.allaboard.project.data.repository.TripRepository
import org.allaboard.project.data.repository.UserRepository
import org.allaboard.project.data.repository.VoteRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

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
class AllAboardModel(
    private val tripRepository: TripRepository,
    private val activityRepository: ActivityRepository,
    private val voteRepository: VoteRepository,
    private val userRepository: UserRepository,
    private val itineraryRepository: ItineraryRepository,
    private val databaseRepository: DatabaseRepository
) {
    // Events to notify viewmodels about backend changes (e.g., votes submitted)
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val events: SharedFlow<String> = _events.asSharedFlow()
    private val itineraryDirtyTrips = mutableSetOf<String>()
    private val itineraryLocks = mutableMapOf<String, Mutex>()
    private val itineraryLocksGuard = Mutex()

    private suspend fun itineraryLockFor(tripId: String): Mutex {
        return itineraryLocksGuard.withLock {
            itineraryLocks.getOrPut(tripId) { Mutex() }
        }
    }

    // ========================================
    // AUTH / LOGIN OPERATIONS
    // ========================================

    /**
     * Initiates Google OAuth sign-in via Supabase.
     * Opens the system browser for the Google consent screen.
     */
    suspend fun signInWithGoogle() {
        databaseRepository.signInWithGoogle()
    }

    suspend fun logout() {
        databaseRepository.logout()
        println("Logged out from Supabase")
        userRepository.clearCache()
    }

    // ========================================
    // TRIP OPERATIONS (Simple delegation)
    // ========================================

    suspend fun getTrip(tripId: String): Trip? {
        return tripRepository.getTrip(tripId)
    }

    suspend fun getAllTripsForUser(): List<Trip> {
        return tripRepository.getTripsForUser()
    }

    /** Upcoming = trip has not ended yet (endDate >= today). Ignores TripStatus since it doesn't auto-update. */
    suspend fun getUpcomingTrips(trips: List<Trip>): List<Trip> {
        val today = kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.UTC).toString()
        return trips.filter { it.endDate >= today }
    }

    /** Past = trip has ended (endDate < today). Ignores TripStatus since it doesn't auto-update. */
    suspend fun getPastTrips(trips: List<Trip>): List<Trip> {
        val today = kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.UTC).toString()
        return trips.filter { it.endDate < today }
    }

    suspend fun createTrip(
        destination: String,
        region: String,
        startDate: String,
        endDate: String,
        imageUrl: String? = null,
        tripId: String? = null
    ): Trip {
        val trip = Trip(
            id = tripId ?: "",
            title = "All Aboard to $destination!",
            destination = destination,
            region = region,
            startDate = startDate,
            endDate = endDate,
            imageUrl = imageUrl,
            // Backend derives creator/member from JWT; frontend should not depend on /user/me here.
            members = emptyList()
        )
        val createdTrip = tripRepository.createTrip(trip)

        // Notify listeners that a trip was created so UI can refresh
        _events.emit(createdTrip.id)

        return createdTrip
    }

    suspend fun updateTripDetails(
        tripId: String,
        destination: String,
        region: String,
        startDate: String,
        endDate: String,
        imageUrl: String? = null
    ): Trip? {
        val existingTrip = tripRepository.getTrip(tripId) ?: return null
        val updatedTrip = existingTrip.copy(
            title = "All Aboard to $destination!",
            destination = destination,
            region = region,
            startDate = startDate,
            endDate = endDate,
            imageUrl = imageUrl
        )
        val result = tripRepository.updateTrip(updatedTrip)

        // Notify listeners that trip was updated so UI can refresh
        _events.emit(tripId)

        return result
    }

    suspend fun joinTrip(tripId: String) {
        tripRepository.joinTrip(tripId)
        _events.emit(tripId)
    }

    suspend fun removeMemberFromTrip(tripId: String, userId: String) {
        tripRepository.removeMemberFromTrip(tripId, userId)
        _events.emit(tripId)
    }

    fun getTripInviteLink(tripId: String): String {
        return "AllAboard.ca/join/$tripId"
    }

    suspend fun deleteTrip(tripId: String) {
        tripRepository.deleteTrip(tripId)
        _events.emit(tripId)
    }

    // ========================================
    // ACTIVITY OPERATIONS
    // ========================================

    suspend fun getActivity(activityId: String): Activity? {
        return activityRepository.getActivity(activityId)
    }

    suspend fun createActivityForTrip(
        tripId: String,
        title: String,
        location: String,
        description: String,
        type: ActivityType?,
        imageUrl: String? = null,
        link: String? = null
    ): Activity {
        val activity = Activity(
            title = title,
            location = location,
            description = description,
            rating = 0f,
            priceLevel = "$$",
            mapPinLabel = title.ifEmpty { location },
            voteCount = 0,
            imageUrl = imageUrl,
            link = link,
            type = type ?: ActivityType.EXPERIENCES
        )
        activityRepository.addActivity(tripId, activity)
        itineraryDirtyTrips += tripId

        // Notify listeners that activities changed for this trip so UI can refresh
        _events.emit(tripId)

        return activity
    }

    suspend fun updateActivity(activity: Activity, tripId: String) {
        activityRepository.updateActivity(activity)
        itineraryDirtyTrips += tripId
        _events.emit(tripId)
    }

    suspend fun deleteActivity(activityId: String, tripId: String) {
        activityRepository.deleteActivity(activityId)
        itineraryDirtyTrips += tripId
        _events.emit(tripId)
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
    ) {
        val vote = Vote(
            activityId = activityId,
            userId = userId,
            tripId = tripId,
            voteType = voteType
        )
        voteRepository.submitVote(vote)
        itineraryDirtyTrips += tripId

        // Notify listeners that votes changed for this trip so UI can refresh
        _events.emit(tripId)
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
        val votedIds = voteRepository.getVotedActivityIds(tripId, userId)
        val allActivities = activityRepository.getActivitiesForTrip(tripId)
        return allActivities.filter { it.id !in votedIds }
    }

    // ========================================
    // USER OPERATIONS
    // ========================================

    suspend fun getCurrentUser(): User? {
        return userRepository.getCurrentUser()
    }

    /** Sets the current user (e.g. after login). Mock uses this; real impl may validate token. */
    suspend fun setCurrentUser(userId: String) {
        userRepository.setCurrentUserId(userId)
    }

    suspend fun updateUserPreferences(
        userId: String,
        budget: BudgetLevel,
        vibe: TravelVibe,
        interests: Set<String>
    ) {
        userRepository.updateUserPreferences(userId, budget, vibe, interests)
    }

    // ITINERARY OPERATIONS
    // ========================================

    suspend fun getItinerary(tripId: String): Itinerary? {
        val lock = itineraryLockFor(tripId)
        return lock.withLock {
            if (tripId in itineraryDirtyTrips) {
                val regenerated = itineraryRepository.regenerateItinerary(tripId)
                if (regenerated != null) {
                    itineraryDirtyTrips -= tripId
                    return@withLock regenerated
                }
            }
            itineraryRepository.getItinerary(tripId)
        }
    }

    suspend fun exportItineraryToGoogleCalendar(tripId: String): Int {
        val providerToken = GoogleOAuthTokenStore.providerAccessToken
            ?: throw IllegalStateException(
                "Google Calendar permission is missing. Please log out and sign in with Google again."
            )

        return itineraryRepository.exportToGoogleCalendar(
            tripId = tripId,
            googleAccessToken = providerToken,
            timeZone = TimeZone.currentSystemDefault().id,
            calendarId = "primary"
        )
    }

    // ========================================
    // DASHBOARD - Combines multiple calls for UI
    // This is the main value of the Model layer
    // ========================================

    /**
     * Get all data needed for TripHomeScreen in one backend call.
     * Backend aggregates trip + activities + voting results + itinerary.
     */
    suspend fun getTripDashboard(tripId: String): TripDashboard {
        return tripRepository.getTripDashboard(tripId)
    }

}
