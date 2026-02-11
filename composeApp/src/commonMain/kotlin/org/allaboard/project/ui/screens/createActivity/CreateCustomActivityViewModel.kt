package org.allaboard.project.ui.screens.createActivity

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI state for the Create Custom Activity screen.
 */
data class CreateCustomActivityUiState(
    val category: String = "Landmark",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val isCreating: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel to manage Create Custom Activity state.
 */
class CreateCustomActivityViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCustomActivityUiState())
    val uiState: StateFlow<CreateCustomActivityUiState> = _uiState.asStateFlow()

    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
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

    fun onCreateActivity() {
        _uiState.value = _uiState.value.copy(isCreating = true)
        // TODO: Perform create action and reset isCreating when done
    }
}
