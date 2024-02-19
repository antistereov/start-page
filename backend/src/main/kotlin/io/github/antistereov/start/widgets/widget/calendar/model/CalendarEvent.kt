package io.github.antistereov.start.widgets.widget.calendar.model

import java.time.LocalDateTime

data class CalendarEvent(
    val summary: String,
    val description: String?,
    val location: String?,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val allDay: Boolean,
    val rrule: RRuleModel?
)