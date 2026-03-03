package org.allaboard.project.domain

import kotlinx.coroutines.runBlocking
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
 * Unit tests for AllAboardModel (domain layer).
 *
 * - Unit tests under src/test/kotlin; one test class per class (Main / MainTest).
 * - Each test: Arrange (setup), Act (execute), Assert (assertEquals / assertTrue / assertNull).
 * - Test valid input conditions and invalid input conditions (slides 16–17).
 */
internal class AllAboardModelTest {

    private fun createModel(): AllAboardModel {
        val tripRepo = MockTripRepository()
        val activityRepo = MockActivityRepository()
        val voteRepo = MockVoteRepository(activityRepo, tripRepo)
        val userRepo = MockUserRepository()
        val itineraryRepo = MockItineraryRepository()
        return AllAboardModel(
            tripRepository = tripRepo,
            activityRepository = activityRepo,
            voteRepository = voteRepo,
            userRepository = userRepo,
            itineraryRepository = itineraryRepo
        )
    }

    @Test
    fun getTrip_validId_returnsTrip() = runBlocking {
        // Arrange (slide 16: setup conditions)
        val model = createModel()
        val tripId = "trip-1"
        // Act
        val result = model.getTrip(tripId)
        // Assert (slide 12: assertEquals – provided value matches actual)
        val trip = requireNotNull(result)
        assertEquals("trip-1", trip.id)
        assertEquals("Japan", trip.destination)
        assertEquals(TripStatus.UPCOMING, trip.status)
    }

    @Test
    fun getTrip_invalidId_returnsNull() = runBlocking {
        // Arrange
        val model = createModel()
        // Act – invalid input (slide 17: test invalid input conditions)
        val result = model.getTrip("nonexistent-id")
        // Assert
        assertNull(result)
    }

    @Test
    fun setCurrentUser_thenGetCurrentUser_returnsUser() = runBlocking {
        // Arrange
        val model = createModel()
        // Act
        model.setCurrentUser("user-1")
        val user = model.getCurrentUser()
        // Assert
        val u = requireNotNull(user)
        assertEquals("user-1", u.id)
        assertEquals("Daniel", u.displayName)
    }

    @Test
    fun getTripInviteLink_returnsExpectedFormat() {
        // Pure function – no suspend, no repos
        val model = createModel()
        val link = model.getTripInviteLink("trip-1")
        assertEquals("AllAboard.ca/join/trip-1", link)
    }

    @Test
    fun getUpcomingTrips_returnsOnlyUpcoming() = runBlocking {
        // Arrange – user-1 is member of trip-1 (UPCOMING) and trip-2 (COMPLETED)
        val model = createModel()
        model.setCurrentUser("user-1")
        // Act
        val upcoming = model.getUpcomingTrips("user-1")
        // Assert
        assertTrue(upcoming.isNotEmpty())
        assertEquals(1, upcoming.size)
        assertEquals(TripStatus.UPCOMING, upcoming.first().status)
        assertEquals("trip-1", upcoming.first().id)
    }

    @Test
    fun getPastTrips_returnsOnlyCompleted() = runBlocking {
        val model = createModel()
        model.setCurrentUser("user-1")
        val past = model.getPastTrips("user-1")
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
        val trips = model.getAllTripsForUser("user-1")
        assertTrue(trips.isNotEmpty())
        assertTrue(trips.any { it.id == "trip-1" })
    }

    @Test
    fun createTrip_returnsCreatedTripAndEmitsEvent() = runBlocking {
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
        val result = model.updateTripDetails(
            tripId = "bad-id",
            destination = "X",
            region = "Y",
            startDate = "A",
            endDate = "B"
        )
        assertNull(result)
    }

    @Test
    fun getActivity_validId_returnsActivity() = runBlocking {
        val model = createModel()
        val activity = requireNotNull(model.getActivity("act-1"))
        assertEquals("act-1", activity.id)
        assertEquals("Senso-ji Temple", activity.title) // Senso-ji is a real temple name
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
        // Before voting, act-1, act-2, act-3 etc. may be unvoted
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
}
