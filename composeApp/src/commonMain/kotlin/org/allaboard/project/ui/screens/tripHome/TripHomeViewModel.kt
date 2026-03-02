package org.allaboard.project.ui.screens.tripHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.domain.AllAboardModel
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.Trip

/**
 * UI State for TripHomeScreen
 */
data class TripHomeUiState(
    val trip: Trip? = null,
    val activities: List<Activity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for TripHomeScreen that manages the UI state and loads data from AllAboardModel
 */
class TripHomeViewModel(
    private val model: AllAboardModel,
    private val tripId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripHomeUiState())
    val uiState: StateFlow<TripHomeUiState> = _uiState.asStateFlow()

    init {
        // Listen for events from the model (e.g., votes updated) and refresh when relevant
        viewModelScope.launch {
            try {
                model.events.collect { eventTripId ->
                    if (eventTripId == tripId) {
                        refresh()
                    }
                }
            } catch (_: Throwable) {
                // ignore collection errors
            }
        }

        refresh()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                // Use model helper that returns activities with vote counts merged
                val dashboard = model.getTripDashboardWithMergedActivityVotes(tripId)
                val trip = dashboard.trip // may be null
                val activities = dashboard.activities

                _uiState.value = _uiState.value.copy(trip = trip, activities = activities, isLoading = false)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = t.message)
            }
        }
    }
}
