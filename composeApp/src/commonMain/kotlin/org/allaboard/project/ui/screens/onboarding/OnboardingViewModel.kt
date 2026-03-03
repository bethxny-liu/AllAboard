package org.allaboard.project.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.domain.AllAboardModel
import org.allaboard.project.domain.BudgetLevel
import org.allaboard.project.domain.TravelVibe

/**
 * UI state for all onboarding user data.
 */
data class OnboardingUiState(
    val budget: BudgetLevel = BudgetLevel.MEDIUM,
    val vibe: TravelVibe = TravelVibe.BALANCED,
    val interests: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for onboarding that persists user preferences via AllAboardModel.
 */
class OnboardingViewModel(
    private val model: AllAboardModel,
    private val editMode: Boolean
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        if (editMode) {
            loadCurrentUserPreferences()
        }
    }

    private fun loadCurrentUserPreferences() {
        viewModelScope.launch {
            try {
                val user = model.getCurrentUser() ?: return@launch
                _uiState.value = _uiState.value.copy(
                    budget = user.budget,
                    vibe = user.travelVibe,
                    interests = user.interests
                )
            } catch (_: Exception) {
                // Keep defaults if user preferences cannot be loaded.
            }
        }
    }

    fun updateBudget(value: BudgetLevel) {
        _uiState.value = _uiState.value.copy(budget = value)
    }

    fun updateVibe(value: TravelVibe) {
        _uiState.value = _uiState.value.copy(vibe = value)
    }

    fun setInterests(value: Set<String>) {
        _uiState.value = _uiState.value.copy(interests = value)
    }

    fun toggleInterest(interest: String) {
        val current = _uiState.value.interests
        _uiState.value = _uiState.value.copy(
            interests = if (interest in current) current - interest else current + interest
        )
    }

    /**
     * Save user preferences to the model and call onComplete when done.
     */
    fun savePreferences(onComplete: () -> Unit) {
        _uiState.value = _uiState.value.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                val user = model.getCurrentUser()
                if (user != null) {
                    model.updateUserPreferences(
                        userId = user.id,
                        budget = _uiState.value.budget,
                        vibe = _uiState.value.vibe,
                        interests = _uiState.value.interests
                    )
                }
                _uiState.value = _uiState.value.copy(isSaving = false)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save preferences"
                )
            }
        }
    }
}
