package io.github.antistereov.start.widgets.transport.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Mot(
    @JsonProperty("DlId") val dlId: String,
    @JsonProperty("StatelessId") val statelessId: String,
    @JsonProperty("Type") val type: String,
    @JsonProperty("Name") val name: String,
    @JsonProperty("Direction") val direction: String?,
    @JsonProperty("Changes") val changes: List<String>,
    @JsonProperty("Diva") val diva: Diva,
    @JsonProperty("TransportationCompany") val transportationCompany: String,
    @JsonProperty("OperatorCode") val operatorCode: String,
    @JsonProperty("ProductName") val productName: String,
    @JsonProperty("TrainNumber") val trainNumber: String
)