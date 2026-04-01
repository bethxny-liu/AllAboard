package org.allaboard.project.ui.screens.createActivity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CreateCustomActivityViewTest {

    @Test
    fun createCustomActivityUiState_selectedCategory() {
        val s = CreateCustomActivityUiState(selectedCategoryIndex = 0)
        assertTrue(s.categories.isNotEmpty())
        assertEquals(s.categories[0], s.selectedCategory)
    }

    @Test
    fun createCustomActivityUiState_fields() {
        val s = CreateCustomActivityUiState(name = "N", location = "L", description = "D", link = "http://x")
        assertEquals("N", s.name)
        assertEquals("L", s.location)
        assertEquals("D", s.description)
        assertEquals("http://x", s.link)
    }

    @Test
    fun createCustomActivityUiState_flags() {
        val s = CreateCustomActivityUiState(isCreating = true, isSuccess = false)
        assertTrue(s.isCreating)
        assertFalse(s.isSuccess)
    }
}
