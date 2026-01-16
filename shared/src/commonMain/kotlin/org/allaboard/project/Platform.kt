package org.allaboard.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform