package org.allaboard.project

/**
 * Category labels used for Trip Home sections and Swiping Results filter.
 * Keeps section titles and filter dropdown in sync.
 */
object CategoryConstants {
    const val ALL = "All"
    const val LANDMARKS = "Landmarks"
    const val RESTAURANTS_AND_FOOD = "Restaurants & Food"
    const val EXPERIENCES = "Experiences"

    /** Category filter options for Swiping Results (match Trip Home sections). */
    val SWIPING_RESULT_CATEGORIES = listOf(
        ALL,
        LANDMARKS,
        RESTAURANTS_AND_FOOD,
        EXPERIENCES
    )
}
