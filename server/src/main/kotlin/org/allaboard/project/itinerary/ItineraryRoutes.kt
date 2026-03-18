package org.allaboard.project.itinerary

import io.github.jan.supabase.postgrest.from
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import org.allaboard.project.SupabaseConfig
import org.allaboard.project.auth.userId
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.Itinerary
import org.allaboard.project.domain.ItineraryDay
import org.allaboard.project.domain.ScheduledActivity

private suspend fun fetchItinerary(tripId: String): Itinerary? {
    val dayRows = SupabaseConfig.client.from("itinerary_days")
        .select { filter { eq("trip_id", tripId) } }
        .decodeList<ItineraryDayRow>()
        .sortedBy { it.dayNumber }

    if (dayRows.isEmpty()) return null

    val activities = SupabaseConfig.client.from("activities")
        .select { filter { eq("trip_id", tripId) } }
        .decodeList<Activity>()
        .associateBy { it.id }

    val dayIds = dayRows.map { it.id }
    val scheduledRows = if (dayIds.isEmpty()) emptyList() else {
        SupabaseConfig.client.from("scheduled_activities")
            .select { filter { isIn("itinerary_day_id", dayIds) } }
            .decodeList<ScheduledActivityRow>()
    }

    val scheduledByDay = scheduledRows.groupBy { it.itineraryDayId }

    val days = dayRows.map { dayRow ->
        val scheduledForDay = (scheduledByDay[dayRow.id] ?: emptyList())
            .sortedWith(compareBy<ScheduledActivityRow> { it.sortOrder }.thenBy { it.startTime })
            .mapNotNull { sa ->
                val act = activities[sa.activityId] ?: return@mapNotNull null
                ScheduledActivity(
                    activity = act,
                    startTime = sa.startTime,
                    endTime = sa.endTime,
                    notes = sa.notes
                )
            }

        ItineraryDay(
            date = dayRow.dayDate,
            dayNumber = dayRow.dayNumber,
            activities = scheduledForDay
        )
    }

    return Itinerary(tripId = tripId, days = days)
}

fun Route.itineraryRoutes() {
    authenticate("supabase-jwt") {
        get("/trips/{id}/itinerary") {
            val tripId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@get
            }

            val userId = call.userId

            val itinerary = fetchItinerary(tripId)
            if (itinerary == null) {
//                call.respond(HttpStatusCode.NotFound, "Itinerary not found")
//                return@get
                call.respond(Itinerary(tripId = tripId, emptyList()))
                return@get
            }
            call.respond(itinerary)
        }

        patch("/trips/{id}/itinerary/days/{date}/activities") {
            val tripId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@patch
            }
            val date = call.parameters["date"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing date")
                return@patch
            }

            val userId = call.userId

            val body = call.receive<UpdateScheduledActivityRequest>()

            // Find itinerary day (must exist)
            val day = SupabaseConfig.client.from("itinerary_days")
                .select {
                    filter {
                        eq("trip_id", tripId)
                        eq("day_date", date)
                    }
                    limit(1)
                }
                .decodeList<ItineraryDayRow>()
                .firstOrNull()

            if (day == null) {
                call.respond(HttpStatusCode.NotFound, "Itinerary day not found")
                return@patch
            }

            // Update the scheduled activity if present; otherwise insert it.
            val updated = SupabaseConfig.client.from("scheduled_activities").update({
                set("start_time", body.startTime)
                set("end_time", body.endTime)
                set("notes", body.notes)
            }) {
                filter {
                    eq("itinerary_day_id", day.id)
                    eq("activity_id", body.activityId)
                }
                select()
            }.decodeList<ScheduledActivityRow>()

            if (updated.isEmpty()) {
                // Insert new row; sort order defaults to 0.
                SupabaseConfig.client.from("scheduled_activities").insert(
                    ScheduledActivityInsert(
                        itineraryDayId = day.id,
                        activityId = body.activityId,
                        startTime = body.startTime,
                        endTime = body.endTime,
                        notes = body.notes
                    )
                )
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
