package io.github.antistereov.start.global.model.exception

class ThirdPartyAPIException(
    service: String,
    message: String
) : RuntimeException(
    "Error from $service API: $message"
)