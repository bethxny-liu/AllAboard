package org.allaboard.project

/**
 * Activity routes and DB types. Matches schema:
 * activities(id, trip_id, title, location, description, rating, price_level, activity_type, added_by).
 * Domain Activity adds mapPinLabel, voteCount, imageUrl, link (app-only / defaults when reading from DB).
 */
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType

/** DB row shape for activities table (snake_case from Supabase). */
@Serializable
data class ActivityRow(
    val id: String,
    @SerialName("trip_id") val tripId: String,
    val title: String,
    val location: String,
    val description: String,
    val rating: Float = 0f,
    @SerialName("price_level") val priceLevel: String = "$$",
    @SerialName("activity_type") val type: ActivityType,
    @SerialName("added_by") val addedBy: String? = null
)

/** Payload for inserting a new activity. */
@Serializable
data class ActivityInsert(
    val id: String,
    @SerialName("trip_id") val tripId: String,
    val title: String,
    val location: String,
    val description: String,
    val rating: Float = 0f,
    @SerialName("price_level") val priceLevel: String = "$$",
    @SerialName("activity_type") val type: ActivityType,
    @SerialName("added_by") val addedBy: String? = null
)

/** Maps DB row to domain Activity; app-only fields use defaults. */
fun ActivityRow.toActivity(): Activity = Activity(
    id = id,
    title = title,
    location = location,
    description = description,
    rating = rating,
    priceLevel = priceLevel,
    mapPinLabel = title,
    voteCount = 0,
    imageUrl = null,
    link = null,
    type = type
)
