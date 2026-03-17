package org.allaboard.project.domain

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.allaboard.project.data.repository.DatabaseRepository
import org.allaboard.project.data.repository.mock.MockActivityRepository
import org.allaboard.project.data.repository.mock.MockItineraryRepository
import org.allaboard.project.data.repository.mock.MockTripRepository
import org.allaboard.project.data.repository.mock.MockUserRepository
import org.allaboard.project.data.repository.mock.MockVoteRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for AllAboardModel (domain coordinator). Verifies: get/create/update trip and invite link;
 * get/create activity; vote and voting results and unvoted activities; current user and preferences;
 * itinerary and trip dashboard; and that createTrip/events flow emits. Includes valid and invalid IDs.
 */
internal class AllAboardModelTest {

    private fun createModel(): AllAboardModel {
        val tripRepo = MockTripRepository()
        val activityRepo = MockActivityRepository()
        val voteRepo = MockVoteRepository(activityRepo, tripRepo)
        val userRepo = MockUserRepository()
        val itineraryRepo = MockItineraryRepository()
        val databaseRepo = object : DatabaseRepository {
            override suspend fun signInWithGoogle(): Result<Unit> = Result.success(Unit)
        }
        return AllAboardModel(
            tripRepository = tripRepo,
            activityRepository = activityRepo,
            voteRepository = voteRepo,
            userRepository = userRepo,
            itineraryRepository = itineraryRepo,
            databaseRepository = databaseRepo
        )
    }

    @Test
    fun getTrip_validId_returnsTrip() = runBlocking {
        val model = createModel()
        val result = model.getTrip("trip-1")
        val trip = requireNotNull(result)
        assertEquals("trip-1", trip.id)
        assertEquals("Japan", trip.destination)
        assertEquals(TripStatus.UPCOMING, trip.status)
    }

    @Test
    fun getTrip_invalidId_returnsNull() = runBlocking {
        val model = createModel()
        assertNull(model.getTrip("nonexistent-id"))
    }

    @Test
    fun setCurrentUser_thenGetCurrentUser_returnsUser() = runBlocking {
        val model = createModel()
        model.setCurrentUser("user-1")
        val user = requireNotNull(model.getCurrentUser())
        assertEquals("user-1", user.id)
        assertEquals("Daniel", user.displayName)
    }

    @Test
    fun getTripInviteLink_returnsExpectedFormat() {
        val model = createModel()
        assertEquals("AllAboard.ca/join/trip-1", model.getTripInviteLink("trip-1"))
    }

    @Test
    fun getUpcomingTrips_returnsOnlyUpcoming() = runBlocking {
        val model = createModel()
        val upcoming = model.getUpcomingTrips()
        assertTrue(upcoming.isNotEmpty())
        assertEquals(1, upcoming.size)
        assertEquals(TripStatus.UPCOMING, upcoming.first().status)
        assertEquals("trip-1", upcoming.first().id)
    }

    @Test
    fun getPastTrips_returnsOnlyCompleted() = runBlocking {
        val model = createModel()
        val past = model.getPastTrips()
        assertTrue(past.isNotEmpty())
        assertEquals(1, past.size)
        assertEquals(TripStatus.COMPLETED, past.first().status)
        assertEquals("trip-2", past.first().id)
    }

    @Test
    fun getTripDashboard_returnsMergedData() = runBlocking {
        val model = createModel()
        val dashboard = model.getTripDashboard("trip-1")
        val dashTrip = requireNotNull(dashboard.trip)
        assertEquals("trip-1", dashTrip.id)
        assertTrue(dashboard.activities.isNotEmpty())
        assertTrue(dashboard.votingResults.isNotEmpty() || dashboard.activities.isNotEmpty())
    }

    @Test
    fun getAllTripsForUser_returnsTripsForUser() = runBlocking {
        val model = createModel()
        val trips = model.getAllTripsForUser()
        assertTrue(trips.isNotEmpty())
        assertTrue(trips.any { it.id == "trip-1" })
    }

    @Test
    fun createTrip_returnsCreatedTripWithCorrectFields() = runBlocking {
        val model = createModel()
        val trip = model.createTrip(
            destination = "Italy",
            region = "Rome",
            startDate = "Jun 1",
            endDate = "Jun 10",
            creatorId = "user-1"
        )
        assertEquals(true, trip.id.isNotEmpty())
        assertEquals("Italy", trip.destination)
        assertEquals("Rome", trip.region)
        assertEquals("All Aboard to Italy!", trip.title)
        assertEquals(true, trip.members.isNotEmpty())
    }

