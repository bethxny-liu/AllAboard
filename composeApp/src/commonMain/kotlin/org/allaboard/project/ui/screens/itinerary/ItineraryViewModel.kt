package org.allaboard.project.ui.screens.itinerary

import kotlinx.datetime.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import kotlin.collections.listOf

// UI state for Itinerary screen
data class ItineraryUiState(
    val rangeStart: LocalDate = LocalDate(2026, 2, 10),
    val rangeEnd: LocalDate = LocalDate(2026, 2, 14),
    val monthToShowYear: Int = rangeStart.year,
    val monthToShowMonth: Int = rangeStart.month.ordinal + 1,
    val days: List<DaySection> = emptyList()
)

class ItineraryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        ItineraryUiState(
            days = listOf(
                DaySection(
                    title = "Day 1: Jan 23",
                    items = listOf(
                        ItineraryItem("10:00 AM", "Pretty Place"),
                        ItineraryItem("2:00 PM", "Restaurant")
                    )
                ),
                DaySection(
                    title = "Day 2: Jan 24",
                    items = listOf(
                        ItineraryItem("10:00 AM", "Pretty Place"),
                        ItineraryItem("2:00 PM", "Restaurant")
                    )
                ),
                DaySection(
                    title = "Day 3: Jan 25",
                    items = listOf(
                        ItineraryItem("10:00 AM", "Pretty Place"),
                        ItineraryItem("2:00 PM", "Restaurant")
                    )
                ),
                DaySection(
                    title = "Day 4: Jan 26",
                    items = listOf(
                        ItineraryItem("10:00 AM", "Pretty Place"),
                        ItineraryItem("2:00 PM", "Restaurant")
                    )
                )
            )
        )
    )
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    // In the future: functions to update dates, load itinerary, etc.
}

// Simple data models for the placeholder
data class ItineraryItem(val time: String, val title: String)
data class DaySection(val title: String, val items: List<ItineraryItem>)
