package org.allaboard.project.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ActivityType {
    LANDMARK,
    RESTAURANT,
    EXPERIENCES
}

@Serializable
data class Activity(
    val id: String,
    @SerialName("trip_id") val tripId: String? = null,
    val title: String,
    val location: String,
    /** Null when Postgrest returns null (same pattern as [User.imageUrl], [Trip.imageUrl]). */
    val description: String? = null,
    val rating: Float = 0f,
    @SerialName("price_level") val priceLevel: String = "$$",
    @SerialName("map_pin_label") val mapPinLabel: String? = null,
    /** Not a DB column — server fills from request/mocks; omitted in Postgrest → decodes as 0. */
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("image_url") val imageUrl: String? = null,
    val link: String? = null,
    @SerialName("activity_type") val type: ActivityType,
    @SerialName("added_by") val addedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    val mapPinDisplay: String get() = mapPinLabel?.takeIf { it.isNotBlank() } ?: title
}
