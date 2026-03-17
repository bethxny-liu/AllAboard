package org.allaboard.project.ui.screens.activityDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
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
    val error: String? = null,
    /** When true, the screen should navigate back (activity was deleted). */
    val activityDeleted: Boolean = false
)

class ActivityDetailsViewModel(
    private val model: AllAboardModel,
    private val tripId: String,
    private val initialActivity: Activity? = null,
    private val activityId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityDetailsUiState(isLoading = true))
    val uiState: StateFlow<ActivityDetailsUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
        viewModelScope.launch {
            model.events.filter { it == tripId }.collect { refresh() }
        }
    }

    private fun loadDetails() {
        if (initialActivity != null) {
            _uiState.value = _uiState.value.copy(
                activity = initialActivity,
                isLoading = false,
                error = null
            )
        } else {
            fetchActivity()
        }
    }

    private fun fetchActivity() {
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

    fun toggleDescriptionExpanded() {
        _uiState.value = _uiState.value.copy(
            descriptionExpanded = !_uiState.value.descriptionExpanded
        )
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        fetchActivity()
    }

    fun deleteActivity() {
        viewModelScope.launch {
            try {
                model.deleteActivity(activityId, tripId)
                _uiState.value = _uiState.value.copy(activityDeleted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete activity"
                )
            }
        }
    }
}
