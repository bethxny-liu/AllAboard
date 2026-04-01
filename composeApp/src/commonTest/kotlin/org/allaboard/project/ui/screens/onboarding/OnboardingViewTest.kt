package org.allaboard.project.ui.screens.onboarding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.allaboard.project.domain.BudgetLevel
import org.allaboard.project.domain.TravelVibe

internal class OnboardingViewTest {

    @Test
    fun onboardingUiState_defaults() {
        val s = OnboardingUiState()
        assertEquals(BudgetLevel.MEDIUM, s.budget)
        assertEquals(TravelVibe.BALANCED, s.vibe)
    }

    @Test
    fun onboardingUiState_customBudget() {
        val s = OnboardingUiState(budget = BudgetLevel.LOW)
        assertEquals(BudgetLevel.LOW, s.budget)
    }

    @Test
    fun onboardingUiState_interests() {
        val s = OnboardingUiState(interests = setOf("Museums", "Food"))
        assertTrue(s.interests.contains("Museums"))
        assertEquals(2, s.interests.size)
    }
}
