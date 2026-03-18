package org.allaboard.project.trip

import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.allaboard.project.SupabaseConfig
import org.allaboard.project.domain.Trip
import org.allaboard.project.domain.TripStatus
import org.allaboard.project.domain.User

/** DB row shape for trips table (snake_case from Supabase). */
@Serializable
data class TripRow(
    val id: String,
    val title: String,
    val destination: String,
    val region: String? = null,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val status: TripStatus = TripStatus.UPCOMING,
    @SerialName("created_by") val createdBy: String? = null
)

/** Payload for inserting a new trip (no id — DB generates it). */
@Serializable
data class TripInsert(
    val title: String,
    val destination: String,
    val region: String = "",
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val status: TripStatus = TripStatus.UPCOMING,
    @SerialName("created_by") val createdBy: String? = null
)

/** DB row shape for `public.trip_members` (`id` / `joined_at` from DB defaults on insert). */
@Serializable
data class TripMemberRow(
    val id: String? = null,
    @SerialName("trip_id") val tripId: String,
    @SerialName("user_id") val userId: String,
    val role: String = "MEMBER"
)

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
