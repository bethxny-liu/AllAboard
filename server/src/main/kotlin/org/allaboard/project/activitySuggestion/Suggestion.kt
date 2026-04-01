package org.allaboard.project.activitySuggestion

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
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
import org.allaboard.project.domain.BudgetLevel
import io.github.jan.supabase.postgrest.from

private val env = dotenv {
    directory = "./server"
    filename = ".env"
    ignoreIfMissing = false
}

private val googlePlacesApiKey: String = env["GOOGLE_PLACES_API_KEY"] ?: ""
private const val PLACES_TEXT_SEARCH_URL = "https://places.googleapis.com/v1/places:searchText"
private const val GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json"

private val httpClient = HttpClient(CIO)
private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class PlacesTextSearchResponseV1(
    val places: List<PlaceResultV1> = emptyList()
)

@Serializable
private data class GeocodeResponse(
    val results: List<GeocodeResult> = emptyList()
)

@Serializable
private data class GeocodeResult(
    val geometry: GeocodeGeometry? = null,
    val types: List<String> = emptyList()
)

@Serializable
private data class GeocodeGeometry(
    val viewport: GeocodeViewport? = null
)

@Serializable
private data class GeocodeViewport(
    val northeast: PlaceLocation? = null,
    val southwest: PlaceLocation? = null
)

@Serializable
private data class PlaceResult(
    val name: String = "",
    @SerialName("formatted_address") val formattedAddress: String = "",
    val rating: Double? = null,
    @SerialName("price_level") val priceLevel: Int? = null,
    @SerialName("place_id") val placeId: String = "",
    val photos: List<PlacePhoto> = emptyList(),
    val geometry: PlaceGeometry? = null,
    val types: List<String> = emptyList()
)

@Serializable
private data class PlaceResultV1(
    val id: String = "",
    @SerialName("displayName") val displayName: PlaceDisplayName? = null,
    @SerialName("formattedAddress") val formattedAddress: String = "",
    val rating: Double? = null,
    @SerialName("priceLevel") val priceLevel: String? = null,
    val location: PlaceLocationV1? = null,
    val types: List<String> = emptyList(),
    val photos: List<PlacePhotoV1> = emptyList()
)

@Serializable
private data class PlaceDisplayName(
    val text: String = ""
)

@Serializable
private data class PlaceLocationV1(
    @SerialName("latitude") val lat: Double? = null,
    @SerialName("longitude") val lng: Double? = null
)

@Serializable
private data class PlacePhoto(
    @SerialName("photo_reference") val photoReference: String = ""
)

