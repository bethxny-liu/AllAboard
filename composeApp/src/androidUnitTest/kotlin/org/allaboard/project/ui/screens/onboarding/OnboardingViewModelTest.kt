package org.allaboard.project.ui.screens.onboarding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.allaboard.project.ViewModelTestBase
import org.allaboard.project.domain.BudgetLevel
import org.allaboard.project.domain.TravelVibe
import org.allaboard.project.allAboardModelFromMocks

internal class OnboardingViewModelTest : ViewModelTestBase() {

    private val model = allAboardModelFromMocks()

    @Test
    fun updateBudget_changesState() {
        val vm = OnboardingViewModel(model, editMode = false)
        vm.updateBudget(BudgetLevel.HIGH)
        assertEquals(BudgetLevel.HIGH, vm.uiState.value.budget)
    }

    @Test
    fun updateVibe_changesState() {
        val vm = OnboardingViewModel(model, editMode = false)
        vm.updateVibe(TravelVibe.RELAXED)
        assertEquals(TravelVibe.RELAXED, vm.uiState.value.vibe)
    }

    @Test
    fun toggleInterest_addsAndRemoves() {
        val vm = OnboardingViewModel(model, editMode = false)
        vm.toggleInterest("Food")
        assertTrue("Food" in vm.uiState.value.interests)
        vm.toggleInterest("Food")
        assertFalse("Food" in vm.uiState.value.interests)
    }
}
