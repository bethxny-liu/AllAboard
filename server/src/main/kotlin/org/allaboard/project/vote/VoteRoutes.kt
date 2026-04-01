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
import org.allaboard.project.domain.ActivityVoteResult
import org.allaboard.project.domain.Vote

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

            val activities = SupabaseConfig.client.from("activities")
                .select { filter { eq("trip_id", tripId) } }
                .decodeList<Activity>()

            val results: List<ActivityVoteResult> = computeVotingResultsForTrip(tripId, activities)
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
