package org.allaboard.project.ui.screens.activityDetails

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType

internal class ActivityDetailsViewTest {

    private val sampleActivity = Activity(
        id = "x1",
        title = "Spot",
        location = "Somewhere",
        description = null,
        type = ActivityType.LANDMARK
    )

    @Test
    fun activityDetailsUiState_toggleExpanded() {
        val s = ActivityDetailsUiState(activity = sampleActivity, descriptionExpanded = false)
        val toggled = s.copy(descriptionExpanded = true)
        assertTrue(toggled.descriptionExpanded)
    }

    @Test
    fun activityDetailsUiState_deletedFlag() {
        val s = ActivityDetailsUiState(activity = sampleActivity, activityDeleted = true)
        assertTrue(s.activityDeleted)
    }

    @Test
    fun activityDetailsUiState_loading() {
        val s = ActivityDetailsUiState(isLoading = true)
        assertTrue(s.isLoading)
        assertFalse(s.activityDeleted)
    }
}
