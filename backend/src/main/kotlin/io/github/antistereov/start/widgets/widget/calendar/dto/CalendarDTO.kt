package io.github.antistereov.start.widgets.widget.calendar.dto

import io.github.antistereov.start.widgets.widget.calendar.model.CalendarEvent

data class CalendarDTO(
    val icsLink: String,
    val events: List<CalendarEvent>,
)