package io.github.antistereov.start.widgets.nextcloud.model

import java.time.LocalDateTime

data class NewsItem(
    val author: String? = null,
    val body: String?,
    val contentHash: String?,
    val enclosureLink: String?,
    val enclosureMime: String?,
    val feedId: Int,
    val fingerprint: String?,
    val guid: String,
    val guidHash: String,
    val id: Long,
    val lastModified: String? = "0",
    val mediaDescription: String?,
    val mediaThumbnail: String?,
    val pubDate: LocalDateTime?,
    val rtl: Boolean = false,
    val starred: Boolean = false,
    val title: String?,
    val unread: Boolean = false,
    val updatedDate: String?,
    val url: String?,
)