package org.allaboard.project.ui.screens.login

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import org.allaboard.project.ViewModelTestBase

/** Smoke tests around [LoginViewModel] / [LoginUiState]; auth wiring stays in the app. */
internal class LoginViewModelTest : ViewModelTestBase() {

    @Test
    fun loginViewModel_isNamedCorrectly() {
        assertEquals("LoginViewModel", LoginViewModel::class.simpleName)
    }

    @Test
    fun loginUiState_defaultMatchesTypicalInitialFlow() {
        val s = LoginUiState()
        assertNull(s.user)
        assertFalse(s.isLoading)
        assertNull(s.error)
    }

    @Test
    fun loginUiState_withError() {
        val s = LoginUiState(isLoading = false, error = "oops", user = null)
        assertEquals("oops", s.error)
    }
}
