package org.nikol.roasti.core.network

import io.ktor.client.HttpClient

expect fun createHttpClient(
    accessTokenProvider: () -> String?,
): HttpClient
