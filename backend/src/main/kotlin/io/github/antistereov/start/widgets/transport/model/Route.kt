package io.github.antistereov.start.widgets.transport.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Route(
    @JsonProperty("PriceLevel") val priceLevel: Int,
    @JsonProperty("Price") val price: String,
    @JsonProperty("Net") val net: String,
    @JsonProperty("Duration") val duration: Int,
    @JsonProperty("Interchanges") val interchanges: Int,
    @JsonProperty("MotChain") val motChain: List<Mot>,
    @JsonProperty("FareZoneOrigin") val fareZoneOrigin: Int,
    @JsonProperty("FareZoneDestination") val fareZoneDestination: Int,
    @JsonProperty("RouteId") val routeId: Int,
    @JsonProperty("PartialRoutes") val partialRoutes: List<PartialRoute>,
    @JsonProperty("MapData") val mapData: List<String>,
)