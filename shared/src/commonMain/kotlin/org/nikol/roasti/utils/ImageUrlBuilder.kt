package org.nikol.roasti.utils

import org.nikol.roasti.AppConfig
import org.nikol.roasti.data.network.ApiRoutes

fun imageUrl(imageId: String): String =
    "${AppConfig.BASE_URL}${ApiRoutes.UploadsImages}/$imageId"
