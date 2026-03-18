package org.allaboard.project.trip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.allaboard.project.domain.TripStatus

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
