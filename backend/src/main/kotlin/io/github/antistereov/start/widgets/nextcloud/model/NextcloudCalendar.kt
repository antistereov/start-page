package io.github.antistereov.start.widgets.nextcloud.model


data class NextcloudCalendar(
    val name: String,
    val color: String,
    val icsLink: String,
    val description: String?,
    var events: List<Event>,
)
