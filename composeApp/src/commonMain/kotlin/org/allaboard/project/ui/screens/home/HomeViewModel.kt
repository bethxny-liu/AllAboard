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
    val displayName: String = "",
    val searchQuery: String = "",
    val upcomingTrips: List<TripSummary> = listOf(
        TripSummary(
            id = "1",
            title = "Japan",
            dateRange = "Dec 15 - Jan 22",
            memberCount = 4,
            imageUrl = null
        )
    ),
    val pastTrips: List<TripSummary> = listOf(
        TripSummary(
            id = "2",
            title = "Banff",
            dateRange = "Dec 15 - Jan 22",
            memberCount = 4,
            imageUrl = null
        )
    ),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for HomeScreen that manages the UI state
 */
class HomeViewModel(
    private val model: AllAboardModel
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTrips()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = model.getCurrentUser()
            _uiState.value = _uiState.value.copy(displayName = user?.displayName ?: "")
        }
    }

    private fun loadTrips() {
        // TODO: Implement actual data loading from repository/API
        // For now, use the default state with sample data
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

}
