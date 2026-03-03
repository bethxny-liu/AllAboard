package org.allaboard.project.data.repository

import org.allaboard.project.domain.ActivityVoteResult
import org.allaboard.project.domain.Vote

interface VoteRepository {
    suspend fun submitVote(vote: Vote)
    suspend fun getVotingResultsForTrip(tripId: String): List<ActivityVoteResult>
    suspend fun getVotingResultForActivity(tripId: String, activityId: String): ActivityVoteResult
    suspend fun getVotedActivityIds(tripId: String, userId: String): Set<String>
}
