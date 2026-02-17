package org.allaboard.project.di

import org.allaboard.project.data.repository.ActivityRepository
import org.allaboard.project.data.repository.ItineraryRepository
import org.allaboard.project.data.repository.TripRepository
import org.allaboard.project.data.repository.UserRepository
import org.allaboard.project.data.repository.VoteRepository
import org.allaboard.project.data.repository.mock.MockActivityRepository
import org.allaboard.project.data.repository.mock.MockItineraryRepository
import org.allaboard.project.data.repository.mock.MockTripRepository
import org.allaboard.project.data.repository.mock.MockUserRepository
import org.allaboard.project.data.repository.mock.MockVoteRepository
import org.allaboard.project.domain.AllAboardModel

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
    val allAboardModel: AllAboardModel by lazy {
        AllAboardModel(
            tripRepository = tripRepository,
            activityRepository = activityRepository,
            voteRepository = voteRepository,
            userRepository = userRepository,
            itineraryRepository = itineraryRepository
        )
    }
}