@Serializable
private data class PlacePhotoV1(
    val name: String = ""
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

private data class Bounds(
    val southwest: PlaceLocation,
    val northeast: PlaceLocation
)

suspend fun suggestActivities(
    trip: Trip
) {
    val destination = trip.destination.trim()
    if (destination.isBlank()) return

    val bounds = fetchBoundsForDestination(destination, trip.region)
    val priceLevels = trip.members.mapNotNull { member ->
        when (member.budget) {
            BudgetLevel.LOW -> 1
            BudgetLevel.MEDIUM -> 2
            BudgetLevel.HIGH -> 3
        }
    }.distinct()

    val interests = trip.members.flatMap { it.interests }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    val normalizedInterests = interests.map { normalizeInterest(it) }
        .filter { it.isNotBlank() }
        .distinct()

    val topics = interests.mapNotNull { toSearchTopic(it, destination) }
        .distinctBy { it.query }
        .toMutableList()

    val hasFoodPreference = normalizedInterests.contains("food and drink")
    if (topics.none { it.preference == "food and drink" }) {
        topics.add(SearchTopic("food and drink", "food and drink in $destination", ActivityType.RESTAURANT))
    }

    if (topics.isEmpty()) return

    val existing = SupabaseConfig.client.from("activities")
        .select { filter { eq("trip_id", trip.id) } }
        .decodeList<Activity>()

    val existingKeys = existing.mapNotNull { activity ->
        normalizeTitleForDedupe(activity.title).takeIf { key -> key.isNotBlank() }
    }.toMutableSet()
    var insertedCount = 0

    //Ignore budget for sightseeing, since sights stay low in budget but are relevant for all travelers
    topics.forEach { topic ->
        val isFoodTopic = topic.preference == "food and drink"
        val limit = if (isFoodTopic) {
            if (hasFoodPreference) 8 else 3
        } else {
            5
        }
        val effectivePriceLevels = if (topic.preference.equals("sightseeing", ignoreCase = true)) {
            emptyList()
        } else {
            priceLevels
        }
        val places = fetchPlaces(
            topic.query,
            limit = limit,
            bounds = bounds,
            priceLevels = effectivePriceLevels
        )
        if (places.isEmpty()) return@forEach

        places.forEach { place ->
            val key = normalizeTitleForDedupe(place.name)
            if (key.isBlank() || key in existingKeys) return@forEach
            existingKeys.add(key)

            //If the place is related to a restaurant, classify it as such regardless of what search it came from
            val isRestaurant = place.types.any { type ->
                type == "restaurant" || type == "meal_takeaway" || type == "meal_delivery" || type == "cafe"
            }
            val effectivePreference = if (isRestaurant) "Food & drink" else topic.preference
            val effectiveType = if (isRestaurant) ActivityType.RESTAURANT else topic.type

            val photoUrl = place.photos.firstOrNull()?.photoReference
                ?.takeIf { it.isNotBlank() }
                ?.let { "https://places.googleapis.com/v1/$it/media?maxWidthPx=800&key=$googlePlacesApiKey" }

            val insert = ActivityInsert(
                tripId = trip.id,
                title = place.name.ifBlank { effectivePreference },
                location = place.formattedAddress,
                description = "Suggested for ${effectivePreference}",
                rating = place.rating?.toFloat() ?: 0f,
                priceLevel = priceLevelToSymbol(place.priceLevel),
                mapPinLabel = place.name.ifBlank { effectivePreference },
                imageUrl = photoUrl,
                link = place.placeId.takeIf { it.isNotBlank() }
                    ?.let { "https://www.google.com/maps/place/?q=place_id:$it" },
                type = effectiveType,
                preference = effectivePreference,
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
    val normalized = normalizeInterest(interest)
    if (normalized.isBlank()) return null

    val type = when (normalized) {
        "food and drink" -> ActivityType.RESTAURANT
        "sightseeing" -> ActivityType.LANDMARK
        "arts and culture" -> ActivityType.EXPERIENCES
        "nightlife" -> ActivityType.EXPERIENCES
        "outdoors" -> ActivityType.EXPERIENCES
        "shopping" -> ActivityType.EXPERIENCES
        else -> ActivityType.EXPERIENCES
    }

    val queryLabel = if (normalized == "sightseeing") {
        "top sights" //Ensures most famous sights
    } else {
        normalized
    }

    return SearchTopic(normalized, "$queryLabel in $destination", type)
}

private fun normalizeInterest(value: String): String {
    return value
        .trim()
        .lowercase()
        .replace("&", "and")
        .replace(Regex("\\s+"), " ")
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

private suspend fun fetchPlaces(
    query: String,
    limit: Int,
    bounds: Bounds?,
    priceLevels: List<Int>
): List<PlaceResult> {
    if (googlePlacesApiKey.isBlank()) return emptyList()

    val restriction = bounds?.let {
        val sw = it.southwest
        val ne = it.northeast
        val swLat = sw.lat ?: return@let null
        val swLng = sw.lng ?: return@let null
        val neLat = ne.lat ?: return@let null
        val neLng = ne.lng ?: return@let null
        LocationRestriction(
            rectangle = Rectangle(
                low = LatLng(latitude = swLat, longitude = swLng),
                high = LatLng(latitude = neLat, longitude = neLng)
            )
        )
    } ?: return emptyList()

    val mappedPriceLevels = priceLevels.mapNotNull { level ->
        when (level) {
            1 -> "PRICE_LEVEL_INEXPENSIVE"
            2 -> "PRICE_LEVEL_MODERATE"
            3 -> "PRICE_LEVEL_EXPENSIVE"
            4 -> "PRICE_LEVEL_VERY_EXPENSIVE"
            else -> null
        }
    }.distinct().ifEmpty { null }

    val request = TextSearchRequest(
        textQuery = query,
        locationRestriction = restriction,
        priceLevels = mappedPriceLevels
    )

    val responseText = httpClient.post(PLACES_TEXT_SEARCH_URL) {
        contentType(ContentType.Application.Json)
        headers.append("X-Goog-Api-Key", googlePlacesApiKey)
        headers.append(
            "X-Goog-FieldMask",
            "places.displayName,places.formattedAddress,places.rating,places.priceLevel,places.id,places.location,places.types,places.photos"
        )
        setBody(json.encodeToString(TextSearchRequest.serializer(), request))
    }.bodyAsText()

    val payload = json.decodeFromString(PlacesTextSearchResponseV1.serializer(), responseText)
    return payload.places.map { place ->
        val mappedPriceLevel = when (place.priceLevel) {
            "PRICE_LEVEL_FREE" -> 0
            "PRICE_LEVEL_INEXPENSIVE" -> 1
            "PRICE_LEVEL_MODERATE" -> 2
            "PRICE_LEVEL_EXPENSIVE" -> 3
            "PRICE_LEVEL_VERY_EXPENSIVE" -> 4
            else -> null
        }
        PlaceResult(
            name = place.displayName?.text ?: "",
            formattedAddress = place.formattedAddress,
            rating = place.rating,
            priceLevel = mappedPriceLevel,
            placeId = place.id,
            photos = place.photos.map { PlacePhoto(photoReference = it.name) },
            geometry = place.location?.let {
                PlaceGeometry(PlaceLocation(lat = it.lat, lng = it.lng))
            },
            types = place.types
        )
    }.take(limit)
}

private suspend fun fetchBoundsForDestination(destination: String, region: String): Bounds? {
    if (googlePlacesApiKey.isBlank()) return null

    val cityQuery = listOf(region.trim(), destination.trim())
        .filter { it.isNotBlank() }
        .joinToString(", ")
    val cityBounds = fetchBoundsForQuery(cityQuery, requiredTypes = setOf("locality", "postal_town"))
    if (cityBounds != null) return cityBounds

    val countryQuery = destination.trim().ifBlank { region.trim() }
    return fetchBoundsForQuery(countryQuery, requiredTypes = setOf("country"))
}

private suspend fun fetchBoundsForQuery(query: String, requiredTypes: Set<String>): Bounds? {
    if (query.isBlank()) return null

    val encodedAddress = URLEncoder.encode(query, Charsets.UTF_8)
    val url = "$GEOCODE_URL?address=$encodedAddress&key=$googlePlacesApiKey"
    val responseText = httpClient.get(url).bodyAsText()
    val payload = json.decodeFromString(GeocodeResponse.serializer(), responseText)
    val result = payload.results.firstOrNull { result ->
        result.types.any { it in requiredTypes }
    } ?: return null
    val viewport = result.geometry?.viewport ?: return null
    val sw = viewport.southwest
    val ne = viewport.northeast
    if (sw?.lat == null || sw.lng == null || ne?.lat == null || ne.lng == null) return null
    return Bounds(sw, ne)
}

@Serializable
private data class TextSearchRequest(
    val textQuery: String,
    val locationRestriction: LocationRestriction? = null,
    val priceLevels: List<String>? = null
)

@Serializable
private data class LocationRestriction(
    val rectangle: Rectangle
)

@Serializable
private data class Rectangle(
    val low: LatLng,
    val high: LatLng
)

@Serializable
private data class LatLng(
    val latitude: Double,
    val longitude: Double
)

private fun normalizeTitleForDedupe(title: String): String {
    val normalized = title
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
    if (normalized.isBlank()) return ""

    val words = normalized.split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.size >= 2) {
        return words.take(2).joinToString(" ")
    }
    return words.joinToString(" ")
}
