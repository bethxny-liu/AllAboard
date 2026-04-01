package org.allaboard.project.maps

internal actual fun mapsStaticApiKey(): String =
    System.getenv("MAPS_STATIC_API_KEY") ?: ""
