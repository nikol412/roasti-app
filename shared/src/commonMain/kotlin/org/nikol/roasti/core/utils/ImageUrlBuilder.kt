package org.nikol.roasti.core.utils

import org.nikol.roasti.core.config.AppConfig
import org.nikol.roasti.core.network.ApiRoutes

fun imageUrl(imageId: String): String =
    "${AppConfig.BASE_URL}${ApiRoutes.UploadsImages}/$imageId"
