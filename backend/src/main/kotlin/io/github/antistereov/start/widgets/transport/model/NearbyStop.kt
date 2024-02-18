package io.github.antistereov.start.widgets.transport.model

data class NearbyStop(
    val name: String,
    val distance: Double,
    val mot: List<String>,
)