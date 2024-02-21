package io.github.antistereov.start.widgets.widget.caldav.calendar.model

import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavAuthType
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResourceType
import java.time.Instant


data class CalDavCalendar(
    override val name: String,
    override val color: String,
    override val icsLink: String,
    override val description: String?,
    override val auth: CalDavAuthType,
    override var lastUpdated: Instant? = null,
    override val readOnly: Boolean,
    var events: List<CalDavEvent> = emptyList(),
) : CalDavResource(name, color, icsLink, description, CalDavResourceType.Calendar, auth, lastUpdated, readOnly, events)
