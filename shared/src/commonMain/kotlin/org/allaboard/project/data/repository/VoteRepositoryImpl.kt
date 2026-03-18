package org.allaboard.project.data.repository

import org.allaboard.project.data.network.ApiClient
import org.allaboard.project.domain.ActivityVoteResult
import org.allaboard.project.domain.Vote

/**
 * Real VoteRepository implementation backed by your backend server.
 */
class VoteRepositoryImpl : VoteRepository {

    @kotlinx.serialization.Serializable
    private data class VotedActivityIdsResponse(val activityIds: List<String>)

    override suspend fun submitVote(vote: Vote) {
        // Send vote as JSON body. id is expected to be null.
        ApiClient.postNoResponse(
            "/trips/${vote.tripId}/activities/${vote.activityId}/vote",
            vote
        )
    }

    override suspend fun getVotingResultsForTrip(tripId: String): List<ActivityVoteResult> {
        return ApiClient.get("/trips/$tripId/votes/results")
    }

    override suspend fun getVotedActivityIds(tripId: String, userId: String): Set<String> {
        val res: VotedActivityIdsResponse = ApiClient.get("/trips/$tripId/votes/mine")
        return res.activityIds.toSet()
    }
}
