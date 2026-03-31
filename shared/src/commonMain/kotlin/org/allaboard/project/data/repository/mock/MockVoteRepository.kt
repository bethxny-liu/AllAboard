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


    override suspend fun getVotedActivityIds(tripId: String, userId: String): Set<String> {
        delay(50)
        val userVotedActivityIds = votes
            .filter { it.userId == userId && it.tripId == tripId }
            .map { it.activityId }
            .toSet()

        return userVotedActivityIds
    }

    /**
     * Simulates backend vote computation logic.
     * This logic will live in the backend in Sprint 3.
     */
    private fun computeVotingResult(trip: Trip, activity: Activity): ActivityVoteResult {
        val activeMemberIds = trip.members.map { it.id }.toSet()
        val activityVotes = votes.filter {
            it.tripId == trip.id &&
                it.activityId == activity.id &&
                it.userId in activeMemberIds
        }

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
                trip.members.find { it.id == vote.userId }?.displayName
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
            rating = 0f,
            priceLevel = "$$",
            mapPinLabel = "",
            voteCount = 0,
            imageUrl = null,
            link = null,
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
