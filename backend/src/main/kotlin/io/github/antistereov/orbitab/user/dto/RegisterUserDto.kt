package io.github.antistereov.orbitab.user.dto

data class RegisterUserDto(
    val username: String,
    val password: String,
    val deviceInfoDto: DeviceInfoRequestDto,
)
