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
        assertTrue(result != null)
        assertEquals("trip-1", result?.id)
        assertEquals("Japan", result?.destination)
        assertEquals(TripStatus.UPCOMING, result?.status)
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
        assertTrue(user != null)
        assertEquals("user-1", user?.id)
        assertEquals("Daniel", user?.displayName)
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
        assertTrue(upcoming.size == 1)
        assertEquals(TripStatus.UPCOMING, upcoming.first().status)
        assertEquals("trip-1", upcoming.first().id)
    }

    @Test
    fun getPastTrips_returnsOnlyCompleted() = runBlocking {
        val model = createModel()
        model.setCurrentUser("user-1")
        val past = model.getPastTrips("user-1")
        assertTrue(past.size == 1)
        assertEquals(TripStatus.COMPLETED, past.first().status)
        assertEquals("trip-2", past.first().id)
    }

    @Test
    fun getTripDashboard_returnsMergedData() = runBlocking {
        val model = createModel()
        val dashboard = model.getTripDashboard("trip-1")
        assertTrue(dashboard.trip != null)
        assertEquals("trip-1", dashboard.trip?.id)
        assertTrue(dashboard.activities.isNotEmpty())
        assertTrue(dashboard.votingResults.isNotEmpty() || dashboard.activities.isNotEmpty())
    }
}
