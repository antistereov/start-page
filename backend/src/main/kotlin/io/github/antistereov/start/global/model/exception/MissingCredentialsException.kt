package io.github.antistereov.start.global.model.exception

class MissingCredentialsException(
    userId: String,
    detail: String,
    service: String
) : RuntimeException(
    "Missing $detail for service: $service user: $userId"
)