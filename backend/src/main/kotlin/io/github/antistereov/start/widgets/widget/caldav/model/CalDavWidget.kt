package io.github.antistereov.start.widgets.widget.caldav.model

import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "caldav")
data class CalDavWidget(
    val id: Long,
    var resources: MutableList<CalDavResource> = mutableListOf(),
)