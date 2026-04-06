package org.allaboard.project.activity

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import java.net.URLEncoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Prefer runtime environment variables (Docker/CI), fall back to server/.env for local dev.
private fun envOrDotenv(key: String): String {
    val fromEnv = System.getenv(key)
    if (!fromEnv.isNullOrBlank()) return fromEnv

    val envFile = dotenv {
        directory = System.getenv("DOTENV_DIR") ?: "./server"
        filename = ".env"
        ignoreIfMissing = true
    }
    return envFile[key] ?: ""
}

private val googlePlacesApiKey: String = envOrDotenv("GOOGLE_PLACES_API_KEY")
private const val GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json"

private val httpClient = HttpClient(CIO)
private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class GeocodeResponse(
    val results: List<GeocodeResult> = emptyList()
)

@Serializable
private data class GeocodeResult(
    val geometry: GeocodeGeometry? = null
)

@Serializable
private data class GeocodeGeometry(
    val location: GeocodeLocation? = null
)

@Serializable
private data class GeocodeLocation(
    val lat: Double? = null,
    val lng: Double? = null
)

suspend fun geocodeLocation(address: String, city: String?, country: String?): Pair<Double, Double>? {
    if (googlePlacesApiKey.isBlank()) return null
    val trimmed = address.trim()
    if (trimmed.isBlank()) return null

    val scopedQuery = listOf(trimmed, city?.trim().orEmpty(), country?.trim().orEmpty())
        .filter { it.isNotBlank() }
        .joinToString(", ")

    val encodedAddress = URLEncoder.encode(scopedQuery, Charsets.UTF_8)
    val url = "$GEOCODE_URL?address=$encodedAddress&key=$googlePlacesApiKey"
    val responseText = httpClient.get(url).bodyAsText()
    val payload = json.decodeFromString(GeocodeResponse.serializer(), responseText)
    val location = payload.results.firstOrNull()?.geometry?.location ?: return null
    val lat = location.lat ?: return null
    val lng = location.lng ?: return null
    return lat to lng
}
