package org.nikol.roasti.core.utils

import org.nikol.roasti.core.config.AppConfig
import org.nikol.roasti.core.network.ApiRoutes

fun imageUrl(imageIdOrUrl: String): String =
    if (imageIdOrUrl.startsWith("http://") || imageIdOrUrl.startsWith("https://")) {
        imageIdOrUrl
    } else {
        "${AppConfig.BASE_URL}${ApiRoutes.UploadsImages}/$imageIdOrUrl"
    }
