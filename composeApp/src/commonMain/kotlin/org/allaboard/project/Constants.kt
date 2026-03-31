package org.allaboard.project

import org.allaboard.project.domain.ActivityType

/**
 * Standardized category enum for activities across the app
 */
enum class Category(val displayName: String, val type: ActivityType?) {
    ALL("All", null),
    RESTAURANTS("Restaurants", ActivityType.RESTAURANT),
    LANDMARKS("Landmarks", ActivityType.LANDMARK),
    EXPERIENCES("Experiences", ActivityType.EXPERIENCES);

    companion object {
        fun fromDisplayName(name: String): Category {
            return entries.find { it.displayName == name } ?: ALL
        }
        fun fromType(type: ActivityType): Category {
            return entries.find { it.type == type } ?: ALL
        }

        val allCategories: List<Category> = entries
    }
}
