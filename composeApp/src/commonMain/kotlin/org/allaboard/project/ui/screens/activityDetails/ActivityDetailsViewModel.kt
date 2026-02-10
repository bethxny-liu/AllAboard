package org.allaboard.project.ui.screens.activityDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Activity Details screen.
 */
data class ActivityDetailsUiState(
    val details: ActivityDetails? = null,
    val descriptionExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Activity Details screen.
 * Loads details via [ActivityDetailsRepository]; swap the implementation (e.g. OpenTripMap) when the backend is ready.
 */
class ActivityDetailsViewModel(
    private val repository: ActivityDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityDetailsUiState())
    val uiState: StateFlow<ActivityDetailsUiState> = _uiState.asStateFlow()

    fun loadDetails(activityId: String, fallbackTitle: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getDetails(activityId, fallbackTitle)
                .fold(
                    onSuccess = { details ->
                        _uiState.value = _uiState.value.copy(
                            details = details,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            details = null,
                            isLoading = false,
                            error = e.message ?: "Could not load details"
                        )
                    }
                )
        }
    }

    fun toggleDescriptionExpanded() {
        _uiState.value = _uiState.value.copy(
            descriptionExpanded = !_uiState.value.descriptionExpanded
        )
    }
}
