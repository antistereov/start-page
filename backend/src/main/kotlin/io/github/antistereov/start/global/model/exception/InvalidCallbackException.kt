package io.github.antistereov.start.global.model.exception

class InvalidCallbackException(
serviceName: String,
message: String,
): RuntimeException(
"Invalid callback for service: $serviceName. Message: $message."
)