package io.github.antistereov.start.widgets.widget.caldav.base.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "caldav_resources")
class CalDavResource(
    @Id val id: String? = null,
    val icsLink: String,
    val name: String,
    val color: String,
    val description: String?,
    val type: CalDavResourceType,
    val auth: CalDavAuthType,
    var lastUpdated: Instant? = null,
    val readOnly: Boolean,
    var entities: MutableList<CalDavEntity> = mutableListOf()
)