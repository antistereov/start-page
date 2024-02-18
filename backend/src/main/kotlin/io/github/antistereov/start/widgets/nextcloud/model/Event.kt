package io.github.antistereov.start.widgets.nextcloud.model

import java.time.ZonedDateTime

data class Event(
    val summary: String,
    val description: String?,
    val location: String?,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val allDay: Boolean,
    val rrule: RRuleModel?
)