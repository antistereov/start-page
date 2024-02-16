package io.github.antistereov.start.global.model.exception

class MissingCredentialsException(
    service: String,
    detail: String,
    userId: String
) : RuntimeException(
    "Missing $detail for service: $service user: $userId"
)