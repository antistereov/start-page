package io.github.antistereov.orbitab.user.model

import io.github.antistereov.orbitab.user.dto.DeviceInfoRequestDto

data class DeviceInfo(
    val deviceId: String,
    val browser: String? = null,
    val os: String? = null,
    val issuedAt: Long,
    val ipAddress: String?,
    val location: LocationInfo?,
) {
    data class LocationInfo(
        val latitude: Float,
        val longitude: Float,
        val cityName: String,
        val regionName: String,
        val countryCode: String,
    )

    fun toDto(): DeviceInfoRequestDto {
        return DeviceInfoRequestDto(
            deviceId = deviceId,
            browser = browser,
            os = os,
        )
    }
}
