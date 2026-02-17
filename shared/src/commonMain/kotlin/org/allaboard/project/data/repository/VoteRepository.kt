package org.allaboard.project.data.repository

import org.allaboard.project.domain.ActivityVoteResult
import org.allaboard.project.domain.Vote

interface VoteRepository {
    suspend fun submitVote(vote: Vote)
    suspend fun getVotingResultsForTrip(tripId: String): List<ActivityVoteResult>
    suspend fun getVotingResultForActivity(tripId: String, activityId: String): ActivityVoteResult
    suspend fun getUnvotedActivityIds(tripId: String, userId: String): List<String>
}
