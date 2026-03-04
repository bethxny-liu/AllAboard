package org.allaboard.project.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for Activity and ActivityType: creation, defaults, and enum values.
 */
internal class ActivityTest {

    @Test
    fun activity_creation_storesProperties() {
        val activity = Activity(
            id = "a1",
            title = "Senso-ji Temple",
            location = "Tokyo",
            description = "Historic temple",
            rating = 4.5f,
            priceLevel = "$$",
            mapPinLabel = "Senso-ji",
            voteCount = 10,
            imageUrl = "https://example.com/sensoji.jpg",
            link = "https://example.com",
            type = ActivityType.LANDMARK
        )
        assertEquals("a1", activity.id)
        assertEquals("Senso-ji Temple", activity.title)
        assertEquals("Tokyo", activity.location)
        assertEquals("Historic temple", activity.description)
        assertEquals(4.5f, activity.rating)
        assertEquals("$$", activity.priceLevel)
        assertEquals("Senso-ji", activity.mapPinLabel)
        assertEquals(10, activity.voteCount)
        assertEquals("https://example.com/sensoji.jpg", activity.imageUrl)
        assertEquals("https://example.com", activity.link)
        assertEquals(ActivityType.LANDMARK, activity.type)
    }

    @Test
    fun activity_defaults_whenOptionalOmitted() {
        val activity = Activity(
            id = "a2",
            title = "Food Tour",
            location = "Osaka",
            description = "Eat",
            mapPinLabel = "Food Tour",
            voteCount = 0,
            type = ActivityType.RESTAURANT
        )
        assertEquals(0f, activity.rating)
        assertEquals("$$", activity.priceLevel)
        assertEquals(0, activity.voteCount)
        assertEquals(null, activity.imageUrl)
        assertEquals(null, activity.link)
    }

    @Test
    fun activityType_enumValues_exist() {
        assertEquals(ActivityType.LANDMARK, ActivityType.LANDMARK)
        assertEquals(ActivityType.RESTAURANT, ActivityType.RESTAURANT)
        assertEquals(ActivityType.EXPERIENCES, ActivityType.EXPERIENCES)
    }
}
