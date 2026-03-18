package org.allaboard.project.activity

/**
 * Insert for `public.activities`. `created_at` / `updated_at` use DB defaults.
 */
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.allaboard.project.domain.ActivityType

@Serializable
data class ActivityInsert(
    @SerialName("trip_id") val tripId: String,
    val title: String,
    val location: String,
    val description: String = "",
    val rating: Float = 0f,
    @SerialName("price_level") val priceLevel: String = "$$",
    @SerialName("map_pin_label") val mapPinLabel: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val link: String? = null,
    @SerialName("activity_type") val type: ActivityType,
    @SerialName("added_by") val addedBy: String? = null
)
