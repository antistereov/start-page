package io.github.antistereov.start.widgets.widget.caldav.base.model

import java.time.LocalDateTime

open class CalDavEntity(
    open val summary: String,
    open val description: String?,
    open val location: String?,
    open val start: LocalDateTime,
    open val end: LocalDateTime,
    open val allDay: Boolean,
    open val rrule: RRuleModel?
) {
    open fun copy(
        summary: String = this.summary,
        description: String? = this.description,
        location: String? = this.location,
        start: LocalDateTime = this.start,
        end: LocalDateTime = this.end,
        allDay: Boolean = this.allDay,
        rrule: RRuleModel? = this.rrule
    ): CalDavEntity {
        return CalDavEntity(summary, description, location, start, end, allDay, rrule)
    }
}