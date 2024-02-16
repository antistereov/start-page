package io.github.antistereov.start.global.model.exception

class ThirdPartyAuthorizationCanceledException(
    serviceName: String,
    error: String,
    message: String,
): RuntimeException(
    "Authorization for service: $serviceName denied. Error: $error. Message: $message."
)