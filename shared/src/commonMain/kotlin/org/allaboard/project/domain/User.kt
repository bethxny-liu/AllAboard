package org.allaboard.project.domain

data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val budget: BudgetLevel = BudgetLevel.MEDIUM,
    val travelVibe: TravelVibe = TravelVibe.BALANCED,
    val interests: Set<String> = emptySet(),
    val imageUrl: String? = null
)

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
