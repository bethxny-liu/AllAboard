package org.allaboard.project.ui.screens.itinerary

import kotlinx.datetime.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel

// UI state for Itinerary screen
data class ItineraryUiState(
    val rangeStart: LocalDate = LocalDate(2026, 2, 10),
    val rangeEnd: LocalDate = LocalDate(2026, 2, 14),
    val monthToShowYear: Int = rangeStart.year,
    val monthToShowMonth: Int = rangeStart.month.ordinal + 1
)

class ItineraryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ItineraryUiState())
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    // In future: functions to update dates, load itinerary, etc.
}

// Simple data models for the placeholder
data class ItineraryItem(val time: String, val title: String)
data class DaySection(val title: String, val items: List<ItineraryItem>)
