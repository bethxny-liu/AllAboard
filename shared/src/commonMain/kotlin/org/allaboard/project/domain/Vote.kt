package org.allaboard.project.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vote(
    val id: String? = null,
    @SerialName("activity_id")
    val activityId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("trip_id")
    val tripId: String,
    @SerialName("vote_type")
    val voteType: VoteType
)

@Serializable
enum class VoteType {
    YES,
    NO,
    SUPER
}

/**
 * Aggregated voting results for display.
 * This is computed by the backend and returned as a single object.
 */
@Serializable
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
