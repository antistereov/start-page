package io.github.antistereov.start.widgets.widget.transport.dvb.model

data class NearbyStop(
    val name: String,
    val distance: Double,
    val mot: List<String>,
)