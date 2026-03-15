package org.allaboard.project.domain

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import org.allaboard.project.data.repository.ActivityRepository
import org.allaboard.project.data.repository.DatabaseRepository
import org.allaboard.project.data.repository.ItineraryRepository
import org.allaboard.project.data.repository.SupabaseClientProvider
import org.allaboard.project.data.repository.TripRepository
import org.allaboard.project.data.repository.UserRepository
import org.allaboard.project.data.repository.VoteRepository
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first

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
        val creator = userRepository.getCurrentUser()
        val trip = Trip(
            id = "",
            title = "All Aboard to $destination!",
            destination = destination,
            region = region,
            startDate = startDate,
            endDate = endDate,
            members = creator?.let { listOf(it) } ?: emptyList()
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
        endDate: String
    ): Trip? {
        val existingTrip = tripRepository.getTrip(tripId) ?: return null
        val updatedTrip = existingTrip.copy(
            title = "All Aboard to $destination!",
            destination = destination,
            region = region,
            startDate = startDate,
            endDate = endDate
        )
        val result = tripRepository.updateTrip(updatedTrip)

        // Notify listeners that trip was updated so UI can refresh
        _events.emit(tripId)

        return result
    }

    fun getTripInviteLink(tripId: String): String {
        return "AllAboard.ca/join/$tripId"
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
            id = generateId(),
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

        // Notify listeners that activities changed for this trip so UI can refresh
        _events.emit(tripId)

        return activity
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
            id = generateId(),
            activityId = activityId,
            userId = userId,
            tripId = tripId,
            voteType = voteType,
            timestamp = currentTimeMillis()
        )
        voteRepository.submitVote(vote)

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

        val votesByActivity = votingResults.associateBy { it.activity.id }
        val mergedActivities = activities.map { activity ->
            val vr = votesByActivity[activity.id]
            if (vr != null) activity.copy(voteCount = vr.totalVotes) else activity
        }

        return TripDashboard(
            trip = trip,
            activities = mergedActivities,
            votingResults = votingResults,
            itinerary = itinerary
        )
    }

    // ========================================
    // HELPERS
    // ========================================

    private fun generateId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..20).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    private fun currentTimeMillis(): Long {
        return kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    }
}
