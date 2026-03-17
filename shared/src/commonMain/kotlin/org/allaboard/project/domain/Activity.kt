package org.allaboard.project.domain

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
    val title: String,
    val location: String,
    val description: String,
    val rating: Float = 0f,
    val priceLevel: String = "$$",
    val mapPinLabel: String,
    val voteCount: Int,
    val imageUrl: String? = null,
    val link: String? = null,
    val type: ActivityType
)