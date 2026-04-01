package org.allaboard.project.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for Activity and ActivityType. Verifies Activity creation with all properties and with
 * optional defaults (rating, priceLevel, voteCount, imageUrl, link); [Activity.mapPinDisplay];
 * ActivityType enum values.
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

    @Test
    fun mapPinDisplay_usesTitleWhenMapPinLabelNull() {
        val a = Activity(
            title = "Tower",
            location = "X",
            mapPinLabel = null,
            type = ActivityType.LANDMARK
        )
        assertEquals("Tower", a.mapPinDisplay)
    }

    @Test
    fun mapPinDisplay_usesLabelWhenNonBlank() {
        val a = Activity(
            title = "Full name",
            location = "X",
            mapPinLabel = "Pin",
            type = ActivityType.LANDMARK
        )
        assertEquals("Pin", a.mapPinDisplay)
    }

    @Test
    fun mapPinDisplay_blankLabelFallsBackToTitle() {
        val a = Activity(
            title = "Only title",
            location = "X",
            mapPinLabel = "   ",
            type = ActivityType.RESTAURANT
        )
        assertEquals("Only title", a.mapPinDisplay)
    }

    @Test
    fun activity_optionalGeoAndMeta() {
        val a = Activity(
            title = "T",
            location = "L",
            type = ActivityType.EXPERIENCES,
            preference = "outdoor",
            latitude = 35.6,
            longitude = 139.7,
            addedBy = "user-1"
        )
        assertEquals("outdoor", a.preference)
        assertEquals(35.6, a.latitude)
        assertEquals(139.7, a.longitude)
        assertEquals("user-1", a.addedBy)
    }
}
