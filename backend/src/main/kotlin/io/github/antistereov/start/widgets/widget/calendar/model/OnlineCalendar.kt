package io.github.antistereov.start.widgets.widget.calendar.model

import java.time.Instant


data class OnlineCalendar(
    val name: String,
    val color: String,
    val icsLink: String,
    val description: String?,
    val auth: CalendarAuth,
    val type: CalendarType,
    var lastUpdated: Instant? = null,
    val timezone: String?,
    val readOnly: Boolean,
    var events: List<CalendarEvent> = emptyList(),
)
