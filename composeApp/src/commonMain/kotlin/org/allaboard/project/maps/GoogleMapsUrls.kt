package org.allaboard.project.maps

private const val STATIC_MAP_ENDPOINT = "https://maps.googleapis.com/maps/api/staticmap"

/**
 * True when we have real coordinates for maps. Custom activities often store `0,0` when unset;
 * that is treated as missing (not a real place on the map).
 */
fun hasUsableMapCoordinates(latitude: Double?, longitude: Double?): Boolean {
    if (latitude == null || longitude == null) return false
    if (latitude == 0.0 && longitude == 0.0) return false
    return true
}

/**
 * Percent-encodes [value] for use in a URI query component (UTF-8 bytes).
 */
internal fun encodeUriQueryComponent(value: String): String = buildString {
    for (b in value.encodeToByteArray()) {
        val bb = b.toInt() and 0xFF
        when {
            bb in 0x41..0x5A || bb in 0x61..0x7A || bb in 0x30..0x39 ||
                bb == '-'.code || bb == '_'.code || bb == '.'.code || bb == '~'.code ->
                append(bb.toChar())
            else -> append("%${bb.toString(16).uppercase().padStart(2, '0')}")
        }
    }
}

/**
 * Google Maps Static API image URL for [latitude] / [longitude], or null if [apiKey] is blank.
 * See https://developers.google.com/maps/documentation/maps-static/start
 */
fun googleStaticMapImageUrl(
    latitude: Double,
    longitude: Double,
    apiKey: String,
): String? {
    if (apiKey.isBlank()) return null
    val center = "$latitude,$longitude"
    val marker = "color:red|$latitude,$longitude"
    return buildString {
        append(STATIC_MAP_ENDPOINT)
        append("?center=").append(encodeUriQueryComponent(center))
        append("&zoom=15")
        append("&size=640x360")
        append("&scale=2")
        append("&maptype=roadmap")
        append("&markers=").append(encodeUriQueryComponent(marker))
        append("&key=").append(encodeUriQueryComponent(apiKey))
    }
}

/**
 * Opens in the Maps app / browser. Prefers coordinates; otherwise searches by [location] text.
 */
fun googleMapsExternalUrl(latitude: Double?, longitude: Double?, location: String): String? {
    if (hasUsableMapCoordinates(latitude, longitude)) {
        return "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    }
    val trimmed = location.trim()
    if (trimmed.isNotEmpty()) {
        return "https://www.google.com/maps/search/?api=1&query=${encodeUriQueryComponent(trimmed)}"
    }
    return null
}
