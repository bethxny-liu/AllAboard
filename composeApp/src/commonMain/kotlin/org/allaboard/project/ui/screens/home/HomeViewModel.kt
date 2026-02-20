package org.allaboard.project.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.domain.AllAboardModel

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
        loadTrips()
    }

    private fun mapTrip(t: org.allaboard.project.domain.Trip): TripSummary = TripSummary(
        id = t.id,
        title = t.destination.ifBlank { t.title },
        dateRange = "${t.startDate} - ${t.endDate}",
        memberCount = t.memberCount,
        imageUrl = t.imageUrl
    )

    private fun loadTrips() {
        viewModelScope.launch {
            try {
                // Try to use current user to fetch personalized trips; fallback to empty
                val currentUser = model.getCurrentUser()
                val userId = currentUser?.id ?: "user-1"
                val upcoming = model.getUpcomingTrips(userId).map { mapTrip(it) }
                val past = model.getPastTrips(userId).map { mapTrip(it) }
                _uiState.value = _uiState.value.copy(upcomingTrips = upcoming, pastTrips = past, isLoading = false)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = t.message)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

}
