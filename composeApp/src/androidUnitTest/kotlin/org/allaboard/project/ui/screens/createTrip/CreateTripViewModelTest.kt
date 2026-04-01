package org.allaboard.project.ui.screens.createTrip

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.allaboard.project.ViewModelTestBase
import org.allaboard.project.allAboardModelFromMocks

internal class CreateTripViewModelTest : ViewModelTestBase() {

    private val model = allAboardModelFromMocks()

    @Test
    fun updateCountry_storesValue() {
        val vm = CreateTripViewModel(model)
        vm.updateCountry("Canada")
        assertEquals("Canada", vm.uiState.country)
    }

    @Test
    fun updateRegion_storesValue() {
        val vm = CreateTripViewModel(model)
        vm.updateRegion("BC")
        assertEquals("BC", vm.uiState.region)
    }

    @Test
    fun validateRequiredFields_falseWhenEmpty() {
        val vm = CreateTripViewModel(model)
        val ok = vm.validateRequiredFields()
        assertFalse(ok)
    }
}
