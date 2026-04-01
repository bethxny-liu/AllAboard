package org.allaboard.project.activity

import io.github.jan.supabase.postgrest.from
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.allaboard.project.SupabaseConfig
import org.allaboard.project.auth.userId
import org.allaboard.project.domain.Activity

/** Activity routes (JWT required; current user derived from token). */
fun Route.activityRoutes() {
    authenticate("supabase-jwt") {
        get("/activities/{id}") {
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing activity id")
                return@get
            }

            val rows = SupabaseConfig.client.from("activities")
                .select { filter { eq("id", id) } }
                .decodeList<Activity>()

            val activity = rows.firstOrNull()
            if (activity == null) {
                call.respond(HttpStatusCode.NotFound, "Activity not found")
                return@get
            }
            call.respond(activity)
        }

        get("/trips/{id}/activities") {
            val tripId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@get
            }

            val rows = SupabaseConfig.client.from("activities")
                .select { filter { eq("trip_id", tripId) } }
                .decodeList<Activity>()

            call.respond(rows)
        }

        post("/trips/{id}/activities") {
            val tripId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@post
            }

            val userId = call.userId
            val body = call.receive<Activity>()

            val coords = if (body.latitude == null || body.longitude == null) {
                geocodeLocation(
                    address = body.location,
                    city = null,
                    country = null
                )
            } else {
                null
            }

            val insert = ActivityInsert(
                tripId = tripId,
                title = body.title,
                location = body.location,
                description = body.description ?: "",
                rating = body.rating,
                priceLevel = body.priceLevel,
                mapPinLabel = body.mapPinLabel?.takeIf { it.isNotBlank() },
                imageUrl = body.link,
                link = body.link,
                type = body.type,
                preference = body.preference,
                latitude = body.latitude ?: coords?.first,
                longitude = body.longitude ?: coords?.second,
                addedBy = userId
            )

            val created = SupabaseConfig.client.from("activities").insert(insert) { select() }
                .decodeList<Activity>()
                .firstOrNull()

            if (created == null) {
                call.respond(HttpStatusCode.InternalServerError, "Activity created but not found")
                return@post
            }

            // Preserve UI-provided voteCount (server doesn't store it in activities).
            call.respond(created.copy(voteCount = body.voteCount))
        }

        patch("/activities/{id}") {
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing activity id")
                return@patch
            }

            val body = call.receive<Activity>()
            if (body.id != id) {
                call.respond(HttpStatusCode.BadRequest, "Activity id mismatch")
                return@patch
            }

            val coords = if (body.latitude == null || body.longitude == null) {
                geocodeLocation(
                    address = body.location,
                    city = null,
                    country = null
                )
            } else {
                null
            }

            val updated = SupabaseConfig.client.from("activities").update({
                set("title", body.title)
                set("location", body.location)
                set("description", body.description)
                set("rating", body.rating)
                set("price_level", body.priceLevel)
                set("map_pin_label", body.mapPinLabel?.takeIf { it.isNotBlank() })
                set("image_url", body.link)
                set("link", body.link)
                set("activity_type", body.type.name)
                set("activity_preference", body.preference)
                set("latitude", body.latitude ?: coords?.first)
                set("longitude", body.longitude ?: coords?.second)
            }) {
                filter { eq("id", id) }
                select()
            }.decodeList<Activity>().firstOrNull()

            if (updated != null) {
                call.respond(updated.copy(voteCount = body.voteCount))
            } else {
                call.respond(HttpStatusCode.NotFound, "Activity not found")
            }
        }

        delete("/activities/{id}") {
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing activity id")
                return@delete
            }

            val deleted = SupabaseConfig.client.from("activities").delete {
                filter { eq("id", id) }
                select()
            }.decodeList<Activity>()

            if (deleted.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, "Activity not found")
                return@delete
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
