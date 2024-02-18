package io.github.antistereov.start.widgets.nextcloud.model

import kotlinx.serialization.Serializable

@Serializable
data class NextcloudCalendar(
    val name: String,
    val color: String,
    val icsLink: String,
    val description: String?,
)
