package io.github.antistereov.orbitab.user.dto

data class DeviceInfoDto(
    val deviceId: String,
    val browser: String? = null,
    val os: String? = null,
)
