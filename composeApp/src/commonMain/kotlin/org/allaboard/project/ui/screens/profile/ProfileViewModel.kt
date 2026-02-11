package org.allaboard.project.ui.screens.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple UI state for the Profile screen.
 */
data class ProfileUiState(
    val displayName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Provide placeholder values for now
        _uiState.value = _uiState.value.copy(
            displayName = "Spongebob",
            isLoading = false,
            error = null
        )
    }
}
