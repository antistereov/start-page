package io.github.antistereov.start.widgets.widget.caldav.base.model

import java.time.Instant

open class CalDavResource(
    open val name: String,
    open val color: String,
    open val icsLink: String,
    open val description: String?,
    open val type: CalDavResourceType,
    open val auth: CalDavAuthType,
    open var lastUpdated: Instant?,
    open val readOnly: Boolean,
    open var entities: MutableList<CalDavEntity> = mutableListOf(),
)