package org.allaboard.project.ui.screens.activityDetails

/**
 * Domain model for a single activity/place details.
 * Used by the details screen and by any data source (stub, OpenTripMap API, etc.).
 */
data class ActivityDetails(
    val id: String,
    val title: String,
    val location: String,
    val description: String,
    val rating: Float = 0f,
    val priceLevel: String = "$$",
    val mapPinLabel: String,
    val imageUrl: String? = null
)
