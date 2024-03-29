package io.github.antistereov.start.widgets.widget.caldav.model

import java.time.LocalDateTime

open class CalDavEntity(
    open val uid: String? = null,
    open val summary: String,
    open val description: String?,
    open val location: String?,
    open val start: LocalDateTime?,
    open val end: LocalDateTime?,
    open val allDay: Boolean,
    open val rrule: RRuleModel? = null,
    open val status: String? = null,
    open val priority: Int? = null,
) {
    open fun copy(
        uid: String? = this.uid,
        summary: String = this.summary,
        description: String? = this.description,
        location: String? = this.location,
        start: LocalDateTime? = this.start,
        end: LocalDateTime? = this.end,
        allDay: Boolean = this.allDay,
        rrule: RRuleModel? = this.rrule,
        status: String? = this.status,
        priority: Int? = this.priority
    ): CalDavEntity {
        return CalDavEntity(uid, summary, description, location, start, end, allDay, rrule, status, priority)
    }
}