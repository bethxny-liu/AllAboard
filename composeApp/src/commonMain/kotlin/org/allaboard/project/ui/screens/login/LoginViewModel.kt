package org.allaboard.project.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.domain.AllAboardModel

/**
 * UI state for LoginScreen.
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for LoginScreen. Accepts AllAboardModel to set the current user on sign-in.
 * Mock: sets current user to default; Sprint 3+ would validate token with backend first.
 */
class LoginViewModel(
    private val model: AllAboardModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Performs sign-in (e.g. "Sign in with Google"). Sets current user via model then invokes onSuccess.
     * Mock: uses a fixed user id; Sprint 3+ would validate token with backend first.
     */
    fun signIn(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                model.setCurrentUser("user-1")
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = t.message
                )
            }
        }
    }
}
