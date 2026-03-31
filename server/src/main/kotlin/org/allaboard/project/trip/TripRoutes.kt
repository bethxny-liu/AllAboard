package org.allaboard.project.trip

import io.github.jan.supabase.postgrest.from
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.allaboard.project.SupabaseConfig
import org.allaboard.project.activitySuggestion.suggestActivities
import org.allaboard.project.auth.userId
import org.allaboard.project.domain.Trip
import org.allaboard.project.domain.User

/** Fetches a single trip by id with members, or null if not found. */
suspend fun fetchTripWithMembers(tripId: String): Trip? {
    val rows = SupabaseConfig.client.from("trips")
        .select { filter { eq("id", tripId) } }
        .decodeList<TripRow>()
    val row = rows.firstOrNull() ?: return null

    val memberIds = SupabaseConfig.client.from("trip_members")
        .select { filter { eq("trip_id", tripId) } }
        .decodeList<TripMemberRow>()
        .map { it.userId }

    val members = if (memberIds.isEmpty()) emptyList() else {
        memberIds.distinct().mapNotNull { uid ->
            SupabaseConfig.client.from("users")
                .select { filter { eq("id", uid) } }
                .decodeList<User>()
                .firstOrNull()
        }
    }

    return Trip(
        id = row.id,
        title = row.title,
        destination = row.destination,
        region = row.region ?: "",
        startDate = row.startDate,
        endDate = row.endDate,
        imageUrl = row.imageUrl,
        status = row.status,
        members = members
    )
}

/** Trip routes (JWT required; current user derived from token). */
fun Route.tripRoutes() {
    authenticate("supabase-jwt") {
        get("/trips") {
            val userId = call.userId
            val memberRows = SupabaseConfig.client.from("trip_members")
                .select { filter { eq("user_id", userId) } }
                .decodeList<TripMemberRow>()
            val tripIds = memberRows.map { it.tripId }.distinct()
            val trips = tripIds.mapNotNull { fetchTripWithMembers(it) }
            call.respond(trips)
        }

        get("/trips/{id}") {
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@get
            }
            val trip = fetchTripWithMembers(id)
            if (trip == null) {
                call.respond(HttpStatusCode.NotFound, "Trip not found")
                return@get
            }
            call.respond(trip)
        }

        post("/trips") {
            val userId = call.userId
            val trip = call.receive<Trip>()

            val insert = TripInsert(
                title = trip.title,
                destination = trip.destination,
                region = trip.region,
                startDate = trip.startDate,
                endDate = trip.endDate,
                imageUrl = trip.imageUrl,
                status = trip.status,
                createdBy = userId
            )

            val created = SupabaseConfig.client.from("trips").insert(insert) { select() }
                .decodeList<TripRow>()
                .firstOrNull()

            if (created == null) {
                call.respond(HttpStatusCode.InternalServerError, "Trip created but not found")
                return@post
            }

            SupabaseConfig.client.from("trip_members").insert(
                TripMemberRow(tripId = created.id, userId = userId, role = "OWNER")
            )

            val withMembers = fetchTripWithMembers(created.id)
            val ret = withMembers ?: Trip(
                id = created.id,
                title = created.title,
                destination = created.destination,
                region = created.region ?: "",
                startDate = created.startDate,
                endDate = created.endDate,
                imageUrl = created.imageUrl,
                status = created.status,
                members = emptyList())
            suggestActivities(ret)
            call.respond(ret)
        }

        patch("/trips/{id}") {
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@patch
            }

            val body = call.receive<Trip>()
            if (body.id != id) {
                call.respond(HttpStatusCode.BadRequest, "Trip id mismatch")
                return@patch
            }

            val updatedRow = SupabaseConfig.client.from("trips").update({
                set("title", body.title)
                set("destination", body.destination)
                set("region", body.region)
                set("start_date", body.startDate)
                set("end_date", body.endDate)
                set("image_url", body.imageUrl)
                set("status", body.status.name)
            }) {
                filter { eq("id", id) }
                select()
            }.decodeList<TripRow>().firstOrNull()

            val updated = if (updatedRow != null) fetchTripWithMembers(id) else null
            if (updated != null) call.respond(updated)
            else call.respond(HttpStatusCode.NotFound, "Trip not found")
        }

        delete("/trips/{id}") {
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@delete
            }

            val userId = call.userId
            val existing = SupabaseConfig.client.from("trips")
                .select { filter { eq("id", id) } }
                .decodeList<TripRow>()
                .firstOrNull()

            if (existing == null) {
                call.respond(HttpStatusCode.NotFound, "Trip not found")
                return@delete
            }

            if (existing.createdBy != userId) {
                call.respond(HttpStatusCode.Forbidden, "Only the trip owner can delete this trip")
                return@delete
            }

            SupabaseConfig.client.from("trips").delete { filter { eq("id", id) } }

            call.respond(HttpStatusCode.NoContent)
        }

        post("/trips/{id}/join") {
            val tripId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@post
            }

            val userId = call.userId
            val inserted = runCatching {
                SupabaseConfig.client.from("trip_members").insert(
                    TripMemberRow(tripId = tripId, userId = userId, role = "MEMBER")
                ) {
                    select(); single()
                }.decodeSingleOrNull<TripMemberRow>()
            }.getOrNull()

            if (inserted == null) {
                call.respond(HttpStatusCode.Conflict, "Already a member or trip not found")
                return@post
            }
            call.respond(HttpStatusCode.OK)
        }

        delete("/trips/{id}/members/{userId}") {
            val tripId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@delete
            }
            val userId = call.parameters["userId"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing user id")
                return@delete
            }

            val deleted = SupabaseConfig.client.from("trip_members").delete {
                filter { eq("trip_id", tripId); eq("user_id", userId) }
                select()
            }.decodeList<TripMemberRow>()

            if (deleted.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, "Membership not found")
                return@delete
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
