package org.allaboard.project.ui.screens.activityDetails

/**
 * In-memory stub implementation for the UI sprint.
 * Replace with [OpenTripMapRepository] or another implementation when the backend is ready.
 */
class StubActivityDetailsRepository : ActivityDetailsRepository {

    override suspend fun getDetails(activityId: String, fallbackTitle: String): Result<ActivityDetails> {
        return Result.success(stubDetailsFor(activityId, fallbackTitle))
    }

    private fun stubDetailsFor(id: String, fallbackTitle: String): ActivityDetails = when (id) {
        "1" -> ActivityDetails(
            id = id,
            title = "Pretty Place",
            location = "Tokyo, Japan",
            description = "Tucked into the hum of Tokyo, this spot feels like a quiet pause between centuries. Traditional architecture rises in warm reds and layered rooftops, framing the city below like a living postcard, while distant mountains soften the skyline at sunset.",
            rating = 0f,
            priceLevel = "$$",
            mapPinLabel = "Asakusa Shrine",
            imageUrl = null
        )
        "2" -> ActivityDetails(
            id = id,
            title = fallbackTitle.ifEmpty { "Scenic Spot" },
            location = "Kyoto, Japan",
            description = "A blend of history and nature. Stone paths wind through gardens and temples, with seasonal colours that change from cherry blossom pink to autumn gold.",
            rating = 0f,
            priceLevel = "$",
            mapPinLabel = "Arashiyama",
            imageUrl = null
        )
        else -> ActivityDetails(
            id = id,
            title = fallbackTitle.ifEmpty { "Place" },
            location = "Japan",
            description = "A memorable destination worth adding to your trip. Explore the area and discover local highlights.",
            rating = 0f,
            priceLevel = "$$",
            mapPinLabel = "Point of interest",
            imageUrl = null
        )
    }
}
