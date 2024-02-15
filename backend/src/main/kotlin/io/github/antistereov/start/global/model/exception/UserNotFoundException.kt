package io.github.antistereov.start.global.model.exception

class UserNotFoundException(userId: String) : RuntimeException("User not found: $userId")