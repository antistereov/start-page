package io.github.antistereov.start.global.model.exception

class InvalidThirdPartyAPIResponseException(
    serviceName: String,
    message: String,
): RuntimeException(
    "Invalid response for service: $serviceName. Message: $message."
)