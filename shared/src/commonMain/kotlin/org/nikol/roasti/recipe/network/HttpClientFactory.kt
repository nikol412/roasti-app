package org.nikol.roasti.recipe.network

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
