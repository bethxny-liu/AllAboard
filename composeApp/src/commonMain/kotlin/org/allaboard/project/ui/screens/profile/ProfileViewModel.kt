package org.allaboard.project.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.domain.AllAboardModel
import org.allaboard.project.ui.screens.login.LoginScreen

/**
 * Simple UI state for the Profile screen.
 */
data class ProfileUiState(
    val displayName: String = "",
    val profileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val model: AllAboardModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val currentUser = model.getCurrentUser()
                _uiState.value = _uiState.value.copy(
                    displayName = currentUser?.displayName ?: "Guest",
                    profileImageUrl = currentUser?.imageUrl,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    displayName = "Guest",
                    profileImageUrl = null,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    fun logout (navigator: Navigator?) {
        viewModelScope.launch {
            try {
                model.logout()
                navigator?.replaceAll(LoginScreen())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }

}
