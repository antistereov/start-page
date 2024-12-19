package io.github.antistereov.orbitab.user.dto

data class LoginUserDto(
    val username: String,
    val password: String,
    val deviceInfoDto: DeviceInfoDto
)