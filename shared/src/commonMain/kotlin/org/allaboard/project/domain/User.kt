package org.allaboard.project.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,

    @SerialName("display_name")
    val displayName: String,

    val email: String,

    @SerialName("budget_level")
    val budget: BudgetLevel = BudgetLevel.MEDIUM,

    @SerialName("travel_vibe")
    val travelVibe: TravelVibe = TravelVibe.BALANCED,

    val interests: Set<String> = emptySet(),

    @SerialName("image_url")
    val imageUrl: String? = null
)
@Serializable
enum class BudgetLevel {
    LOW,
    MEDIUM,
    HIGH;

    val symbol: String
        get() = when (this) {
            LOW -> "$"
            MEDIUM -> "$$"
            HIGH -> "$$$"
        }
}

@Serializable
enum class TravelVibe {
    RELAXED,
    ADVENTUROUS,
    BALANCED;

    companion object {
        fun fromString(value: String?): TravelVibe? = when (value?.lowercase()) {
            "relaxed" -> RELAXED
            "adventurous" -> ADVENTUROUS
            "balanced" -> BALANCED
            else -> null
        }
    }
}
