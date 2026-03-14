package org.allaboard.project.domain

data class Vote(
    val id: String,
    val activityId: String,
    val userId: String,
    val tripId: String,
    val voteType: VoteType,
    val timestamp: Long
)

enum class VoteType {
    YES,
    NO,
    SUPER
}

/**
 * Aggregated voting results for display.
 * This is computed by the backend and returned as a single object.
 */
data class ActivityVoteResult(
    val activity: Activity,
    val yesVotes: Int,
    val noVotes: Int,
    val totalVotes: Int,
    val yesPercentage: Float,
    val isComplete: Boolean,
    val isConfirmed: Boolean,
    val voterNames: List<String>
)
