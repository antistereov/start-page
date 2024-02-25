package io.github.antistereov.start.global.exception

class ThirdPartyAuthorizationCanceledException(
    serviceName: String,
    error: String,
    message: String,
): RuntimeException(
    "Authorization denied for service: $serviceName, error: $error, message: $message"
)
