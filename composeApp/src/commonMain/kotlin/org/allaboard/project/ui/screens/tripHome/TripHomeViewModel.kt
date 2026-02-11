package org.allaboard.project.ui.screens.tripHome

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType



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
    // Use unified activities from domain
    val activities: List<Activity> = listOf(
        Activity(
            id = "act-1",
            title = "Senso-ji Temple",
            location = "Asakusa, Tokyo",
            description = "Historic temple with vibrant market streets and iconic Kaminarimon gate.",
            rating = 4.7f,
            priceLevel = "$$",
            mapPinLabel = "Senso-ji",
            voteCount = 3,
            imageUrl = null,
            type = ActivityType.LANDMARK
        ),
        Activity(
            id = "act-1-2",
            title = "Senso-ji Temple",
            location = "Asakusa, Tokyo",
            description = "Historic temple with vibrant market streets and iconic Kaminarimon gate.",
            rating = 4.7f,
            priceLevel = "$$",
            mapPinLabel = "Senso-ji",
            voteCount = 3,
            imageUrl = null,
            type = ActivityType.LANDMARK
        ),
        Activity(
            id = "act-2",
            title = "Ichiran Ramen",
            location = "Shibuya, Tokyo",
            description = "Solo-booth ramen experience known for rich tonkotsu broth.",
            rating = 4.5f,
            priceLevel = "$",
            mapPinLabel = "Ichiran",
            voteCount = 4,
            imageUrl = null,
            type = ActivityType.RESTAURANT
        ),
        Activity(
            id = "act-2-2",
            title = "Ichiran Ramen",
            location = "Shibuya, Tokyo",
            description = "Solo-booth ramen experience known for rich tonkotsu broth.",
            rating = 4.5f,
            priceLevel = "$",
            mapPinLabel = "Ichiran",
            voteCount = 4,
            imageUrl = null,
            type = ActivityType.RESTAURANT
        ),
        Activity(
            id = "act-3",
            title = "Mount Fuji Day Trip",
            location = "Yamanashi Prefecture",
            description = "Scenic day trip with lake views and photo spots near Fuji. asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfadsfasdfad",
            rating = 4.8f,
            priceLevel = "$$$",
            mapPinLabel = "Fuji Viewpoint",
            voteCount = 4,
            imageUrl = null,
            type = ActivityType.EXPERIENCES
        ),
        Activity(
            id = "act-3-2",
            title = "Mount Fuji Day Trip",
            location = "Yamanashi Prefecture",
            description = "Scenic day trip with lake views and photo spots near Fuji. asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfadsfasdfad",
            rating = 4.8f,
            priceLevel = "$$$",
            mapPinLabel = "Fuji Viewpoint",
            voteCount = 4,
            imageUrl = null,
            type = ActivityType.EXPERIENCES
        )
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

    fun onViewItineraryClick() {
        // TODO: Navigate to itinerary screen
    }
}
