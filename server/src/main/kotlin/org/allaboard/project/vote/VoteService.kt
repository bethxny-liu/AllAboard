package org.allaboard.project.vote

import io.github.jan.supabase.postgrest.from
import org.allaboard.project.SupabaseConfig
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityVoteResult
import org.allaboard.project.domain.User
import org.allaboard.project.domain.Vote
import org.allaboard.project.domain.VoteType
import org.allaboard.project.trip.TripMemberRow

/**
 * Shared backend logic for computing voting results.
 *
 * Keep this in one place so `/trips/{tripId}/votes/results` and any other endpoint
 * (e.g., the trip dashboard) always return identical computed results.
 */
suspend fun computeVotingResultsForTrip(tripId: String, activities: List<Activity>): List<ActivityVoteResult> {
    if (activities.isEmpty()) return emptyList()

    // 1) Fetch votes for this trip
    val votes = SupabaseConfig.client.from("votes")
        .select { filter { eq("trip_id", tripId) } }
        .decodeList<Vote>()

    // 2) Fetch trip members for denominator + voter names
    val tripMemberRows = SupabaseConfig.client.from("trip_members")
        .select { filter { eq("trip_id", tripId) } }
        .decodeList<TripMemberRow>()

    val memberIds = tripMemberRows.map { it.userId }.distinct()
    val totalMembers = memberIds.size

    val membersById: Map<String, User> = if (memberIds.isEmpty()) {
        emptyMap()
    } else {
        // N+1 is fine for small trips; optimize later with an IN query.
        memberIds.associateWith { uid ->
            SupabaseConfig.client.from("users")
                .select { filter { eq("id", uid) } }
                .decodeList<User>()
                .first()
        }
    }

    val votesByActivity = votes.groupBy { it.activityId }

    return activities.map { activityRow ->
        val activityVotes = votesByActivity[activityRow.id].orEmpty()

        val yesVotes = activityVotes.count { it.voteType == VoteType.YES }
        val noVotes = activityVotes.count { it.voteType == VoteType.NO }
        val totalVotes = activityVotes.size

        val yesPercentage = if (totalMembers > 0) yesVotes.toFloat() / totalMembers else 0f
        val isComplete = totalVotes >= totalMembers
        val isConfirmed = isComplete && yesPercentage > 0.5f

        val voterNames = activityVotes
            .filter { it.voteType == VoteType.YES }
            .mapNotNull { membersById[it.userId]?.displayName }

        ActivityVoteResult(
            activity = activityRow.copy(voteCount = totalVotes),
            yesVotes = yesVotes,
            noVotes = noVotes,
            totalVotes = totalVotes,
            yesPercentage = yesPercentage,
            isComplete = isComplete,
            isConfirmed = isConfirmed,
            voterNames = voterNames
        )
    }.sortedByDescending { it.yesVotes }
}
