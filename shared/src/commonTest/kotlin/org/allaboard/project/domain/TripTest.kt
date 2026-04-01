package org.allaboard.project.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for Trip and TripStatus. Verifies Trip creation and all properties; memberCount for
 * empty and non-empty members; dateRange and displayDateRange (range and single-day); toTripDisplayDate
 * for valid ISO dates and invalid format returning original string; TripStatus enum values.
 */
internal class TripTest {

    private fun createUser(id: String) = User(id = id, displayName = "User$id", email = "$id@test.com")

    @Test
    fun trip_creation_storesProperties() {
        val creator = createUser("u1")
        val trip = Trip(
            id = "t1",
            title = "Trip to Japan",
            destination = "Tokyo",
            region = "Kanto",
            startDate = "2025-01-10",
            endDate = "2025-01-20",
            imageUrl = "https://example.com/img.jpg",
            status = TripStatus.UPCOMING,
            members = listOf(creator)
        )
        assertEquals("t1", trip.id)
        assertEquals("Trip to Japan", trip.title)
        assertEquals("Tokyo", trip.destination)
        assertEquals("Kanto", trip.region)
        assertEquals("2025-01-10", trip.startDate)
        assertEquals("2025-01-20", trip.endDate)
        assertEquals(TripStatus.UPCOMING, trip.status)
        assertEquals(1, trip.members.size)
        assertEquals(1, trip.memberCount)
    }

    @Test
    fun trip_memberCount_reflectsMembersSize() {
        val trip = Trip(
            id = "t2",
            title = "Solo",
            destination = "A",
            region = "B",
            startDate = "2025-01-01",
            endDate = "2025-01-01",
            members = emptyList()
        )
        assertEquals(0, trip.memberCount)
    }

    @Test
    fun trip_dateRange_returnsFormattedRange() {
        val trip = Trip(
            id = "t3",
            title = "X",
            destination = "Y",
            region = "Z",
            startDate = "2025-03-01",
            endDate = "2025-03-10",
            members = emptyList()
        )
        assertEquals("2025-03-01 - 2025-03-10", trip.dateRange)
    }

    @Test
    fun trip_displayDateRange_formatsIsoToDisplay() {
        val trip = Trip(
            id = "t4",
            title = "X",
            destination = "Y",
            region = "Z",
            startDate = "2025-06-15",
            endDate = "2025-06-20",
            members = emptyList()
        )
        assertEquals("June 15 - June 20", trip.displayDateRange)
    }

    @Test
    fun tripStatus_enumValues_exist() {
        assertEquals(TripStatus.UPCOMING, TripStatus.UPCOMING)
        assertEquals(TripStatus.ONGOING, TripStatus.ONGOING)
        assertEquals(TripStatus.COMPLETED, TripStatus.COMPLETED)
    }

    @Test
    fun trip_displayDateRange_singleDay_showsOneDate() {
        val trip = Trip(
            id = "t5",
            title = "X",
            destination = "Y",
            region = "Z",
            startDate = "2025-01-15",
            endDate = "2025-01-15",
            members = emptyList()
        )
        assertEquals("Jan 15", trip.displayDateRange)
    }

    @Test
    fun toTripDisplayDate_invalidFormat_returnsOriginal() {
        assertEquals("not-a-date", "not-a-date".toTripDisplayDate())
        assertEquals("2025-13-01", "2025-13-01".toTripDisplayDate())
    }

    @Test
    fun trip_displayDateRange_blankEndDate_usesStartOnly() {
        val trip = Trip(
            id = "t6",
            title = "X",
            destination = "Y",
            region = "Z",
            startDate = "2025-02-01",
            endDate = "",
            members = emptyList()
        )
        assertEquals("Feb 1", trip.displayDateRange)
    }

    @Test
    fun toTripDisplayDate_januaryAndDecember() {
        assertEquals("Jan 1", "2026-01-01".toTripDisplayDate())
        assertEquals("Dec 31", "2026-12-31".toTripDisplayDate())
    }

    @Test
    fun trip_defaults_statusAndImage() {
        val trip = Trip(
            id = "t7",
            title = "T",
            destination = "D",
            region = "R",
            startDate = "2025-01-01",
            endDate = "2025-01-02",
            members = emptyList()
        )
        assertEquals(TripStatus.UPCOMING, trip.status)
        assertEquals(null, trip.imageUrl)
    }
}
