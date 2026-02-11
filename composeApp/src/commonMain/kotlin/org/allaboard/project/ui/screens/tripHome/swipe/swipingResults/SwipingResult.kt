package org.allaboard.project.ui.screens.tripHome.swipe.swipingResults

import org.allaboard.project.CategoryConstants
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType

/**
 * Result of swiping for a single activity: the activity plus vote (yes-swipe) count
 * and the names of participants who voted yes.
 */
data class SwipingResult(
    val activity: Activity,
    val voteCount: Int,
    val voterNames: List<String>
) {
    /** Display category for filtering (matches Swiping Results dropdown). */
    val category: String
        get() = when (activity.type) {
            ActivityType.LANDMARK -> CategoryConstants.LANDMARKS
            ActivityType.RESTAURANT -> CategoryConstants.RESTAURANTS_AND_FOOD
            ActivityType.ACTIVITY -> CategoryConstants.EXPERIENCES
        }

    /** Display text for voters, e.g. "Daniel, Rachael + 2 more" */
    fun voterDisplayText(maxVisible: Int = 2): String {
        if (voterNames.isEmpty()) return "No votes"
        val visible = voterNames.take(maxVisible)
        val extra = voterNames.size - visible.size
        return if (extra > 0) {
            "${visible.joinToString(", ")} + $extra more"
        } else {
            visible.joinToString(", ")
        }
    }
}
