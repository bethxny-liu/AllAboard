package org.allaboard.project.ui.screens.activityDetails

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.allaboard.project.domain.Activity

/**
 * UI state for the Activity Details screen.
 */
data class ActivityDetailsUiState(
    val activity: Activity? = null,
    val descriptionExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ActivityDetailsViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityDetailsUiState())
    val uiState: StateFlow<ActivityDetailsUiState> = _uiState.asStateFlow()

    fun loadDetails(activity: Activity, fallbackActivityId: String) {
        _uiState.value = _uiState.value.copy(
            activity = activity,
            isLoading = false,
            error = null
        )
    }

    fun toggleDescriptionExpanded() {
        _uiState.value = _uiState.value.copy(
            descriptionExpanded = !_uiState.value.descriptionExpanded
        )
    }
}
