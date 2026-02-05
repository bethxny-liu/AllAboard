package org.allaboard.project.ui.screens.tripHome

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Data class representing a landmark
 */
data class Landmark(
    val id: String,
    val title: String,
    val voteCount: Int,
    val imageUrl: String? = null
)

/**
 * Data class representing a restaurant
 */
data class Restaurant(
    val id: String,
    val title: String,
    val voteCount: Int,
    val imageUrl: String? = null
)

/**
 * Data class representing an activity
 */
data class Activity(
    val id: String,
    val title: String,
    val voteCount: Int,
    val imageUrl: String? = null
)

/**
 * Data class representing a trip
 */
data class Trip(
    val id: String,
    val title: String,
    val dateRange: String,
    val memberCount: Int,
    val imageUrl: String? = null
)

/**
 * UI State for TripHomeScreen
 */
data class TripHomeUiState(
    val trip: Trip = Trip(
        id = "1",
        title = "All Aboard to Japan!",
        dateRange = "Dec 15 - Jan 22",
        memberCount = 4,
        imageUrl = null
    ),
    val landmarks: List<Landmark> = listOf(
        Landmark("1", "Mount Fuji", 4),
        Landmark("2", "Mount Fuji", 3)
    ),
    val restaurants: List<Restaurant> = listOf(
        Restaurant("1", "Ichiran Ramen", 4),
        Restaurant("2", "Ichiran Ramen", 3)
    ),
    val activities: List<Activity> = listOf(
        Activity("1", "Big Camera Store", 0),
        Activity("2", "Big Camera Store", 0)
    ),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for TripHomeScreen that manages the UI state
 */
class TripHomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TripHomeUiState())
    val uiState: StateFlow<TripHomeUiState> = _uiState.asStateFlow()

    init {
        loadTripData()
    }

    private fun loadTripData() {
        // TODO: Implement actual data loading from repository/API
        // For now, use the default state with sample data
    }

    fun onStartSwipingClick() {
        // TODO: Navigate to swiping/discovery screen
    }

    fun onViewItineraryClick() {
        // TODO: Navigate to itinerary screen
    }

    fun onSeeAllLandmarks() {
        // TODO: Navigate to landmarks list screen
    }

    fun onSeeAllRestaurants() {
        // TODO: Navigate to restaurants list screen
    }

    fun onSeeAllActivities() {
        // TODO: Navigate to activities list screen
    }

    fun onLandmarkClick(landmarkId: String) {
        // TODO: Navigate to landmark details
    }

    fun onRestaurantClick(restaurantId: String) {
        // TODO: Navigate to restaurant details
    }

    fun onActivityClick(activityId: String) {
        // TODO: Navigate to activity details
    }
}
