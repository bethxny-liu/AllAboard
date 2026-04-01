package org.allaboard.project.ui.screens.tripHome

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TripHomeViewTest {

    @Test
    fun tripHomeUiState_defaults() {
        val s = TripHomeUiState()
        assertEquals(null, s.trip)
        assertFalse(s.tripDeleted)
    }

    @Test
    fun tripHomeUiState_loading() {
        val s = TripHomeUiState(isLoading = true)
        assertTrue(s.isLoading)
    }

    @Test
    fun tripHomeUiState_error() {
        val s = TripHomeUiState(error = "failed")
        assertEquals("failed", s.error)
    }
}
