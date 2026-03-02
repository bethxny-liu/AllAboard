package org.allaboard.project.ui.screens.createActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.allaboard.project.Category
import org.allaboard.project.domain.AllAboardModel
import org.allaboard.project.domain.ActivityType

/**
 * UI state for the Create Custom Activity screen.
 */
data class CreateCustomActivityUiState(
    val categories: List<Category> = Category.allCategories.filter { it != Category.ALL },
    val selectedCategoryIndex: Int = 0,
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val link: String = "",
    val isCreating: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) {
    val selectedCategory: Category
        get() = categories.getOrNull(selectedCategoryIndex) ?: Category.LANDMARKS
}

/**
 * ViewModel to manage Create Custom Activity state.
 * Accepts an AllAboardModel and the tripId so the view modely is scoped to a particular trip.
 */
class CreateCustomActivityViewModel(
    private val model: AllAboardModel,
    private val tripId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCustomActivityUiState())
    val uiState: StateFlow<CreateCustomActivityUiState> = _uiState.asStateFlow()

    fun updateCategory(index: Int) {
        _uiState.value = _uiState.value.copy(selectedCategoryIndex = index)
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateLink(link: String) {
        _uiState.value = _uiState.value.copy(link = link)
    }

    fun onCreateActivity() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Name required")
            return
        }

        _uiState.value = state.copy(isCreating = true, error = null)

        viewModelScope.launch {
            try {
                val activityType = state.selectedCategory.type ?: ActivityType.EXPERIENCES
                model.createActivityForTrip(
                    tripId = tripId,
                    title = state.name,
                    location = state.location,
                    description = state.description,
                    type = activityType,
                    imageUrl = null, // need to implement image upload separately
                    link = state.link.ifBlank { null }
                )
                _uiState.value = _uiState.value.copy(isCreating = false, isSuccess = true)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(isCreating = false, error = t.message)
            }
        }
    }
}
