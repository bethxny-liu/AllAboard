package org.allaboard.project.ui.screens.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ProfileViewTest {

    @Test
    fun profileUiState_guest() {
        val s = ProfileUiState(displayName = "Guest")
        assertEquals("Guest", s.displayName)
        assertFalse(s.isLoading)
    }

    @Test
    fun profileUiState_withImage() {
        val s = ProfileUiState(displayName = "A", profileImageUrl = "https://example.com/x.png")
        assertTrue(s.profileImageUrl!!.endsWith(".png"))
    }

    @Test
    fun profileUiState_errorOptional() {
        val s = ProfileUiState(error = "network")
        assertEquals("network", s.error)
    }
}
