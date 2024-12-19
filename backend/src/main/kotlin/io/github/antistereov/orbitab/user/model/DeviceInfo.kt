package io.github.antistereov.orbitab.user.model

import io.github.antistereov.orbitab.user.dto.DeviceInfoDto

data class DeviceInfo(
    val deviceId: String,
    val browser: String? = null,
    val os: String? = null,
    val refreshToken: String,
    val issuedAt: Long,
) {
    fun toDto(): DeviceInfoDto {
        return DeviceInfoDto(
            deviceId = deviceId,
            browser = browser,
            os = os,
        )
    }
}
