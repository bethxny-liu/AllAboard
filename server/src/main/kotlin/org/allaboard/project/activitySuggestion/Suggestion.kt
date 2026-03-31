package org.allaboard.project.activitySuggestion

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import java.net.URLEncoder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.allaboard.project.SupabaseConfig
import org.allaboard.project.activity.ActivityInsert
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.domain.Trip
import io.github.jan.supabase.postgrest.from

private val env = dotenv {
    directory = "./server"
    filename = ".env"
    ignoreIfMissing = false
}

private val googlePlacesApiKey: String = env["GOOGLE_PLACES_API_KEY"] ?: ""
private const val PLACES_TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json"

private val httpClient = HttpClient(CIO)
private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class PlacesTextSearchResponse(
    val results: List<PlaceResult> = emptyList()
)

@Serializable
private data class PlaceResult(
    val name: String = "",
    @SerialName("formatted_address") val formattedAddress: String = "",
    val rating: Double? = null,
    @SerialName("price_level") val priceLevel: Int? = null,
    @SerialName("place_id") val placeId: String = "",
    val photos: List<PlacePhoto> = emptyList(),
    val geometry: PlaceGeometry? = null
)

@Serializable
private data class PlacePhoto(
    @SerialName("photo_reference") val photoReference: String = ""
)

@Serializable
private data class PlaceGeometry(
    val location: PlaceLocation? = null
)

@Serializable
private data class PlaceLocation(
    val lat: Double? = null,
    val lng: Double? = null
)

private data class SearchTopic(
    val preference: String,
    val query: String,
    val type: ActivityType
)

suspend fun suggestActivities(
    trip: Trip
) {
    val destination = trip.destination.trim()
    if (destination.isBlank()) return

    val interests = trip.members.flatMap { it.interests }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    val topics = interests.mapNotNull { toSearchTopic(it, destination) }
        .distinctBy { it.query }

    if (topics.isEmpty()) return

    val existing = SupabaseConfig.client.from("activities")
        .select { filter { eq("trip_id", trip.id) } }
        .decodeList<Activity>()

    val existingKeys = existing.map { "${it.title}|${it.location}" }.toMutableSet()
    var insertedCount = 0

    topics.forEach { topic ->
        val places = fetchPlaces(topic.query, limit = 5)
        if (places.isEmpty()) return@forEach

        places.forEach { place ->
            val key = "${place.name}|${place.formattedAddress}"
            if (key in existingKeys) return@forEach
            existingKeys.add(key)

            val insert = ActivityInsert(
                tripId = trip.id,
                title = place.name.ifBlank { topic.preference },
                location = place.formattedAddress,
                description = "Suggested for ${topic.preference}",
                rating = place.rating?.toFloat() ?: 0f,
                priceLevel = priceLevelToSymbol(place.priceLevel),
                mapPinLabel = place.name.ifBlank { topic.preference },
                imageUrl = null,
                link = place.placeId.takeIf { it.isNotBlank() }
                    ?.let { "https://www.google.com/maps/place/?q=place_id:$it" },
                type = topic.type,
                preference = topic.preference,
                latitude = place.geometry?.location?.lat,
                longitude = place.geometry?.location?.lng,
                addedBy = null
            )

            SupabaseConfig.client.from("activities").insert(insert)
            insertedCount += 1
        }
    }

    if (insertedCount == 0) {
        println("[places] no new activities inserted")
    } else {
        println("[places] inserted $insertedCount activities")
    }
}

private fun toSearchTopic(interest: String, destination: String): SearchTopic? {
    val trimmed = interest.trim()
    if (trimmed.isBlank()) return null

    val type = when (trimmed.lowercase()) {
        "food and drink" -> ActivityType.RESTAURANT
        "sightseeing" -> ActivityType.LANDMARK
        "arts and culture" -> ActivityType.EXPERIENCES
        "nightlife" -> ActivityType.EXPERIENCES
        "outdoors" -> ActivityType.EXPERIENCES
        "shopping" -> ActivityType.EXPERIENCES
        else -> ActivityType.EXPERIENCES
    }

    return SearchTopic(trimmed, "$trimmed in $destination", type)
}

private fun priceLevelToSymbol(priceLevel: Int?): String {
    return when (priceLevel) {
        0, 1 -> "$"
        2 -> "$$"
        3 -> "$$$"
        4 -> "$$$$"
        else -> "$$"
    }
}

private suspend fun fetchPlaces(query: String, limit: Int): List<PlaceResult> {
    if (googlePlacesApiKey.isBlank()) return emptyList()

    val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8)
    val url = "$PLACES_TEXT_SEARCH_URL?query=$encodedQuery&key=$googlePlacesApiKey"
    val responseText = httpClient.get(url).bodyAsText()
    val payload = json.decodeFromString(PlacesTextSearchResponse.serializer(), responseText)
    return payload.results.take(limit)
}
