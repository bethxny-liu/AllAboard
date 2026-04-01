package org.allaboard.project

import org.allaboard.project.data.repository.DatabaseRepository
import org.allaboard.project.data.repository.mock.MockActivityRepository
import org.allaboard.project.data.repository.mock.MockItineraryRepository
import org.allaboard.project.data.repository.mock.MockTripRepository
import org.allaboard.project.data.repository.mock.MockUserRepository
import org.allaboard.project.data.repository.mock.MockVoteRepository
import org.allaboard.project.domain.AllAboardModel

/**
 * Same graph as [org.allaboard.project.domain.AllAboardModelTest] `createModel()`.
 * Lives in androidUnitTest with ViewModel tests (needs JVM Main dispatcher setup).
 */
internal fun allAboardModelFromMocks(): AllAboardModel {
    val tripRepo = MockTripRepository()
    val activityRepo = MockActivityRepository()
    val voteRepo = MockVoteRepository(activityRepo, tripRepo)
    val userRepo = MockUserRepository()
    val itineraryRepo = MockItineraryRepository()
    val databaseRepo = object : DatabaseRepository {
        override suspend fun signInWithGoogle(): Result<Unit> = Result.success(Unit)
        override suspend fun logout() {}
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
