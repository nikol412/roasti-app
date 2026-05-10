package org.nikol.roasti.features.common

data class Page<T>(
    val items: List<T>,
    val currentPage: Int,
    val itemsCount: Int,
    val lastPage: Int,
    val nextPage: Int,
)
