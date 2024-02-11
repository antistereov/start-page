package io.github.antistereov.start.widgets.nextcloud.model

data class NewsItem(
    val author: String? = null,
    val body: String? = null,
    val contentHash: String? = null,
    val enclosureLink: String? = null,
    val enclosureMime: String? = null,
    val feedId: Int,
    val fingerprint: String? = null,
    val guid: String,
    val guidHash: String,
    val id: Int,
    val lastModified: String? = "0",
    val mediaDescription: String? = null,
    val mediaThumbnail: String? = null,
    val pubDate: Int? = null,
    val rtl: Boolean = false,
    val starred: Boolean = false,
    val title: String? = null,
    val unread: Boolean = false,
    val updatedDate: String? = null,
    val url: String? = null
)