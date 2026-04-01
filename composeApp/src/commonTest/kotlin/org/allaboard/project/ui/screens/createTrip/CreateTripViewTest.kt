package org.allaboard.project.ui.screens.createTrip

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CreateTripViewTest {

    @Test
    fun createTripUiState_defaults() {
        val s = CreateTripUiState()
        assertEquals("", s.country)
        assertFalse(s.isEditMode)
    }

    @Test
    fun createTripUiState_editMode() {
        val s = CreateTripUiState(isEditMode = true, tripId = "tid")
        assertTrue(s.isEditMode)
        assertEquals("tid", s.tripId)
    }

    @Test
    fun createTripUiState_crewEmptyByDefault() {
        val s = CreateTripUiState()
        assertTrue(s.crew.isEmpty())
    }
}
