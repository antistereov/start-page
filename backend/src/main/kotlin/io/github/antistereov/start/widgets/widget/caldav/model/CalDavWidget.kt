package io.github.antistereov.start.widgets.widget.caldav.model

import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource

data class CalDavWidget(
    var resources: MutableList<CalDavResource> = mutableListOf(),
)