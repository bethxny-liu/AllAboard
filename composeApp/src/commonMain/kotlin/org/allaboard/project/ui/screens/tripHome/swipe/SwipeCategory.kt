package org.allaboard.project.ui.screens.tripHome.swipe

import org.allaboard.project.domain.ActivityType

/**
 * Category filter for the swiping screen (All, Landmarks, Food, Activities).
 * Labels are short for the swipe dropdown; display names for results use [CategoryConstants].
 */
enum class SwipeCategory(val label: String, val type: ActivityType?) {
    ALL("All", null),
    LANDMARKS("Landmarks", ActivityType.LANDMARK),
    FOOD("Food", ActivityType.RESTAURANT),
    ACTIVITIES("Activities", ActivityType.ACTIVITY)
    ;

    companion object {
        fun fromType(type: ActivityType): SwipeCategory {
            return when (type) {
                ActivityType.LANDMARK -> LANDMARKS
                ActivityType.RESTAURANT -> FOOD
                ActivityType.ACTIVITY -> ACTIVITIES
            }
        }
    }
}
