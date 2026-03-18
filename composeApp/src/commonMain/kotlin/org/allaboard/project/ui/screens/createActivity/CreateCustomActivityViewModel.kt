package org.allaboard.project.ui.screens.createActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.allaboard.project.Category
import org.allaboard.project.domain.Activity
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
 * When [existingActivity] is non-null, the screen is in edit mode.
 */
class CreateCustomActivityViewModel(
    private val model: AllAboardModel,
    private val tripId: String,
    private val existingActivity: Activity? = null
) : ViewModel() {

    private val categories = Category.allCategories.filter { it != Category.ALL }

    private val _uiState = MutableStateFlow(
        if (existingActivity != null) {
            val index = categories.indexOfFirst { it.type == existingActivity.type }.takeIf { it >= 0 } ?: 0
            CreateCustomActivityUiState(
                categories = categories,
                selectedCategoryIndex = index,
                name = existingActivity.title,
                location = existingActivity.location,
                description = existingActivity.description ?: "",
                link = existingActivity.link ?: ""
            )
        } else {
            CreateCustomActivityUiState(categories = categories)
        }
    )
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

    fun onCreateOrUpdateActivity() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Name required")
            return
        }

        _uiState.value = state.copy(isCreating = true, error = null)

        viewModelScope.launch {
            try {
                val activityType = state.selectedCategory.type ?: ActivityType.EXPERIENCES
                if (existingActivity != null) {
                    val updated = existingActivity.copy(
                        title = state.name,
                        location = state.location,
                        description = state.description,
                        type = activityType,
                        link = state.link.ifBlank { null },
                        mapPinLabel = state.name.ifEmpty { state.location }
                    )
                    model.updateActivity(updated, tripId)
                } else {
                    model.createActivityForTrip(
                        tripId = tripId,
                        title = state.name,
                        location = state.location,
                        description = state.description,
                        type = activityType,
                        imageUrl = null,
                        link = state.link.ifBlank { null }
                    )
                }
                _uiState.value = _uiState.value.copy(isCreating = false, isSuccess = true)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(isCreating = false, error = t.message)
            }
        }
    }
}
