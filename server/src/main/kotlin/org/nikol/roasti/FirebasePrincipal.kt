package org.nikol.roasti

import io.ktor.server.auth.Principal

const val FIREBASE_AUTH = "firebase"

data class FirebasePrincipal(val uid: String) : Principal
