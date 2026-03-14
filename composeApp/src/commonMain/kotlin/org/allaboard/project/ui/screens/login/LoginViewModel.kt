package org.allaboard.project.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.allaboard.project.data.repository.SupabaseClientProvider
import org.allaboard.project.domain.AllAboardModel
import org.allaboard.project.domain.User

/**
 * UI state for LoginScreen.
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null
)

/**
 * ViewModel for LoginScreen. Delegates all auth logic to AllAboardModel.
 *
 * Continuously observes Supabase [SessionStatus]. When it transitions to
 * [SessionStatus.Authenticated] — either because a session already existed on
 * launch, or because [handleAuthCallback][org.allaboard.project.MainActivity]
 * imported a new one — the ViewModel fetches the user from the backend and
 * sets [LoginUiState.user], which triggers navigation to HomeScreen.
 */
class LoginViewModel(
    private val model: AllAboardModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Observe Supabase session status for the lifetime of this ViewModel.
        // This covers:
        //  1. Already authenticated on cold start (existing session).
        //  2. Session imported after the OAuth deep-link callback.
        viewModelScope.launch {
            SupabaseClientProvider.client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        // Avoid re-fetching if we already have the user
                        if (_uiState.value.user != null) return@collect

                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        try {
                            val user = model.getCurrentUser()
                            println("user = $user")
                            if (user != null) {
                                _uiState.value = _uiState.value.copy(isLoading = false, user = user)
                            } else {
                                _uiState.value = _uiState.value.copy(isLoading = false,
                                    error = "Authenticated but backend returned no user")
                            }
                        } catch (t: Throwable) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = t.message ?: "Failed to fetch user"
                            )
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        // No session — stay on login screen, make sure loading is off
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    else -> { /* Initializing, etc. — no-op */ }
                }
            }
        }
    }

    /**
     * Initiates Google OAuth sign-in via AllAboardModel.
     * Opens the browser for the Google consent screen.
     * Navigation happens automatically when the session observer above
     * detects the new Authenticated status after the deep-link callback.
     */
    fun signIn() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                model.signInWithGoogle()
                // Don't need to do anything else here — the sessionStatus collector
                // will pick up the Authenticated event once handleAuthCallback imports
                // the session.
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = t.message ?: "Sign-in failed"
                )
            }
        }
    }
}
