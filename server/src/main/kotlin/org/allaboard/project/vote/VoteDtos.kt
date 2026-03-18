package org.allaboard.project.vote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.allaboard.project.domain.VoteType

@Serializable
data class VotedActivityIdsResponse(
    val activityIds: List<String>
)

@Serializable
data class VoteInsert(
    @SerialName("activity_id") val activityId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("trip_id") val tripId: String,
    @SerialName("vote_type") val voteType: VoteType
)
