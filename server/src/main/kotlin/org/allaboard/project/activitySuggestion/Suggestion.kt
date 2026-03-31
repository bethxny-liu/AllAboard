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
    val photos: List<PlacePhoto> = emptyList()
)

@Serializable
private data class PlacePhoto(
    @SerialName("photo_reference") val photoReference: String = ""
)

suspend fun suggestActivities(
    trip: Trip
) {
    val places = fetchPlaces("food and drink in japan", limit = 10)
    if (places.isEmpty()) {
        println("[places] no results returned")
        return
    }

    val existing = SupabaseConfig.client.from("activities")
        .select { filter { eq("trip_id", trip.id) } }
        .decodeList<ActivityInsert>()

    val existingKeys = existing.map { "${it.title}|${it.location}" }.toSet()

    places.forEach { place ->
        val key = "${place.name}|${place.formattedAddress}"
        if (key in existingKeys) return@forEach

        val insert = ActivityInsert(
            tripId = trip.id,
            title = place.name.ifBlank { "Food and drink" },
            location = place.formattedAddress,
            description = "Suggested by Google Places",
            rating = place.rating?.toFloat() ?: 0f,
            priceLevel = priceLevelToSymbol(place.priceLevel),
            mapPinLabel = place.name.ifBlank { "Food and drink" },
            imageUrl = null,
            link = place.placeId.takeIf { it.isNotBlank() }?.let { "https://www.google.com/maps/place/?q=place_id:$it" },
            type = ActivityType.RESTAURANT,
            addedBy = null
        )

        SupabaseConfig.client.from("activities").insert(insert)
    }
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
