package org.allaboard.project.ui.screens.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.allaboard.project.ViewModelTestBase
import org.allaboard.project.allAboardModelFromMocks

internal class ProfileViewModelTest : ViewModelTestBase() {

    private val model = allAboardModelFromMocks()

    @Test
    fun profileViewModel_simpleName() {
        assertEquals("ProfileViewModel", ProfileViewModel::class.simpleName)
    }

    @Test
    fun uiState_exposesProfileUiState() {
        val vm = ProfileViewModel(model)
        assertTrue(vm.uiState.value is ProfileUiState)
    }

    @Test
    fun profileUiState_errorEdgeCase() {
        val s = ProfileUiState(error = "timeout", isLoading = false)
        assertEquals("timeout", s.error)
        assertFalse(s.isLoading)
    }

    @Test
    fun profileUiState_loadingVsGuest() {
        val loading = ProfileUiState(displayName = "", isLoading = true)
        val guest = ProfileUiState(displayName = "Guest", isLoading = false)
        assertTrue(loading.isLoading)
        assertTrue(guest.displayName.isNotEmpty())
    }
}
