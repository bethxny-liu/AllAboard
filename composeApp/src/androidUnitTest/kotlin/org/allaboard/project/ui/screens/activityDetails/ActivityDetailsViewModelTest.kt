package org.allaboard.project.ui.screens.activityDetails

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.allaboard.project.ViewModelTestBase
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.allAboardModelFromMocks

internal class ActivityDetailsViewModelTest : ViewModelTestBase() {

    private val model = allAboardModelFromMocks()

    @Test
    fun initialActivity_showsInState() {
        val act = Activity(
            id = "act-1",
            title = "Test",
            location = "Here",
            description = "Desc",
            type = ActivityType.LANDMARK
        )
        val vm = ActivityDetailsViewModel(model, tripId = "trip-1", initialActivity = act, activityId = "act-1")
        assertEquals("Test", vm.uiState.value.activity?.title)
    }

    @Test
    fun initialActivity_notLoadingAfterSet() {
        val act = Activity(
            id = "act-1",
            title = "T",
            location = "L",
            description = null,
            type = ActivityType.LANDMARK
        )
        val vm = ActivityDetailsViewModel(model, tripId = "trip-1", initialActivity = act, activityId = "act-1")
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun initialActivity_matchesId() {
        val act = Activity(
            id = "act-2",
            title = "X",
            location = "Y",
            description = null,
            type = ActivityType.RESTAURANT
        )
        val vm = ActivityDetailsViewModel(model, tripId = "trip-1", initialActivity = act, activityId = "act-2")
        assertEquals("act-2", vm.uiState.value.activity?.id)
    }
}
