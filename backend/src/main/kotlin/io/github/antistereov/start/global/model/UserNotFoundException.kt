package io.github.antistereov.start.global.model

class UserNotFoundException(userId: String) : RuntimeException("User not found: $userId")