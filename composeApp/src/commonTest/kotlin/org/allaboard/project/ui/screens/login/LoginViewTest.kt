package org.allaboard.project.ui.screens.login

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.allaboard.project.domain.User

internal class LoginViewTest {

    @Test
    fun loginUiState_defaults() {
        val s = LoginUiState()
        assertNull(s.user)
        assertEquals(false, s.isLoading)
    }

    @Test
    fun loginUiState_loadingFlag() {
        val s = LoginUiState(isLoading = true)
        assertTrue(s.isLoading)
    }

    @Test
    fun loginUiState_withUser() {
        val u = User(
            id = "u1",
            displayName = "Sam",
            email = "sam@example.com"
        )
        val s = LoginUiState(user = u, isLoading = false)
        assertEquals("Sam", s.user?.displayName)
    }
}
