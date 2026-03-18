package org.allaboard.project.vote

import io.github.jan.supabase.postgrest.from
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.allaboard.project.SupabaseConfig
import org.allaboard.project.auth.userId
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.domain.ActivityVoteResult
import org.allaboard.project.domain.User
import org.allaboard.project.domain.Vote
import org.allaboard.project.domain.VoteType

/**
 * Vote routes backed by the `votes` table.
 */
fun Route.voteRoutes() {
    authenticate("supabase-jwt") {
        post("/trips/{tripId}/activities/{activityId}/vote") {
            val tripId = call.parameters["tripId"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@post
            }
            val activityId = call.parameters["activityId"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing activity id")
                return@post
            }

            val body = call.receive<Vote>()

            if (body.tripId != tripId || body.activityId != activityId) {
                call.respond(HttpStatusCode.BadRequest, "Trip/activity mismatch")
                return@post
            }

            val userId = call.userId

            // Insert (or upsert) into votes table. Table has UNIQUE(activity_id, user_id).
            // We'll use upsert so user can change their vote.
            SupabaseConfig.client.from("votes").upsert(
                VoteInsert(
                    activityId = activityId,
                    userId = userId,
                    tripId = tripId,
                    voteType = body.voteType
                )
            ) {
                onConflict = "activity_id,user_id"
            }

            call.respond(HttpStatusCode.NoContent)
        }

        get("/trips/{tripId}/votes/results") {
            val tripId = call.parameters["tripId"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@get
            }

            // 1) Fetch activities for this trip
            val activities = SupabaseConfig.client.from("activities")
                .select { filter { eq("trip_id", tripId) } }
                .decodeList<ActivityRow>()

            if (activities.isEmpty()) {
                call.respond(emptyList<ActivityVoteResult>())
                return@get
            }

            // 2) Fetch votes for this trip
            val votes = SupabaseConfig.client.from("votes")
                .select { filter { eq("trip_id", tripId) } }
                .decodeList<Vote>()

            // 3) Fetch trip members for denominator + voter display names
            val tripMemberRows = SupabaseConfig.client.from("trip_members")
                .select { filter { eq("trip_id", tripId) } }
                .decodeList<TripMemberRow>()

            val memberIds = tripMemberRows.map { it.userId }.distinct()
            val totalMembers = memberIds.size

            val membersById: Map<String, User> = if (memberIds.isEmpty()) {
                emptyMap()
            } else {
                // N+1, but trip sizes are small; optimize later with an IN query if needed.
                memberIds.associateWith { uid ->
                    SupabaseConfig.client.from("users")
                        .select { filter { eq("id", uid) } }
                        .decodeList<User>()
                        .first()
                }
            }

            val votesByActivity = votes.groupBy { it.activityId }

            val results: List<ActivityVoteResult> = activities.map { actRow ->
                val activityVotes = votesByActivity[actRow.id].orEmpty()

                val yesVotes = activityVotes.count { it.voteType == VoteType.YES }
                val noVotes = activityVotes.count { it.voteType == VoteType.NO }
                val totalVotes = activityVotes.size

                val yesPercentage = if (totalMembers > 0) yesVotes.toFloat() / totalMembers else 0f
                val isComplete = totalVotes >= totalMembers
                val isConfirmed = isComplete && yesPercentage > 0.5f

                val voterNames = activityVotes
                    .filter { it.voteType == VoteType.YES }
                    .mapNotNull { membersById[it.userId]?.displayName }

                val activity = Activity(
                    id = actRow.id,
                    title = actRow.title,
                    location = actRow.location,
                    description = actRow.description,
                    rating = actRow.rating,
                    priceLevel = actRow.priceLevel,
                    mapPinLabel = actRow.mapPinLabel ?: actRow.title,
                    voteCount = totalVotes,
                    imageUrl = actRow.imageUrl,
                    link = actRow.link,
                    type = runCatching { ActivityType.valueOf(actRow.activityType) }
                        .getOrDefault(ActivityType.EXPERIENCES)
                )

                ActivityVoteResult(
                    activity = activity,
                    yesVotes = yesVotes,
                    noVotes = noVotes,
                    totalVotes = totalVotes,
                    yesPercentage = yesPercentage,
                    isComplete = isComplete,
                    isConfirmed = isConfirmed,
                    voterNames = voterNames
                )
            }.sortedByDescending { it.yesVotes }

            call.respond(results)
        }

        get("/trips/{tripId}/votes/mine") {
            val tripId = call.parameters["tripId"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@get
            }

            val userId = call.userId

            val rows = SupabaseConfig.client.from("votes")
                .select {
                    filter {
                        eq("trip_id", tripId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<Vote>()

            call.respond(VotedActivityIdsResponse(rows.map { it.activityId }.distinct()))
        }
    }
}
