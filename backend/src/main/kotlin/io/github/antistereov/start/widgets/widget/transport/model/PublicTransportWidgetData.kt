package io.github.antistereov.start.widgets.widget.transport.model

import io.github.antistereov.start.widgets.widget.location.model.PublicTransportCompany

data class PublicTransportWidgetData(
    var transportCompanies: MutableSet<PublicTransportCompany> =  mutableSetOf(),
    var stops: MutableSet<PublicTransportStop> = mutableSetOf(),
)