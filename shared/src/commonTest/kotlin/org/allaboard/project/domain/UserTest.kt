package org.allaboard.project.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for User, BudgetLevel, and TravelVibe: creation, defaults, symbol/fromString behavior.
 */
internal class UserTest {

    @Test
    fun user_creation_storesProperties() {
        val user = User(
            id = "u1",
            displayName = "Alex",
            email = "alex@test.com",
            budget = BudgetLevel.HIGH,
            travelVibe = TravelVibe.ADVENTUROUS,
            interests = setOf("Hiking", "Food"),
            imageUrl = "https://example.com/photo.jpg"
        )
        assertEquals("u1", user.id)
        assertEquals("Alex", user.displayName)
        assertEquals("alex@test.com", user.email)
        assertEquals(BudgetLevel.HIGH, user.budget)
        assertEquals(TravelVibe.ADVENTUROUS, user.travelVibe)
        assertEquals(2, user.interests.size)
        assertEquals(true, user.interests.contains("Hiking"))
        assertEquals("https://example.com/photo.jpg", user.imageUrl)
    }

    @Test
    fun user_defaults_whenOptionalOmitted() {
        val user = User(id = "u2", displayName = "Sam", email = "sam@test.com")
        assertEquals(BudgetLevel.MEDIUM, user.budget)
        assertEquals(TravelVibe.BALANCED, user.travelVibe)
        assertEquals(0, user.interests.size)
        assertNull(user.imageUrl)
    }

    @Test
    fun budgetLevel_symbol_returnsExpectedStrings() {
        assertEquals("$", BudgetLevel.LOW.symbol)
        assertEquals("$$", BudgetLevel.MEDIUM.symbol)
        assertEquals("$$$", BudgetLevel.HIGH.symbol)
    }

    @Test
    fun travelVibe_fromString_validReturnsEnum() {
        assertEquals(TravelVibe.RELAXED, TravelVibe.fromString("relaxed"))
        assertEquals(TravelVibe.ADVENTUROUS, TravelVibe.fromString("adventurous"))
        assertEquals(TravelVibe.BALANCED, TravelVibe.fromString("balanced"))
    }

    @Test
    fun travelVibe_fromString_invalidReturnsNull() {
        assertNull(TravelVibe.fromString("unknown"))
        assertNull(TravelVibe.fromString(null))
    }
}
