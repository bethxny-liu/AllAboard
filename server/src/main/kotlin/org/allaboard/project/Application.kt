package org.allaboard.project

import io.github.jan.supabase.postgrest.from
import io.ktor.http.*
import io.ktor.server.request.receive
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.allaboard.project.auth.configureAuth
import org.allaboard.project.auth.userId
import org.allaboard.project.domain.Trip
import org.allaboard.project.domain.User
import org.allaboard.project.fetchTripWithMembers

fun main() {
    // Eagerly initialise the Supabase client so .env errors surface immediately
    println("[server] Supabase URL = ${SupabaseConfig.supabaseUrl}")

    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            }
        )
    }

    // Log every request + response body
    install(RequestLoggingMiddleware)

    // JWT authentication — verifies Supabase JWTs with HMAC256
    configureAuth()

    routing {
        // ── Public routes (no auth required) ────────────────────────────
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        // ── Protected routes (JWT required) ─────────────────────────────
        authenticate("supabase-jwt") {
            get("/user/me") {
                val userId = call.userId

                val rows = SupabaseConfig.client.from("users")
                    .select { filter { eq("id", userId) } }
                    .decodeList<User>()

                val user = rows.firstOrNull()
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@get
                }
                call.respond(user)
            }
            patch("/user/me/preferences") {
                val userId = call.userId
                val body = call.receive<User>()
                SupabaseConfig.client.from("users").update({
                    set("budget_level", body.budget.name)
                    set("travel_vibe", body.travelVibe.name)
                    set("interests", body.interests.toList())
                }) {
                    filter { eq("id", userId) }
                }
                val rows = SupabaseConfig.client.from("users")
                    .select { filter { eq("id", userId) } }
                    .decodeList<User>()
                val updatedUser = rows.firstOrNull()
                if (updatedUser != null) {
                    call.respond(updatedUser)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "User not found after update")
                }
            }

            // ── Trip routes (JWT required; current user from token) ─────────
            get("/trips") {
                val userId = call.userId
                val memberRows = SupabaseConfig.client.from("trip_members")
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<org.allaboard.project.TripMemberRow>()
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
                val insert = org.allaboard.project.TripInsert(
                    title = trip.title,
                    destination = trip.destination,
                    region = trip.region,
                    startDate = trip.startDate,
                    endDate = trip.endDate,
                    imageUrl = trip.imageUrl,
                    status = trip.status,
                    createdBy = userId
                )
                SupabaseConfig.client.from("trips").insert(insert)
                val created = SupabaseConfig.client.from("trips")
                    .select { filter { eq("created_by", userId); eq("title", trip.title); eq("start_date", trip.startDate); eq("end_date", trip.endDate) } }
                    .decodeList<org.allaboard.project.TripRow>()
                    .firstOrNull()
                if (created == null) {
                    call.respond(HttpStatusCode.InternalServerError, "Trip created but not found")
                    return@post
                }
                SupabaseConfig.client.from("trip_members").insert(
                    org.allaboard.project.TripMemberInsert(tripId = created.id, userId = userId, role = "OWNER")
                )
                val withMembers = fetchTripWithMembers(created.id)
                call.respond(withMembers ?: Trip(
                    id = created.id,
                    title = created.title,
                    destination = created.destination,
                    region = created.region ?: "",
                    startDate = created.startDate,
                    endDate = created.endDate,
                    imageUrl = created.imageUrl,
                    status = created.status,
                    members = emptyList()
                ))
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
                SupabaseConfig.client.from("trips").update({
                    set("title", body.title)
                    set("destination", body.destination)
                    set("region", body.region)
                    set("start_date", body.startDate)
                    set("end_date", body.endDate)
                    set("image_url", body.imageUrl)
                    set("status", body.status.name)
                }) {
                    filter { eq("id", id) }
                }
                val updated = fetchTripWithMembers(id)
                if (updated != null) call.respond(updated)
                else call.respond(HttpStatusCode.NotFound, "Trip not found")
            }
            delete("/trips/{id}") {
                val id = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                    return@delete
                }
                SupabaseConfig.client.from("trips").delete {
                    filter { eq("id", id) }
                }
                call.respond(HttpStatusCode.NoContent)
            }
            post("/trips/{id}/join") {
                val tripId = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                    return@post
                }
                val userId = call.userId
                SupabaseConfig.client.from("trip_members").insert(
                    org.allaboard.project.TripMemberInsert(tripId = tripId, userId = userId, role = "MEMBER")
                )
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
                SupabaseConfig.client.from("trip_members").delete {
                    filter { eq("trip_id", tripId); eq("user_id", userId) }
                }
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
