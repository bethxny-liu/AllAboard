package org.allaboard.project.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.domain.AllAboardModel
import org.allaboard.project.domain.displayDateRange

/**
 * Data class representing a trip summary for the home screen
 */
data class TripSummary(
    val id: String,
    val title: String,
    val dateRange: String,
    val memberCount: Int,
    val imageUrl: String? = null
)

/**
 * UI State for HomeScreen
 */
data class HomeUiState(
    val displayName: String = "",
    val searchQuery: String = "",
    val upcomingTrips: List<TripSummary> = emptyList(),
    val pastTrips: List<TripSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for HomeScreen that manages the UI state
 */
class HomeViewModel(private val model: AllAboardModel) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Listen for events from the model (e.g., trip created/updated) and refresh
        viewModelScope.launch {
            try {
                model.events.collect {
                    // Refresh trips when any event is received
                    loadTrips()
                }
            } catch (_: Throwable) {
                // ignore collection errors
            }
        }

        loadTrips()
    }

    private fun mapTrip(t: org.allaboard.project.domain.Trip): TripSummary = TripSummary(
        id = t.id,
        title = t.destination.ifBlank { t.title },
        dateRange = t.displayDateRange,
        memberCount = t.memberCount,
        imageUrl = t.imageUrl
    )

    private fun loadTrips() {
        viewModelScope.launch {
            try {
                val currentUser = model.getCurrentUser()
                val trips = model.getAllTripsForUser()
                val upcoming = model.getUpcomingTrips(trips).map { mapTrip(it) }
                val past = model.getPastTrips(trips).map { mapTrip(it) }
                _uiState.value = _uiState.value.copy(upcomingTrips = upcoming, pastTrips = past, isLoading = false, displayName = currentUser?.displayName ?: "")
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = t.message)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

}
