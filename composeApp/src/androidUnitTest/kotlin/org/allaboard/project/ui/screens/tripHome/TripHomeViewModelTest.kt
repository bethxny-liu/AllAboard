package org.allaboard.project.ui.screens.tripHome

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.allaboard.project.ViewModelTestBase
import org.allaboard.project.allAboardModelFromMocks
import org.allaboard.project.domain.Trip
import org.allaboard.project.domain.TripStatus

internal class TripHomeViewModelTest : ViewModelTestBase() {

    private val model = allAboardModelFromMocks()

    @Test
    fun tripHomeViewModel_simpleName() {
        assertEquals("TripHomeViewModel", TripHomeViewModel::class.simpleName)
    }

    @Test
    fun activities_fieldIsAlwaysList() {
        val vm = TripHomeViewModel(model, tripId = "trip-1")
        assertTrue(vm.uiState.value.activities is List<*>)
    }

    @Test
    fun tripDeleted_startsFalse() {
        val vm = TripHomeViewModel(model, tripId = "trip-1")
        assertFalse(vm.uiState.value.tripDeleted)
    }

    @Test
    fun tripHomeUiState_withTripAndErrorMutuallyPossibleShape() {
        val t = Trip(
            id = "x",
            title = "T",
            destination = "D",
            region = "R",
            startDate = "a",
            endDate = "b",
            status = TripStatus.UPCOMING
        )
        val s = TripHomeUiState(trip = t, error = "retry")
        assertEquals("x", s.trip?.id)
        assertEquals("retry", s.error)
    }

    @Test
    fun tripHomeUiState_deletedFlag() {
        val s = TripHomeUiState(tripDeleted = true)
        assertTrue(s.tripDeleted)
    }
}
