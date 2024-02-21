package io.github.antistereov.start.widgets.widget.caldav.calendar.model

import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavEntity
import io.github.antistereov.start.widgets.widget.caldav.base.model.RRuleModel
import java.time.LocalDateTime

class CalDavEvent(
    override val summary: String,
    override val description: String?,
    override val location: String?,
    override val start: LocalDateTime,
    override val end: LocalDateTime,
    override val allDay: Boolean,
    override val rrule: RRuleModel?
) : CalDavEntity(summary, description, location, start, end, allDay, rrule) {
    override fun copy(
        summary: String,
        description: String?,
        location: String?,
        start: LocalDateTime,
        end: LocalDateTime,
        allDay: Boolean,
        rrule: RRuleModel?
    ): CalDavEvent {
        return CalDavEvent(summary, description, location, start, end, allDay, rrule)
    }
}
