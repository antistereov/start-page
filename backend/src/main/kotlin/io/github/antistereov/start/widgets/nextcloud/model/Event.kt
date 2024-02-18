package io.github.antistereov.start.widgets.nextcloud.model

import java.time.LocalDateTime

data class Event(
    val summary: String,
    val description: String?,
    val location: String?,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val allDay: Boolean,
    val rrule: RRuleModel?
)