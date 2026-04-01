package org.allaboard.project.ui.screens.createActivity

import kotlin.test.Test
import kotlin.test.assertEquals
import org.allaboard.project.ViewModelTestBase
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.allAboardModelFromMocks

internal class CreateCustomActivityViewModelTest : ViewModelTestBase() {

    private val model = allAboardModelFromMocks()

    @Test
    fun updateName_storesValue() {
        val vm = CreateCustomActivityViewModel(model, tripId = "trip-1", existingActivity = null)
        vm.updateName("Hello")
        assertEquals("Hello", vm.uiState.value.name)
    }

    @Test
    fun updateLocation_storesValue() {
        val vm = CreateCustomActivityViewModel(model, tripId = "trip-1", existingActivity = null)
        vm.updateLocation("Downtown")
        assertEquals("Downtown", vm.uiState.value.location)
    }

    @Test
    fun editMode_prefillsFromExistingActivity() {
        val existing = Activity(
            id = "e1",
            title = "Old",
            location = "There",
            description = "d",
            type = ActivityType.LANDMARK
        )
        val vm = CreateCustomActivityViewModel(model, tripId = "trip-1", existingActivity = existing)
        assertEquals("Old", vm.uiState.value.name)
    }
}
