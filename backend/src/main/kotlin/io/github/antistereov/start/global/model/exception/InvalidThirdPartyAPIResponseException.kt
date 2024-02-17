package io.github.antistereov.start.global.model.exception

class InvalidThirdPartyAPIResponseException(
    uri: String,
    message: String,
): RuntimeException(
    "Invalid response when calling uri: $uri. Message: $message."
)