    @Test
    fun updateTripDetails_validId_returnsUpdatedTrip() = runBlocking {
        val model = createModel()
        val result = model.updateTripDetails(
            tripId = "trip-1",
            destination = "Osaka",
            region = "Kansai",
            startDate = "Jan 1",
            endDate = "Jan 10"
        )
        val updated = requireNotNull(result)
        assertEquals("Osaka", updated.destination)
        assertEquals("Kansai", updated.region)
    }

    @Test
    fun updateTripDetails_invalidId_returnsNull() = runBlocking {
        val model = createModel()
        assertNull(
            model.updateTripDetails(
                tripId = "bad-id",
                destination = "X",
                region = "Y",
                startDate = "A",
                endDate = "B"
            )
        )
    }

    @Test
    fun getActivity_validId_returnsActivity() = runBlocking {
        val model = createModel()
        val activity = requireNotNull(model.getActivity("act-1"))
        assertEquals("act-1", activity.id)
        assertEquals("Senso-ji Temple", activity.title)
    }

    @Test
    fun getActivity_invalidId_returnsNull() = runBlocking {
        val model = createModel()
        assertNull(model.getActivity("nonexistent"))
    }

    @Test
    fun createActivityForTrip_addsActivityAndReturnsIt() = runBlocking {
        val model = createModel()
        val activity = model.createActivityForTrip(
            tripId = "trip-1",
            title = "Test Activity",
            location = "Test City",
            description = "Test desc",
            type = ActivityType.LANDMARK
        )
        assertEquals(true, activity.id.isNotEmpty())
        assertEquals("Test Activity", activity.title)
        assertEquals("Test City", activity.location)
        assertEquals(ActivityType.LANDMARK, activity.type)
    }

    @Test
    fun voteOnActivity_submitsVote() = runBlocking {
        val model = createModel()
        model.voteOnActivity("trip-1", "act-1", "user-1", VoteType.YES)
        val results = model.getVotingResults("trip-1")
        assertEquals(true, results.any { it.activity.id == "act-1" })
    }

    @Test
    fun getVotingResults_returnsResultsForTrip() = runBlocking {
        val model = createModel()
        val results = model.getVotingResults("trip-1")
        assertTrue(results.all { it.activity.id.isNotEmpty() })
    }

    @Test
    fun getUnvotedActivities_returnsActivitiesUserHasNotVotedOn() = runBlocking {
        val model = createModel()
        val unvoted = model.getUnvotedActivities("trip-1", "user-1")
        assertEquals(true, unvoted.all { it.id.isNotEmpty() })
    }

    @Test
    fun updateUserPreferences_updatesUser() = runBlocking {
        val model = createModel()
        model.setCurrentUser("user-1")
        model.updateUserPreferences("user-1", BudgetLevel.HIGH, TravelVibe.ADVENTUROUS, setOf("Hiking"))
        val user = requireNotNull(model.getCurrentUser())
        assertEquals(BudgetLevel.HIGH, user.budget)
        assertEquals(TravelVibe.ADVENTUROUS, user.travelVibe)
        assertEquals(true, user.interests.contains("Hiking"))
    }

    @Test
    fun getItinerary_validTripId_returnsItinerary() = runBlocking {
        val model = createModel()
        val itinerary = requireNotNull(model.getItinerary("trip-1"))
        assertEquals("trip-1", itinerary.tripId)
        assertEquals(true, itinerary.days.isNotEmpty())
    }

    @Test
    fun createTrip_emitsEventOnEventsFlow() = runBlocking {
        val model = createModel()
        var emitted: String? = null
        val job = launch { emitted = model.events.first() }
        val trip = model.createTrip(
            destination = "Paris",
            region = "Île-de-France",
            startDate = "2025-07-01",
            endDate = "2025-07-10",
            creatorId = "user-1"
        )
        job.join()
        assertEquals(trip.id, emitted)
    }

    @Test
    fun createActivityForTrip_withOptionalParams_usesDefaults() = runBlocking {
        val model = createModel()
        val activity = model.createActivityForTrip(
            tripId = "trip-1",
            title = "Custom",
            location = "City",
            description = "Desc",
            type = null,
            imageUrl = null,
            link = null
        )
        assertEquals(ActivityType.EXPERIENCES, activity.type)
        assertEquals("$$", activity.priceLevel)
        assertEquals(0f, activity.rating)
        assertEquals(0, activity.voteCount)
        assertEquals("Custom", activity.mapPinLabel)
    }
}
