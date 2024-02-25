package io.github.antistereov.start.widgets.widget.caldav.model

import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "caldav")
data class CalDavWidget(
    @Id var id: String? = null,
    var resources: MutableList<CalDavResource> = mutableListOf(),
)