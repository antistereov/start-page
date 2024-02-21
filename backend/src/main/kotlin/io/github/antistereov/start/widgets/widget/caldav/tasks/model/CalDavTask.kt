package io.github.antistereov.start.widgets.widget.caldav.tasks.model

import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavEntity
import io.github.antistereov.start.widgets.widget.caldav.base.model.RRuleModel
import java.time.LocalDateTime

data class CalDavTask(
    val title: String,
    override val description: String?,
    override val location: String?,
    override val start: LocalDateTime,
    override val end: LocalDateTime,
    override val allDay: Boolean,
    override val rrule: RRuleModel?,
    val status: String,
    val priority: Int?
) : CalDavEntity(title, description, location, start, end, allDay, rrule)