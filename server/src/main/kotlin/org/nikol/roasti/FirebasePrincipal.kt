package org.nikol.roasti

import io.ktor.server.auth.Principal
import org.nikol.roasti.features.users.UserId

const val FIREBASE_AUTH = "firebase"

data class FirebasePrincipal(val uid: UserId) : Principal
