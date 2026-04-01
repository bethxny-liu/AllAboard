package org.allaboard.project.ui.screens.itinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.domain.AllAboardModel
import org.allaboard.project.domain.Itinerary
import org.allaboard.project.domain.ItineraryDay

// UI state for Itinerary screen
data class ItineraryUiState(
    val itinerary: Itinerary? = null,
    val days: List<ItineraryDay> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isExporting: Boolean = false,
    val exportMessage: String? = null
)

class ItineraryViewModel(
    private val tripId: String,
    private val model: AllAboardModel
) : ViewModel() {
    private val _uiState = MutableStateFlow(ItineraryUiState(isLoading = true))
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            model.events.collect { eventTripId ->
                if (eventTripId == tripId) {
                    refresh()
                }
            }
        }

        loadItinerary()
    }

    private fun loadItinerary() {
        viewModelScope.launch {
            try {
                val itinerary = model.getItinerary(tripId)
                _uiState.value = _uiState.value.copy(
                    itinerary = itinerary,
                    days = itinerary?.days ?: emptyList(),
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load itinerary"
                )
            }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadItinerary()
    }

    fun exportAllDaysToGoogleCalendar() {
        if (_uiState.value.isExporting) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportMessage = null)
            try {
                val created = model.exportItineraryToGoogleCalendar(tripId)
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportMessage = if (created > 0) {
                        "Added $created event${if (created == 1) "" else "s"} to Google Calendar."
                    } else {
                        "No itinerary events were exported."
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportMessage = e.message ?: "Failed to export itinerary to Google Calendar."
                )
            }
        }
    }
}
