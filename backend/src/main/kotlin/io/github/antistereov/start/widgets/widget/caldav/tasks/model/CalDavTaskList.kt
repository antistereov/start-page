package io.github.antistereov.start.widgets.widget.caldav.tasks.model

import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavAuthType
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResourceType
import java.time.Instant

data class CalDavTaskList(
    override val name: String,
    override val color: String,
    override val icsLink: String,
    override val description: String?,
    override val auth: CalDavAuthType,
    override var lastUpdated: Instant? = null,
    override val readOnly: Boolean,
    var tasks: List<CalDavTask> = emptyList(),
) : CalDavResource(name, color, icsLink, description, CalDavResourceType.TaskList, auth, lastUpdated, readOnly, tasks)
