package io.github.antistereov.start.widgets.widget.caldav.dto

import io.github.antistereov.start.widgets.widget.caldav.model.CalDavAuthType
import io.github.antistereov.start.widgets.widget.caldav.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.model.CalDavResourceType

class CreateCalDavResourceDTO(
    val icsLink: String,
    private val name: String,
    private val color: String,
    private val description: String?,
    private val type: CalDavResourceType,
    private val auth: CalDavAuthType,
    private val readOnly: Boolean,
) {

    fun toCalDavResource(): CalDavResource {
        return CalDavResource(
            icsLink = this.icsLink,
            name = this.name,
            color = this.color,
            description = this.description,
            type = this.type,
            auth = this.auth,
            readOnly = this.readOnly,
        )
    }
}