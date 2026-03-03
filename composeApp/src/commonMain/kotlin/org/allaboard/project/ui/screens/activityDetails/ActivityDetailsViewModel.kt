package org.allaboard.project.ui.screens.activityDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.AllAboardModel

/**
 * UI state for the Activity Details screen.
 */
data class ActivityDetailsUiState(
    val activity: Activity? = null,
    val descriptionExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ActivityDetailsViewModel(
    private val model: AllAboardModel,
    private val initialActivity: Activity? = null,
    private val activityId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityDetailsUiState(isLoading = true))
    val uiState: StateFlow<ActivityDetailsUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
    }

    private fun loadDetails() {
        // If an Activity is provided, use it directly
        if (initialActivity != null) {
            _uiState.value = _uiState.value.copy(
                activity = initialActivity,
                isLoading = false,
                error = null
            )
        } else {
            // Otherwise, fetch from the model using the activityId
            viewModelScope.launch {
                try {
                    val activity = model.getActivity(activityId)
                    if (activity != null) {
                        _uiState.value = _uiState.value.copy(
                            activity = activity,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Activity not found"
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load activity"
                    )
                }
            }
        }
    }

    fun toggleDescriptionExpanded() {
        _uiState.value = _uiState.value.copy(
            descriptionExpanded = !_uiState.value.descriptionExpanded
        )
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadDetails()
    }
}
