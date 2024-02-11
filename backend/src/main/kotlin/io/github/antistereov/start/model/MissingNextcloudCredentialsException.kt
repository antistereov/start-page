package io.github.antistereov.start.model

class MissingNextcloudCredentialsException(userId: String, detail: String) : RuntimeException("Missing $detail for user: $userId")