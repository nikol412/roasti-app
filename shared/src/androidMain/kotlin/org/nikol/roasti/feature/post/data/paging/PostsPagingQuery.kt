package org.nikol.roasti.feature.post.data.paging

data class PostsPagingQuery(
    val query: String = "",
) {
    val isDefaultFeed: Boolean
        get() = query.isBlank()
}
