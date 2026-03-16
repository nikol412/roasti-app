package org.nikol.roasti.data.upload

import org.nikol.roasti.AppConfig

fun imageUrl(imageId: String): String =
    "${AppConfig.BASE_URL}/api/v1/uploads/images/$imageId"
