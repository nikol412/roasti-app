package org.nikol.roasti.data.network

import io.ktor.client.HttpClient

expect fun createHttpClient(
    accessTokenProvider: () -> String?,
): HttpClient
