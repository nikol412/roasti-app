package org.nikol.roasti

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform