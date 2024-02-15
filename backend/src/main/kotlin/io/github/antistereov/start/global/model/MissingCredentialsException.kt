package io.github.antistereov.start.global.model

class MissingCredentialsException(
    userId: String,
    detail: String,
    service: String
) : RuntimeException(
    "Missing $detail for service: $service user: $userId"
)