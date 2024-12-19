package io.github.antistereov.orbitab.service.geolocation.model

data class GeoLocationResponse(
    val ipVersion: Int,
    val ipAddress: String,
    val latitude: Float,
    val longitude: Float,
    val countryName: String,
    val countryCode: String,
    val timeZone: String,
    val zipCode: String?,
    val cityName: String,
    val regionName: String,
    val continent: String,
    val continentCode: String,
    val isProxy: Boolean,
    val language: String?,
    val timeZones: List<String>?,
    val tlds: List<String>?
)