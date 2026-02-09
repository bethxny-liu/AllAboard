package org.allaboard.project.ui.screens.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Single ViewModel for all onboarding user data.
 * Persists budget, vibe, and interests whenever the user clicks "Next" or "Finish".
 */
data class OnboardingUiState(
    val budget: BudgetLevel? = null,
    val vibe: String? = null,
    val interests: Set<String> = emptySet()
)

class OnboardingViewModel : ViewModel() {

    var uiState by mutableStateOf(OnboardingUiState())
        private set

    fun updateBudget(value: BudgetLevel) {
        uiState = uiState.copy(budget = value)
    }

    fun updateVibe(value: String?) {
        uiState = uiState.copy(vibe = value)
    }

    fun setInterests(value: Set<String>) {
        uiState = uiState.copy(interests = value)
    }

    fun toggleInterest(interest: String) {
        val current = uiState.interests
        uiState = uiState.copy(
            interests = if (interest in current) current - interest else current + interest
        )
    }
}
